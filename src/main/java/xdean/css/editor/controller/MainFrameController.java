package xdean.css.editor.controller;

import static xdean.jex.util.function.Predicates.not;
import static xdean.jex.util.task.TaskUtil.andFinal;
import static xdean.jfxex.bean.BeanConvertUtil.toDoubleBinding;
import static xdean.jfxex.bean.BeanUtil.mapToBoolean;
import static xdean.jfxex.bean.BeanUtil.nestBooleanValue;
import static xdean.jfxex.bean.BeanUtil.nestDoubleProp;
import static xdean.jfxex.bean.BeanUtil.nestDoubleValue;
import static xdean.jfxex.bean.BeanUtil.nestValue;
import static xdean.jfxex.bean.ListenerUtil.on;
import static xdean.jfxex.event.EventHandlers.consume;
import static xdean.jfxex.event.EventHandlers.consumeIf;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Named;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.DoubleBinding;
import javafx.fxml.FXML;
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
import xdean.css.editor.context.setting.HelpActions;
import xdean.css.editor.context.setting.PreferenceSettings;
import xdean.css.editor.control.CssEditor;
import xdean.css.editor.feature.CssEditorFeature;
import xdean.css.editor.model.FileWrapper;
import xdean.css.editor.service.ContextService;
import xdean.css.editor.service.DialogService;
import xdean.css.editor.service.RecentFileService;
import xdean.jex.log.Logable;
import xdean.jex.util.cache.CacheUtil;
import xdean.jfx.spring.FxInitializable;
import xdean.jfx.spring.annotation.FxController;
import xdean.jfx.spring.context.FxContext;

@FxController(fxml = "/fxml/MainFrame.fxml")
public class MainFrameController implements FxInitializable, Logable, CssEditorFeature {

  private @FXML ScrollBar verticalScrollBar, horizontalScrollBar;
  private @FXML TabPane tabPane;
  private @FXML VBox bottomExtraPane;
  private @Inject @Named(FxContext.FX_PRIMARY_STAGE) Stage stage;
  private @Inject MainFrameModel model;
  private @Inject RecentFileService recentService;
  private @Inject DialogService messageService;
  private @Inject ContextService contextService;
  private @Inject PreferenceSettings options;
  private @Inject EditActions editActions;
  private @Inject FileActions fileActions;
  private @Inject HelpActions helpActions;

  @Override
  public void initAfterFxSpringReady() {
    initBind();
  }

  private void initBind() {
    Bindings.bindContentBidirectional(contextService.editorList(), model.editors);
    contextService.activeEditorProperty().bindBidirectional(model.activeEditor);
    tabPane.getSelectionModel().selectedItemProperty()
        .addListener(on(not(null), n -> model.activeEditor.set(((CssEditorTab) n).getEditor())));
    model.activeEditor.addListener(on(not(null), n -> tabPane.getSelectionModel().select(CssEditorTab.get(n))));

    // disable
    BooleanBinding nullArea = model.activeEditor.isNull();
    editActions.format().disableProperty().bind(nullArea);
    editActions.comment().disableProperty().bind(nullArea);
    editActions.find().disableProperty().bind(nullArea);
    editActions.undo().disableProperty()
        .bind(nullArea.or(nestBooleanValue(model.activeEditor, c -> c.undoAvailableProperty()).not()));
    editActions.redo().disableProperty()
        .bind(nullArea.or(nestBooleanValue(model.activeEditor, c -> c.redoAvailableProperty()).not()));
    fileActions.close().disableProperty().bind(nullArea);
    fileActions.saveAs().disableProperty().bind(nullArea);
    BooleanBinding modified = nestBooleanValue(model.activeEditor, m -> m.modifiedProperty());
    fileActions.save().disableProperty().bind(fileActions.saveAs().disableProperty().or(modified.not()));
    fileActions.revert().disableProperty()
        .bind(mapToBoolean(nestValue(model.activeEditor, t -> t.fileProperty()), f -> f == null || f.isNewFile())
            .or(modified.not()));

    // scroll bar
    DoubleBinding editorTextHeight = nestDoubleValue(model.activeEditor, c -> c.totalHeightEstimateProperty());
    DoubleBinding editorHeight = nestDoubleValue(model.activeEditor, c -> c.heightProperty());
    verticalScrollBar.maxProperty().bind(editorTextHeight.subtract(editorHeight));
    verticalScrollBar.visibleAmountProperty().bind(
        Bindings.max(0, toDoubleBinding(verticalScrollBar.maxProperty())
            .multiply(editorHeight).divide(editorTextHeight)));
    verticalScrollBar.valueProperty()
        .bindBidirectional(nestDoubleProp(model.activeEditor, c -> c.estimatedScrollYProperty()).normalize());
    verticalScrollBar.visibleProperty().bind(model.activeEditor.isNotNull()
        .and(verticalScrollBar.maxProperty().greaterThan(verticalScrollBar.visibleAmountProperty())));
    verticalScrollBar.managedProperty().bind(Bindings.createBooleanBinding(
        () -> andFinal(() -> verticalScrollBar.isVisible(), v -> verticalScrollBar.getParent().layout()),
        verticalScrollBar.visibleProperty()));

    DoubleBinding editorTextWidth = nestDoubleValue(model.activeEditor, c -> c.totalWidthEstimateProperty());
    DoubleBinding editorWidth = nestDoubleValue(model.activeEditor, c -> c.widthProperty());
    horizontalScrollBar.maxProperty().bind(editorTextWidth.subtract(editorWidth));
    horizontalScrollBar.visibleAmountProperty().bind(
        Bindings.max(0, horizontalScrollBar.maxProperty()
            .multiply(editorWidth).divide(editorTextWidth)));
    horizontalScrollBar.valueProperty()
        .bindBidirectional(nestDoubleProp(model.activeEditor, c -> c.estimatedScrollXProperty()).normalize());
    horizontalScrollBar.visibleProperty().bind(options.wrapText().booleanProperty().not().and(model.activeEditor.isNotNull())
        .and(horizontalScrollBar.maxProperty().greaterThan(horizontalScrollBar.visibleAmountProperty())));
    horizontalScrollBar.managedProperty().bind(Bindings.createBooleanBinding(
        () -> andFinal(() -> horizontalScrollBar.isVisible(), v -> horizontalScrollBar.getParent().layout()),
        horizontalScrollBar.visibleProperty()));

    // tabList
    Bindings.bindContentBidirectional(tabPane.getTabs(), model.tabs);

    // events
    stage.addEventHandler(fileActions.newFile().getEventType(), e -> newFile());
    stage.addEventHandler(fileActions.open().getEventType(), e -> open(e.getData()));

    stage.addEventFilter(fileActions.exit().getEventType(), consumeIf(e -> !canExit()));
    stage.addEventHandler(fileActions.exit().getEventType(), e -> exit());

    stage.addEventHandler(helpActions.about().getEventType(), e -> about());
    stage.addEventHandler(helpActions.help().getEventType(), e -> help());
  }

  @Override
  public void bind(CssEditor editor) {
    editor.addEventHandler(fileActions.save().getEventType(), consumeIf(e -> save(editor)));
    editor.addEventHandler(fileActions.saveAs().getEventType(), consumeIf(e -> saveAs(editor, e.getData())));
    editor.addEventFilter(fileActions.close().getEventType(), consumeIf(e -> !canClose(editor)));
    editor.addEventHandler(fileActions.close().getEventType(), consume(e -> close(editor)));
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

  public boolean save(CssEditor editor) {
    return editor.fileProperty().get().fileOrNew.unify(p -> saveToFile(editor, p), i -> saveAs(editor, Optional.empty()));
  }

  public boolean saveAs(CssEditor editor, Optional<Path> data) {
    Path path;
    if (data.isPresent()) {
      path = data.get();
    } else {
      FileChooser fileChooser = new FileChooser();
      fileChooser.setTitle("Save");
      fileChooser.getExtensionFilters().add(new ExtensionFilter("Style Sheet", "*.css"));
      fileChooser.setInitialDirectory(recentService.getLatestFile().map(p -> p.getParent().toFile()).orElse(new File(".")));
      fileChooser.setInitialFileName(editor.nameBinding().get());
      File selectedFile = fileChooser.showSaveDialog(stage);
      if (selectedFile == null) {
        return false;
      }
      path = selectedFile.toPath();
    }
    boolean result = saveToFile(editor, path);
    if (result) {
      editor.fileProperty().set(FileWrapper.existFile(path));
    } else {
      // TODO show save fail
    }
    return result;
  }

  private boolean canClose(CssEditor editor) {
    if (editor.modifiedProperty().get()) {
      ButtonType result = messageService.showConfirmCancelDialog(stage, "Save",
          String.format("File\n\t%s\nhas been modified. Save changes?", editor.fileProperty().get().toString()));
      if (result.equals(ButtonType.YES)) {
        return save(editor);
      }
      return result.equals(ButtonType.NO);
    }
    return true;
  }

  private void close(CssEditor editor) {
    model.editors.remove(editor);
  }

  private boolean canExit() {
    return options.openLast().getValue() || model.editors.stream()
        .allMatch(editor -> {
          select(editor);
          return canClose(editor);
        });
  }

  public void exit() {
    stage.close();
  }

  public void about() {
    // TODO About
  }

  public void help() {
    messageService.showMessageDialog(stage, "Help", "Send email to xuda1107@gmail.com for help.");
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

  protected boolean saveToFile(CssEditor editor, Path file) {
    try {
      Files.write(file, editor.getText().getBytes(options.charset().getValue()));
      editor.modify().saved();
      return true;
    } catch (UnsupportedOperationException e) {
      throw e;
    } catch (IOException e) {
      error("Save fail.");
      return false;
    }
  }

  private CssEditor openFile(FileWrapper file) {
    Optional<CssEditor> existEditor = file.getExistFile().flatMap(f -> findExistTab(f));
    if (existEditor.isPresent()) {
      tabPane.getSelectionModel().select(CssEditorTab.get(existEditor.get()));
      return existEditor.get();
    }
    CssEditor editor = model.newTab(file);
    editor.reload();
    select(editor);
    return editor;
  }

  private Optional<CssEditor> findExistTab(Path file) {
    return model.editors.stream()
        .filter(e -> e.fileProperty().get().getExistFile().map(p -> Objects.equals(file, p)).orElse(false))
        .findFirst();
  }

  Optional<CssEditorTab> findEntity(Tab t) {
    return CacheUtil.get(MainFrameController.this, t);
  }

  private void select(CssEditor editor) {
    tabPane.getSelectionModel().select(CssEditorTab.get(editor));
  }
}
