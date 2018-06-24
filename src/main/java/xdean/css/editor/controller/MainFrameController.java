package xdean.css.editor.controller;

import static xdean.css.editor.context.Context.LAST_FILE_PATH;
import static xdean.jex.util.lang.ExceptionUtil.uncheck;
import static xdean.jex.util.task.TaskUtil.andFinal;
import static xdean.jfxex.bean.BeanConvertUtil.toDoubleBinding;
import static xdean.jfxex.bean.BeanUtil.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.DoubleBinding;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.ToggleGroup;
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
import xdean.css.editor.model.FileWrapper;
import xdean.css.editor.service.ContextService;
import xdean.css.editor.service.DialogService;
import xdean.css.editor.service.SkinService;
import xdean.jex.extra.tryto.Try;
import xdean.jex.log.Logable;
import xdean.jex.util.cache.CacheUtil;
import xdean.jex.util.collection.ListUtil;
import xdean.jex.util.file.FileUtil;
import xdean.jex.util.task.If;
import xdean.jfx.spring.FxInitializable;
import xdean.jfx.spring.annotation.FxController;
import xdean.jfx.spring.context.FxContext;
import xdean.jfxex.support.RecentFileMenuSupport;
import xdean.jfxex.support.skin.SkinStyle;

@Scope(BeanDefinition.SCOPE_SINGLETON)
@FxController(fxml = "/fxml/MainFrame.fxml")
public class MainFrameController implements FxInitializable, Logable, ContextService {

  @FXML
  MenuItem formatItem, undoItem, redoItem, commentItem,
      closeItem, saveItem, saveAsItem, revertItem, findItem;
  @FXML
  Button newButton, openButton, saveButton, undoButton, redoButton;

  @FXML
  Menu openRecentMenu, skinMenu;

  @FXML
  ScrollBar verticalScrollBar, horizontalScrollBar;

  @FXML
  TabPane tabPane;

  @FXML
  VBox bottomExtraPane;

  @FXML
  StatusBarController statusBarController;

  @FXML
  SearchBarController searchBarController;

  @Inject
  @Named(FxContext.FX_PRIMARY_STAGE)
  Stage stage;

  @Inject
  MainFrameModel model;

  @Inject
  RecentFileMenuSupport recentSupport;

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
    initMenu();
    initBind();
    Try.to(() -> openLastFile()).onException(e -> error("Load last closed file fail.", e));

    stage.titleProperty().bind(map(model.currentFile, f -> (f == null ? "" : f.getFileName()) + " - CSS Editor FX"));
    stage.setOnCloseRequest(e -> {
      e.consume();
      exit();
    });
  }

  private void initField() {
    recentSupport.bind(openRecentMenu, (Consumer<Path>) f -> openFile(FileWrapper.existFile(f)));
    searchBarController.editorProperty().bind(model.currentEditor);
    statusBarController.override.bindBidirectional(nestBooleanProp(model.currentEditor, m -> m.overrideProperty()));
    statusBarController.area.bind(model.currentEditor);
  }

  private void initMenu() {
    ToggleGroup group = new ToggleGroup();
    for (SkinStyle style : skinManager.getSkinList()) {
      RadioMenuItem item = new RadioMenuItem(style.getName());
      item.setToggleGroup(group);
      item.setOnAction(e -> skinManager.changeSkin(style));
      if (skinManager.currentSkin() == style) {
        item.setSelected(true);
      }
      skinMenu.getItems().add(item);
    }
  }

  private void initBind() {
    // shortcut
    formatItem.acceleratorProperty().bind(editActions.format().valueProperty());
    commentItem.acceleratorProperty().bind(editActions.comment().valueProperty());
    findItem.acceleratorProperty().bind(editActions.find().valueProperty());
    closeItem.acceleratorProperty().bind(fileActions.close().valueProperty());

    // disable
    BooleanBinding nullArea = model.currentEditor.isNull();
    formatItem.disableProperty().bind(nullArea);
    commentItem.disableProperty().bind(nullArea);
    findItem.disableProperty().bind(nullArea);
    undoItem.disableProperty().bind(nullArea.or(nestBooleanValue(model.currentEditor, c -> c.undoAvailableProperty()).not()));
    redoItem.disableProperty().bind(nullArea.or(nestBooleanValue(model.currentEditor, c -> c.redoAvailableProperty()).not()));
    undoButton.disableProperty().bind(undoItem.disableProperty());
    redoButton.disableProperty().bind(redoItem.disableProperty());
    closeItem.disableProperty().bind(nullArea);
    saveAsItem.disableProperty().bind(nullArea);
    saveItem.disableProperty().bind(saveAsItem.disableProperty().or(model.currentModified.not()));
    saveButton.disableProperty().bind(saveItem.disableProperty());
    revertItem.disableProperty().bind(model.currentFile.isNull()
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
  }

  @FXML
  public void newFile() {
    openFile(FileWrapper.newFile(model.nextNewOrder()));
  }

  @FXML
  public void open() {
    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle("Open");
    fileChooser.getExtensionFilters().add(new ExtensionFilter("Style Sheet", "*.css"));
    fileChooser.setInitialDirectory(recentSupport.getLatestFile().map(p -> p.getParent().toFile()).orElse(new File(".")));
    File selectedFile = fileChooser.showOpenDialog(stage);
    if (selectedFile != null) {
      openFile(FileWrapper.existFile(selectedFile.toPath()));
    }
  }

  @FXML
  public void clearRecent() {
    recentSupport.clear();
  }

  @FXML
  public void close() {
    if (askToSaveAndShouldContinue()) {
      model.currentTabEntity.getSafe().ifPresent(model.tabEntities::remove);
    }
  }

  @FXML
  public boolean save() {
    return model.currentFile.get().fileOrNew.unify(p -> saveToFile(p), i -> saveAs());
  }

  @FXML
  public boolean saveAs() {
    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle("Save");
    fileChooser.getExtensionFilters().add(new ExtensionFilter("Style Sheet", "*.css"));
    fileChooser.setInitialDirectory(recentSupport.getLatestFile().map(p -> p.getParent().toFile()).orElse(new File(".")));
    fileChooser.setInitialFileName(model.currentEditor.get().nameBinding().get());
    File selectedFile = fileChooser.showSaveDialog(stage);
    if (selectedFile == null) {
      return false;
    } else {
      Path path = selectedFile.toPath();
      return andFinal(() -> saveToFile(path),
          b -> If.that(b).todo(() -> model.currentEditor.get().fileProperty().set(FileWrapper.existFile(path))));
    }
  }

  @FXML
  public void revert() {
    model.currentEditor.get().reload();
  }

  @FXML
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
      stage.close();
    } else if (askToSaveAndShouldContinue()) {
      stage.close();
    }
  }

  @FXML
  public void undo() {
    model.currentEditor.get().undo();
  }

  @FXML
  public void redo() {
    model.currentEditor.get().redo();
  }

  @FXML
  public void find() {
    searchBarController.toggle();
  }

  @FXML
  public void format() {
    model.currentEditor.get().format();
  }

  @FXML
  public void comment() {
    editActions.comment().fire(activeEditor());
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

  protected boolean askToSaveAndShouldContinue() {
    if (model.currentModified.get()) {
      ButtonType result = messageService.showConfirmCancelDialog(stage, "Save", "This file has been modified. Save changes?");
      if (result.equals(ButtonType.YES)) {
        return save();
      }
      return result.equals(ButtonType.NO);
    }
    return true;
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
    tabEntity.setOnCloseRequest(e -> andFinal(() -> close(), () -> e.consume()));
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
  public Optional<CssEditor> activeEditor() {
    return Optional.ofNullable(model.currentEditor.get());
  }
}
