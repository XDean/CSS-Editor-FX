package xdean.css.editor.controller;

import static xdean.jex.util.lang.ExceptionUtil.throwToReturn;
import static xdean.jex.util.lang.ExceptionUtil.uncheck;
import static xdean.jex.util.task.TaskUtil.andFinal;
import static xdean.jfxex.bean.BeanConvertUtil.toDoubleBinding;
import static xdean.jfxex.bean.BeanUtil.nestBooleanValue;
import static xdean.jfxex.bean.BeanUtil.nestDoubleProp;
import static xdean.jfxex.bean.BeanUtil.nestDoubleValue;
import static xdean.jfxex.bean.BeanUtil.nestProp;
import static xdean.jfxex.bean.ListenerUtil.list;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;

import javax.inject.Inject;

import org.controlsfx.control.StatusBar;
import org.springframework.context.annotation.Bean;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.DoubleBinding;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
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
import lombok.extern.slf4j.Slf4j;
import xdean.css.editor.config.Config;
import xdean.css.editor.config.ConfigKey;
import xdean.css.editor.config.Context;
import xdean.css.editor.config.Key;
import xdean.css.editor.config.Options;
import xdean.css.editor.controller.MainFrameModel.TabEntity;
import xdean.css.editor.controller.comp.SearchBar;
import xdean.css.editor.controller.manager.StatusBarManager;
import xdean.css.editor.util.Util;
import xdean.jex.util.cache.CacheUtil;
import xdean.jex.util.collection.ListUtil;
import xdean.jex.util.file.FileUtil;
import xdean.jex.util.task.If;
import xdean.jfx.spring.FxGetRoot;
import xdean.jfx.spring.annotation.FxController;
import xdean.jfxex.bean.CollectionUtil;
import xdean.jfxex.support.RecentFileMenuSupport;
import xdean.jfxex.support.skin.SkinStyle;

@Slf4j
@FxController("/fxml/MainFrame.fxml")
public class MainFrameController implements Initializable, FxGetRoot<VBox> {

  @FXML
  MenuItem suggestItem, formatItem, undoItem, redoItem, commentItem,
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
  StatusBar statusBar;

  StatusBarManager statusBarManager;
  Stage stage;
  RecentFileMenuSupport recentSupport;

  Path lastFilePath = Context.TEMP_PATH.resolve("last");

  SearchBar searchBar;

  @Inject
  MainFrameModel model;

  final TabEntity EMPTY = model.new TabEntity(this);

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    initField();
    initMenu();
    initComp();
    initBind();
    throwToReturn(() -> openLastFile()).ifPresent(e -> log.error("Load last closed file fail.", e));
  }

  private void initField() {
    // codeAreaManager = new CodeAreaManager(codeArea);
    statusBarManager = new StatusBarManager(statusBar, model.currentCodeArea,
        nestProp(model.currentManager, m -> m.overrideProperty()));
    tabPane.getTabs().clear();// DELETE
  }

  @Bean
  public RecentFileMenuSupport recent() {
    return new RecentFileMenuSupport(openRecentMenu) {
      @Override
      public List<String> load() {
        return Arrays.asList(Config.getProperty(ConfigKey.RECENT_LOCATIONS, "").split(","));
      }

      @Override
      public void save(List<String> s) {
        Config.setProperty(ConfigKey.RECENT_LOCATIONS, String.join(", ", s));
      }
    };
  }

  private void initMenu() {
    recentSupport = new RecentFileMenuSupport(openRecentMenu) {
      @Override
      public List<String> load() {
        return Arrays.asList(Config.getProperty(ConfigKey.RECENT_LOCATIONS, "").split(","));
      }

      @Override
      public void save(List<String> s) {
        Config.setProperty(ConfigKey.RECENT_LOCATIONS, String.join(", ", s));
      }
    };
    recentSupport.setOnAction(this::openFile);

    ToggleGroup group = new ToggleGroup();
    for (SkinStyle style : Context.SKIN.getSkinList()) {
      RadioMenuItem item = new RadioMenuItem(style.getName());
      item.setToggleGroup(group);
      item.setOnAction(e -> Context.SKIN.changeSkin(style));
      if (Context.SKIN.currentSkin() == style) {
        item.setSelected(true);
      }
      skinMenu.getItems().add(item);
    }
  }

  private void initComp() {
    searchBar = new SearchBar(model.currentCodeArea);
    bottomExtraPane.getChildren().add(searchBar);
  }

  private void initBind() {
    // shortcut
    suggestItem.acceleratorProperty().bind(Key.SUGGEST.property());
    formatItem.acceleratorProperty().bind(Key.FORMAT.property());
    commentItem.acceleratorProperty().bind(Key.COMMENT.property());
    findItem.acceleratorProperty().bind(Key.FIND.property());
    closeItem.acceleratorProperty().bind(Key.CLOSE.property());

    // disable
    BooleanBinding nullArea = model.currentCodeArea.isNull();
    suggestItem.disableProperty().bind(nullArea);
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
    CollectionUtil.bind(model.tabEntities, tabPane.getTabs(),
        tabEntity -> tabEntity.tab,
        tab -> findEntity(tab).orElseThrow(() -> new RuntimeException("Tab not found " + tab)));
    model.tabEntities.addListener(list(b -> b.onRemoved(t -> t.releaseName())));
  }

  @FXML
  public void newFile() {
    openFile(null);
  }

  @FXML
  public void open() {
    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle("Open");
    fileChooser.getExtensionFilters().add(new ExtensionFilter("Style Sheet", "*.css"));
    fileChooser.setInitialDirectory(recentSupport.getLastFile().getParentFile());
    File selectedFile = fileChooser.showOpenDialog(stage);
    if (selectedFile != null) {
      openFile(selectedFile);
    }
  }

  @FXML
  public void clearRecent() {
    recentSupport.clear();
  }

  @FXML
  public void close() {
    if (askToSaveAndShouldContinue()) {
      tabPane.getTabs().remove(tabPane.getSelectionModel().getSelectedIndex());
    }
  }

  @FXML
  public boolean save() {
    if (model.currentTabEntity.get().isNew()) {
      return saveAs();
    } else {
      return saveToFile(model.currentFile.get());
    }
  }

  @FXML
  public boolean saveAs() {
    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle("Save");
    fileChooser.getExtensionFilters().add(new ExtensionFilter("Style Sheet", "*.css"));
    fileChooser.setInitialDirectory(recentSupport.getLastFile().getParentFile());
    fileChooser.setInitialFileName(model.currentTabEntity.get().name.get());
    File selectedFile = fileChooser.showSaveDialog(stage);
    if (selectedFile == null) {
      return false;
    } else {
      return andFinal(() -> saveToFile(selectedFile),
          b -> If.that(b).todo(() -> model.currentTabEntity.get().file.setValue(selectedFile)));
    }
  }

  @FXML
  public void revert() {
    model.currentTabEntity.get().loadFile();
  }

  @FXML
  public void exit() {
    if (Options.openLastFile.get()) {
      uncheck(() -> FileUtil.createDirectory(lastFilePath));
      uncheck(() -> Files.newDirectoryStream(lastFilePath, "*.tmp").forEach(p -> uncheck(() -> Files.delete(p))));
      ListUtil.forEach(model.tabEntities, (te, i) -> {
        String nameString = te.file.get() == null ? Integer.toString(te.order.get()) : te.file.get().toString();
        String text = te.manager.isModified() ? te.codeArea.getText() : "";
        Path path = lastFilePath.resolve(String.format("%s.tmp", i));
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
  public void suggest() {
    model.currentManager.get().suggest();
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
    OptionsController.show(tabPane.getScene().getWindow());
  }

  @FXML
  public void help() {
    Util.showMessageDialog(stage, "Help", "Send email to xuda1107@gmail.com for help.");
  }

  @FXML
  public void about() {
    // TODO About
  }

  private void openLastFile() throws IOException {
    if (Options.openLastFile.get()) {
      FileUtil.createDirectory(lastFilePath);
      Files.newDirectoryStream(lastFilePath, "*.tmp").forEach(p -> uncheck(() -> {
        TabEntity te = openFile(null);
        List<String> lines = Files.readAllLines(p, Options.charset.get());
        Optional.ofNullable(lines.get(0)).ifPresent(s -> throwToReturn(() -> Integer.valueOf(s))
            .ifLeft(i -> te.renameNew(i))
            .ifRight(e -> te.file.set(new File(s))));
        Optional<String> text = lines.stream()
            .skip(1)
            .reduce((a, b) -> String.join(System.lineSeparator(), a, b));
        if (text.isPresent()) {
          te.codeArea.replaceText(text.get());
          te.codeArea.getUndoManager().forgetHistory();
        } else {
          te.loadFile();
        }
      }));
    }
  }

  protected boolean askToSaveAndShouldContinue() {
    if (model.currentModified.get()) {
      ButtonType result = Util.showConfirmCancelDialog(stage, "Save", "This file has been modified. Save changes?");
      if (result.equals(ButtonType.YES)) {
        return save();
      }
      return result.equals(ButtonType.NO);
    }
    return true;
  }

  protected boolean saveToFile(File file) {
    try {
      Files.write(file.toPath(), model.currentCodeArea.get().getText().getBytes(Options.charset.get()));
      saved();
      return true;
    } catch (UnsupportedOperationException e) {
      throw e;
    } catch (IOException e) {
      log.error("Save fail.");
      return false;
    }
  }

  private TabEntity openFile(File file) {
    if (file != null) {
      Optional<TabEntity> existTab = getExistTab(file);
      if (existTab.isPresent()) {
        tabPane.getSelectionModel().select(existTab.get().tab);
        return existTab.get();
      }
    }
    TabEntity tabEntity = model.new TabEntity(this);
    tabEntity.file.set(file);
    tabEntity.loadFile();

    Tab tab = tabEntity.tab;

    tab.setOnCloseRequest(e -> andFinal(() -> close(), () -> e.consume()));
    tab.setContent(tabEntity.codeArea);
    tab.textProperty().bind(tabEntity.name);

    model.tabEntities.add(tabEntity);
    tabPane.getSelectionModel().select(tab);

    return tabEntity;
  }

  public void setStage(Stage stage) {
    this.stage = stage;
    stage.setTitle("CSS Editor FX");
    model.currentTabEntity.addListener((ob, o, n) -> {
      String title = n == null ? null : (n.file.get() == null ? n.name.get() : n.file.get().getAbsolutePath());
      if (title == null || title.trim().length() == 0) {
        stage.setTitle("CSS Editor FX");
      } else {
        stage.setTitle(String.join(" - ", "CSS Editor FX", title));
      }
    });
    stage.setOnCloseRequest(e -> {
      e.consume();
      exit();
    });
  }

  private void saved() {
    model.currentManager.get().saved();
  }

  private Optional<TabEntity> getExistTab(File file) {
    return model.tabEntities.stream()
        .filter(t -> Objects.equals(file, t.file.get()))
        .findFirst();
  }

  Optional<TabEntity> findEntity(Tab t) {
    return CacheUtil.get(MainFrameController.this, t);
  }
}
