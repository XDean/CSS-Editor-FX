package xdean.css.editor.controller;

import static xdean.css.editor.context.Context.LAST_FILE_PATH;
import static xdean.jex.util.lang.ExceptionUtil.uncheck;
import static xdean.jex.util.task.TaskUtil.andFinal;
import static xdean.jfxex.bean.BeanConvertUtil.toDoubleBinding;
import static xdean.jfxex.bean.BeanUtil.*;
import static xdean.jfxex.event.EventHandlers.consume;
import static xdean.jfxex.event.EventHandlers.consumeIf;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Named;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.binding.ObjectBinding;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import xdean.css.editor.context.setting.EditActions;
import xdean.css.editor.context.setting.FileActions;
import xdean.css.editor.context.setting.PreferenceSettings;
import xdean.css.editor.control.CssEditor;
import xdean.css.editor.feature.CssEditorFeature;
import xdean.css.editor.model.FileWrapper;
import xdean.css.editor.service.ContextService;
import xdean.css.editor.service.DialogService;
import xdean.css.editor.service.RecentFileService;
import xdean.css.editor.service.SkinService;
import xdean.jex.extra.tryto.Try;
import xdean.jex.log.Logable;
import xdean.jex.util.cache.CacheUtil;
import xdean.jex.util.collection.ListUtil;
import xdean.jex.util.file.FileUtil;
import xdean.jfx.spring.FxInitializable;
import xdean.jfx.spring.annotation.FxController;
import xdean.jfx.spring.context.FxContext;
import xdean.jfxex.bean.annotation.CheckNull;

@FxController(fxml = "/fxml/MainFrame.fxml")
public class MainFrameController implements FxInitializable, Logable,
    ContextService, CssEditorFeature {

  @FXML
  ScrollBar verticalScrollBar, horizontalScrollBar;

  @FXML
  TabPane tabPane;

  @FXML
  VBox bottomExtraPane;

  @FXML
  StatusBarController statusBarController;

  @Inject
  @Named(FxContext.FX_PRIMARY_STAGE)
  Stage stage;

  @Inject
  MainFrameModel model;

  @Inject
  RecentFileService recentService;

  @Inject
  OptionsController oc;

  @Inject
  SkinService skinManager;

  @Inject
  DialogService messageService;

  @Inject
  PreferenceSettings options;

  @Inject
  EditActions editActions;

  @Inject
  FileActions fileActions;

  @Override
  public void initAfterFxSpringReady() {
    initField();
    initBind();
    Try.to(() -> openLastFile()).onException(e -> error("Load last closed file fail.", e));

    stage.titleProperty().bind(map(model.currentFile, f -> (f == null ? "" : f.getFileName()) + " - CSS Editor FX"));
    stage.setOnCloseRequest(e -> {
      e.consume();
      exit();
    });
  }

  private void initField() {
    statusBarController.override.bindBidirectional(nestBooleanProp(model.currentEditor, m -> m.overrideProperty()));
    statusBarController.area.bind(model.currentEditor);
  }

  private void initBind() {

    // disable
    BooleanBinding nullArea = model.currentEditor.isNull();
    editActions.format().disableProperty().bind(nullArea);
    editActions.comment().disableProperty().bind(nullArea);
    editActions.find().disableProperty().bind(nullArea);
    editActions.undo().disableProperty()
        .bind(nullArea.or(nestBooleanValue(model.currentEditor, c -> c.undoAvailableProperty()).not()));
    editActions.redo().disableProperty()
        .bind(nullArea.or(nestBooleanValue(model.currentEditor, c -> c.redoAvailableProperty()).not()));
    fileActions.close().disableProperty().bind(nullArea);
    fileActions.saveAs().disableProperty().bind(nullArea);
    fileActions.save().disableProperty().bind(fileActions.saveAs().disableProperty().or(model.currentModified.not()));
    fileActions.revert().disableProperty().bind(model.currentFile.isNull()
        .or(model.currentModified.not())
        .or(nestBooleanValue(model.currentTabEntity, t -> t.getEditor().isNewBinding())));

    // scroll bar
    DoubleBinding editorTextHeight = nestDoubleValue(model.currentEditor, c -> c.totalHeightEstimateProperty());
    DoubleBinding editorHeight = nestDoubleValue(model.currentEditor, c -> c.heightProperty());
    verticalScrollBar.maxProperty().bind(editorTextHeight.subtract(editorHeight));
    verticalScrollBar.visibleAmountProperty().bind(
        Bindings.max(0, toDoubleBinding(verticalScrollBar.maxProperty())
            .multiply(editorHeight).divide(editorTextHeight)));
    verticalScrollBar.valueProperty()
        .bindBidirectional(nestDoubleProp(model.currentEditor, c -> c.estimatedScrollYProperty()).normalize());
    verticalScrollBar.visibleProperty().bind(model.currentEditor.isNotNull()
        .and(verticalScrollBar.maxProperty().greaterThan(verticalScrollBar.visibleAmountProperty())));
    verticalScrollBar.managedProperty().bind(Bindings.createBooleanBinding(
        () -> andFinal(() -> verticalScrollBar.isVisible(), v -> verticalScrollBar.getParent().layout()),
        verticalScrollBar.visibleProperty()));

    DoubleBinding editorTextWidth = nestDoubleValue(model.currentEditor, c -> c.totalWidthEstimateProperty());
    DoubleBinding editorWidth = nestDoubleValue(model.currentEditor, c -> c.widthProperty());
    horizontalScrollBar.maxProperty().bind(editorTextWidth.subtract(editorWidth));
    horizontalScrollBar.visibleAmountProperty().bind(
        Bindings.max(0, horizontalScrollBar.maxProperty()
            .multiply(editorWidth).divide(editorTextWidth)));
    horizontalScrollBar.valueProperty()
        .bindBidirectional(nestDoubleProp(model.currentEditor, c -> c.estimatedScrollXProperty()).normalize());
    horizontalScrollBar.visibleProperty().bind(options.wrapText().booleanProperty().not().and(model.currentEditor.isNotNull())
        .and(horizontalScrollBar.maxProperty().greaterThan(horizontalScrollBar.visibleAmountProperty())));
    horizontalScrollBar.managedProperty().bind(Bindings.createBooleanBinding(
        () -> andFinal(() -> horizontalScrollBar.isVisible(), v -> horizontalScrollBar.getParent().layout()),
        horizontalScrollBar.visibleProperty()));

    // tabList
    Bindings.bindContent(tabPane.getTabs(), model.tabEntities);
    tabPane.getSelectionModel().selectedItemProperty().addListener((ob, o, n) -> model.currentTabEntity.set((CssEditorTab) n));
    model.currentTabEntity.addListener((ob, o, n) -> tabPane.getSelectionModel().select(n));

    // events
    eventNode().addEventHandler(fileActions.newFile().getEventType(), e -> newFile());
    eventNode().addEventHandler(fileActions.open().getEventType(), e -> open(e.getData()));

    eventNode().addEventFilter(fileActions.exit().getEventType(), consumeIf(e -> !canExit()));
    eventNode().addEventHandler(fileActions.exit().getEventType(), e -> exit());
  }

  @Override
  public void bind(CssEditor editor) {
    editor.addEventHandler(fileActions.save().getEventType(), e -> save());
    editor.addEventHandler(fileActions.saveAs().getEventType(), e -> saveAs(e.getData()));
    editor.addEventHandler(fileActions.revert().getEventType(), e -> revert());
    editor.addEventFilter(fileActions.close().getEventType(), consumeIf(e -> !canClose(editor)));
    editor.addEventHandler(fileActions.close().getEventType(), e -> close(editor));

    editor.addEventHandler(editActions.undo().getEventType(), e -> editor.undo());
    editor.addEventHandler(editActions.redo().getEventType(), e -> editor.redo());
  }

  private void newFile() {
    openFile(FileWrapper.newFile(model.nextNewOrder()));
  }

  private boolean open(Optional<Path> data) {
    Path path;
    if (data.isPresent()) {
      path = data.get();
    } else {
      FileChooser fileChooser = new FileChooser();
      fileChooser.setTitle("Open");
      fileChooser.getExtensionFilters().add(new ExtensionFilter("Style Sheet", "*.css"));
      fileChooser.setInitialDirectory(recentService.getLatestFile().map(p -> p.getParent().toFile()).orElse(new File(".")));
      File selectedFile = fileChooser.showOpenDialog(stage);
      if (selectedFile == null) {
        return false;
      }
      path = selectedFile.toPath();
    }
    openFile(FileWrapper.existFile(path));
    return true;
  }

  public boolean save() {
    return model.currentFile.get().fileOrNew.unify(p -> saveToFile(p), i -> saveAs(Optional.empty()));
  }

  public boolean saveAs(Optional<Path> data) {
    Path path;
    if (data.isPresent()) {
      path = data.get();
    } else {
      FileChooser fileChooser = new FileChooser();
      fileChooser.setTitle("Save");
      fileChooser.getExtensionFilters().add(new ExtensionFilter("Style Sheet", "*.css"));
      fileChooser.setInitialDirectory(recentService.getLatestFile().map(p -> p.getParent().toFile()).orElse(new File(".")));
      fileChooser.setInitialFileName(model.currentEditor.get().nameBinding().get());
      File selectedFile = fileChooser.showSaveDialog(stage);
      if (selectedFile == null) {
        return false;
      }
      path = selectedFile.toPath();
    }
    boolean result = saveToFile(path);
    if (result) {
      getActiveEditor().fileProperty().set(FileWrapper.existFile(path));
    } else {
      // TODO show save fail
    }
    return result;
  }

  public void revert() {
    model.currentEditor.get().reload();
  }

  private boolean canClose(CssEditor editor) {
    if (editor.modifiedProperty().get()) {
      ButtonType result = messageService.showConfirmCancelDialog(stage, "Save",
          String.format("File\n\t%s\nhas been modified. Save changes?", editor.fileProperty().get().toString()));
      if (result.equals(ButtonType.YES)) {
        return save();
      }
      return result.equals(ButtonType.NO);
    }
    return true;
  }

  private void close(CssEditor editor) {
    model.tabEntities.removeIf(t -> t.getEditor() == editor);
  }

  private boolean canExit() {
    if (options.openLast().getValue()) {
      return true;
    }
    return model.tabEntities.stream()
        .allMatch(tab -> {
          tab.getTabPane().getSelectionModel().select(tab);
          return canClose(tab.getEditor());
        });
  }

  public void exit() {
    if (options.openLast().getValue()) {
      uncheck(() -> FileUtil.createDirectory(LAST_FILE_PATH));
      uncheck(() -> Files.newDirectoryStream(LAST_FILE_PATH, "*.tmp").forEach(p -> uncheck(() -> Files.delete(p))));
      ListUtil.forEach(model.tabEntities, (te, i) -> {
        String nameString = te.getEditor().fileProperty().get().fileOrNew.unify(p -> p.toString(), n -> n.toString());
        String text = te.getEditor().modifiedProperty().get() ? te.getEditor().getText() : "";
        Path path = LAST_FILE_PATH.resolve(String.format("%s.tmp", i));
        uncheck(() -> Files.write(path, String.join("\n", nameString, text).getBytes(options.charset().getValue())));
      });
    }
    stage.close();
  }

  @FXML
  public void format() {
    model.currentEditor.get().format();
  }

  @FXML
  public void comment() {
    fire(editActions.comment());
  }

  @FXML
  public void option() {
    oc.open(stage);
  }

  @FXML
  public void help() {
    messageService.showMessageDialog(stage, "Help", "Send email to xuda1107@gmail.com for help.");
  }

  @FXML
  public void about() {
    // TODO About
  }

  @FXML
  private void onDragOver(DragEvent event) {
    Dragboard db = event.getDragboard();
    if (db.hasFiles()) {
      event.acceptTransferModes(TransferMode.COPY);
    } else {
      event.consume();
    }
  }

  @FXML
  private void onDragDrop(DragEvent event) {
    Dragboard db = event.getDragboard();
    if (db.hasFiles()) {
      event.setDropCompleted(true);
      for (File file : db.getFiles()) {
        if (file.getName().endsWith(".css")) {
          openFile(FileWrapper.existFile(file.toPath()));
        }
      }
    }
    event.consume();
  }

  private void openLastFile() throws IOException {
    if (options.openLast().getValue()) {
      FileUtil.createDirectory(LAST_FILE_PATH);
      Files.newDirectoryStream(LAST_FILE_PATH, "*.tmp").forEach(p -> uncheck(() -> {
        List<String> lines = Files.readAllLines(p, options.charset().getValue());
        if (lines.isEmpty()) {
          return;
        }
        String head = lines.get(0);
        CssEditorTab te = openFile(Try.to(() -> Integer.valueOf(head)).map(i -> FileWrapper.newFile(i))
            .getOrElse(() -> FileWrapper.existFile(Paths.get(head))));
        lines.stream()
            .skip(1)
            .reduce((a, b) -> String.join(System.lineSeparator(), a, b))
            .ifPresent(t -> {
              te.getEditor().replaceText(t);
              te.getEditor().getUndoManager().forgetHistory();
            });
      }));
    }
  }

  protected boolean saveToFile(Path file) {
    try {
      Files.write(file, model.currentEditor.get().getText().getBytes(options.charset().getValue()));
      saved();
      return true;
    } catch (UnsupportedOperationException e) {
      throw e;
    } catch (IOException e) {
      error("Save fail.");
      return false;
    }
  }

  private CssEditorTab openFile(FileWrapper file) {
    Optional<CssEditorTab> existTab = file.getExistFile().flatMap(f -> findExistTab(f));
    if (existTab.isPresent()) {
      tabPane.getSelectionModel().select(existTab.get());
      return existTab.get();
    }
    CssEditorTab tabEntity = model.newTab(file);
    tabEntity.setOnCloseRequest(consume(e -> fire(tabEntity.getEditor(), fileActions.close())));
    tabPane.getSelectionModel().select(tabEntity);
    return tabEntity;
  }

  private void saved() {
    model.currentEditor.get().modify().saved();
  }

  private Optional<CssEditorTab> findExistTab(Path file) {
    return model.tabEntities.stream()
        .filter(t -> t.getEditor().fileProperty().get().getExistFile().map(p -> Objects.equals(file, p)).orElse(false))
        .findFirst();
  }

  Optional<CssEditorTab> findEntity(Tab t) {
    return CacheUtil.get(MainFrameController.this, t);
  }

  @Override
  public ObjectBinding<@CheckNull CssEditor> activeEditorBinding() {
    return model.currentEditor;
  }

  @Override
  public Node eventNode() {
    return tabPane;
  }
}
