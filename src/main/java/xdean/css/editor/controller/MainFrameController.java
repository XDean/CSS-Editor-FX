package xdean.css.editor.controller;

import static xdean.css.editor.config.Context.LAST_FILE_PATH;
import static xdean.jex.util.lang.ExceptionUtil.uncheck;
import static xdean.jex.util.task.TaskUtil.andFinal;
import static xdean.jfxex.bean.BeanConvertUtil.toDoubleBinding;
import static xdean.jfxex.bean.BeanUtil.map;
import static xdean.jfxex.bean.BeanUtil.nestBooleanProp;
import static xdean.jfxex.bean.BeanUtil.nestBooleanValue;
import static xdean.jfxex.bean.BeanUtil.nestDoubleProp;
import static xdean.jfxex.bean.BeanUtil.nestDoubleValue;

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

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.DoubleBinding;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import xdean.css.editor.config.Key;
import xdean.css.editor.config.Options;
import xdean.css.editor.domain.FileWrapper;
import xdean.css.editor.service.MessageService;
import xdean.css.editor.service.SkinService;
import xdean.jex.extra.tryto.Try;
import xdean.jex.log.Logable;
import xdean.jex.util.cache.CacheUtil;
import xdean.jex.util.collection.ListUtil;
import xdean.jex.util.file.FileUtil;
import xdean.jex.util.task.If;
import xdean.jfx.spring.FxGetRoot;
import xdean.jfx.spring.FxInitializable;
import xdean.jfx.spring.annotation.FxController;
import xdean.jfx.spring.starter.FxContext;
import xdean.jfxex.support.RecentFileMenuSupport;
import xdean.jfxex.support.skin.SkinStyle;

@FxController(fxml = "/fxml/MainFrame.fxml")
public class MainFrameController implements FxInitializable, FxGetRoot<VBox>, Logable {

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

  @Inject
  SearchBarController searchBar;

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
  MessageService messageService;

  @Override
  public void initAfterFxSpringReady() {
    initField();
    initMenu();
    initComp();
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
    searchBar.codeArea.bind(model.currentCodeArea);
    statusBarController.override.bindBidirectional(nestBooleanProp(model.currentManager, m -> m.overrideProperty()));
    statusBarController.area.bind(model.currentCodeArea);
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

  private void initComp() {
    bottomExtraPane.getChildren().add(searchBar.getRoot());
  }

  private void initBind() {
    // shortcut
    formatItem.acceleratorProperty().bind(Key.FORMAT.property());
    commentItem.acceleratorProperty().bind(Key.COMMENT.property());
    findItem.acceleratorProperty().bind(Key.FIND.property());
    closeItem.acceleratorProperty().bind(Key.CLOSE.property());

    // disable
    BooleanBinding nullArea = model.currentCodeArea.isNull();
    formatItem.disableProperty().bind(nullArea);
    commentItem.disableProperty().bind(nullArea);
    findItem.disableProperty().bind(nullArea);
    undoItem.disableProperty().bind(nullArea.or(nestBooleanValue(model.currentCodeArea, c -> c.undoAvailableProperty()).not()));
    redoItem.disableProperty().bind(nullArea.or(nestBooleanValue(model.currentCodeArea, c -> c.redoAvailableProperty()).not()));
    undoButton.disableProperty().bind(undoItem.disableProperty());
    redoButton.disableProperty().bind(redoItem.disableProperty());
    closeItem.disableProperty().bind(nullArea);
    saveAsItem.disableProperty().bind(nullArea);
    saveItem.disableProperty().bind(saveAsItem.disableProperty().or(model.currentModified.not()));
    saveButton.disableProperty().bind(saveItem.disableProperty());
    revertItem.disableProperty().bind(model.currentFile.isNull()
        .or(model.currentModified.not())
        .or(nestBooleanValue(model.currentTabEntity, t -> t.isNew)));

    // scroll bar
    DoubleBinding codeAreaTextHeight = nestDoubleValue(model.currentCodeArea, c -> c.totalHeightEstimateProperty());
    DoubleBinding codeAreaHeight = nestDoubleValue(model.currentCodeArea, c -> c.heightProperty());
    verticalScrollBar.maxProperty().bind(codeAreaTextHeight.subtract(codeAreaHeight));
    verticalScrollBar.visibleAmountProperty().bind(
        Bindings.max(0, toDoubleBinding(verticalScrollBar.maxProperty())
            .multiply(codeAreaHeight).divide(codeAreaTextHeight)));
    verticalScrollBar.valueProperty()
        .bindBidirectional(nestDoubleProp(model.currentCodeArea, c -> c.estimatedScrollYProperty()).normalize());
    verticalScrollBar.visibleProperty().bind(model.currentCodeArea.isNotNull()
        .and(verticalScrollBar.maxProperty().greaterThan(verticalScrollBar.visibleAmountProperty())));
    verticalScrollBar.managedProperty().bind(Bindings.createBooleanBinding(
        () -> andFinal(() -> verticalScrollBar.isVisible(), v -> verticalScrollBar.getParent().layout()),
        verticalScrollBar.visibleProperty()));

    DoubleBinding codeAreaTextWidth = nestDoubleValue(model.currentCodeArea, c -> c.totalWidthEstimateProperty());
    DoubleBinding codeAreaWidth = nestDoubleValue(model.currentCodeArea, c -> c.widthProperty());
    horizontalScrollBar.maxProperty().bind(codeAreaTextWidth.subtract(codeAreaWidth));
    horizontalScrollBar.visibleAmountProperty().bind(
        Bindings.max(0, horizontalScrollBar.maxProperty()
            .multiply(codeAreaWidth).divide(codeAreaTextWidth)));
    horizontalScrollBar.valueProperty()
        .bindBidirectional(nestDoubleProp(model.currentCodeArea, c -> c.estimatedScrollXProperty()).normalize());
    horizontalScrollBar.visibleProperty().bind(Options.wrapText.property().not().and(model.currentCodeArea.isNotNull())
        .and(horizontalScrollBar.maxProperty().greaterThan(horizontalScrollBar.visibleAmountProperty())));
    horizontalScrollBar.managedProperty().bind(Bindings.createBooleanBinding(
        () -> andFinal(() -> horizontalScrollBar.isVisible(), v -> horizontalScrollBar.getParent().layout()),
        horizontalScrollBar.visibleProperty()));

    // tabList
    Bindings.bindContent(tabPane.getTabs(), model.tabEntities);
    tabPane.getSelectionModel().selectedItemProperty().addListener((ob, o, n) -> model.currentTabEntity.set((CssTab) n));
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
    fileChooser.setInitialFileName(model.currentTabEntity.get().name.get());
    File selectedFile = fileChooser.showSaveDialog(stage);
    if (selectedFile == null) {
      return false;
    } else {
      Path path = selectedFile.toPath();
      return andFinal(() -> saveToFile(path),
          b -> If.that(b).todo(() -> model.currentTabEntity.get().file.set(FileWrapper.existFile(path))));
    }
  }

  @FXML
  public void revert() {
    model.currentTabEntity.get().reload();
  }

  @FXML
  public void exit() {
    if (Options.openLastFile.get()) {
      uncheck(() -> FileUtil.createDirectory(LAST_FILE_PATH));
      uncheck(() -> Files.newDirectoryStream(LAST_FILE_PATH, "*.tmp").forEach(p -> uncheck(() -> Files.delete(p))));
      ListUtil.forEach(model.tabEntities, (te, i) -> {
        String nameString = te.file.get().fileOrNew.unify(p -> p.toString(), n -> n.toString());
        String text = te.manager.modify.isModified() ? te.manager.codeArea.getText() : "";
        Path path = LAST_FILE_PATH.resolve(String.format("%s.tmp", i));
        uncheck(() -> Files.write(path, String.join("\n", nameString, text).getBytes(Options.charset.get())));
      });
      stage.close();
    } else if (askToSaveAndShouldContinue()) {
      stage.close();
    }
  }

  @FXML
  public void undo() {
    model.currentCodeArea.get().undo();
  }

  @FXML
  public void redo() {
    model.currentCodeArea.get().redo();
  }

  @FXML
  public void find() {
    searchBar.toggle();
  }

  @FXML
  public void format() {
    model.currentManager.get().format();
  }

  @FXML
  public void comment() {
    model.currentManager.get().comment();
  }

  @FXML
  public void option() {
    Dialog<Void> dialog = new Dialog<>();
    dialog.initOwner(stage);
    dialog.setDialogPane(oc.getRoot());
    dialog.show();
  }

  @FXML
  public void help() {
    messageService.showMessageDialog(stage, "Help", "Send email to xuda1107@gmail.com for help.");
  }

  @FXML
  public void about() {
    // TODO About
  }

  private void openLastFile() throws IOException {
    if (Options.openLastFile.get()) {
      FileUtil.createDirectory(LAST_FILE_PATH);
      Files.newDirectoryStream(LAST_FILE_PATH, "*.tmp").forEach(p -> uncheck(() -> {
        List<String> lines = Files.readAllLines(p, Options.charset.get());
        if (lines.isEmpty()) {
          return;
        }
        String head = lines.get(0);
        CssTab te = openFile(Try.to(() -> Integer.valueOf(head)).map(i -> FileWrapper.newFile(i))
            .getOrElse(() -> FileWrapper.existFile(Paths.get(head))));
        lines.stream()
            .skip(1)
            .reduce((a, b) -> String.join(System.lineSeparator(), a, b))
            .ifPresent(t -> {
              te.manager.codeArea.replaceText(t);
              te.manager.codeArea.getUndoManager().forgetHistory();
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
      Files.write(file, model.currentCodeArea.get().getText().getBytes(Options.charset.get()));
      saved();
      return true;
    } catch (UnsupportedOperationException e) {
      throw e;
    } catch (IOException e) {
      error("Save fail.");
      return false;
    }
  }

  private CssTab openFile(FileWrapper file) {
    Optional<CssTab> existTab = file.getExistFile().flatMap(f -> findExistTab(f));
    if (existTab.isPresent()) {
      tabPane.getSelectionModel().select(existTab.get());
      return existTab.get();
    }
    CssTab tabEntity = model.newTab(file);
    tabEntity.setOnCloseRequest(e -> andFinal(() -> close(), () -> e.consume()));
    tabPane.getSelectionModel().select(tabEntity);
    return tabEntity;
  }

  private void saved() {
    model.currentManager.get().modify.saved();
  }

  private Optional<CssTab> findExistTab(Path file) {
    return model.tabEntities.stream()
        .filter(t -> t.file.get().getExistFile().map(p -> Objects.equals(file, p)).orElse(false))
        .findFirst();
  }

  Optional<CssTab> findEntity(Tab t) {
    return CacheUtil.get(MainFrameController.this, t);
  }
}
