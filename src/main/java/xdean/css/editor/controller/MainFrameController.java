package xdean.css.editor.controller;

import static xdean.jex.util.task.TaskUtil.*;
import static xdean.jfx.ex.util.bean.BeanConvertUtil.*;
import static xdean.jfx.ex.util.bean.BeanUtil.*;

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
import java.util.function.Function;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
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
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import org.controlsfx.control.StatusBar;
import org.fxmisc.richtext.CodeArea;

import xdean.css.editor.Util;
import xdean.css.editor.config.ConfigKey;
import xdean.css.editor.config.Context;
import xdean.css.editor.config.Key;
import xdean.css.editor.config.Options;
import xdean.css.editor.controller.comp.SearchBar;
import xdean.css.editor.controller.manager.CodeAreaManager;
import xdean.css.editor.controller.manager.StatusBarManager;
import xdean.jex.config.Config;
import xdean.jex.extra.IntSequence;
import xdean.jex.util.cache.CacheUtil;
import xdean.jex.util.collection.ListUtil;
import xdean.jex.util.file.FileUtil;
import xdean.jfx.ex.support.RecentFileMenuSupport;
import xdean.jfx.ex.support.skin.SkinStyle;
import xdean.jfx.ex.util.bean.CollectionUtil;

import com.sun.javafx.binding.BidirectionalBinding;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;

@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MainFrameController implements Initializable {

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

  @SuppressWarnings("unused")
  StatusBarManager statusBarManager;
  Stage stage;
  RecentFileMenuSupport recentSupport;

  ObservableList<TabEntity> tabList;
  Path lastFilePath = Context.TEMP_PATH.resolve("last");

  IntSequence nameOrder = new IntSequence(1);
  SearchBar searchBar;

  @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
  private class TabEntity {
    Tab tab;
    CodeArea codeArea;
    CodeAreaManager manager;
    StringProperty name;
    ObjectProperty<File> file;
    FontAwesomeIconView icon;
    IntegerProperty order;

    TabEntity() {
      this.tab = new Tab();
      this.manager = new CodeAreaManager(new CodeArea());
      this.codeArea = manager.getCodeArea();
      this.file = new SimpleObjectProperty<>();
      this.name = new SimpleStringProperty();
      this.icon = new FontAwesomeIconView();
      this.order = new SimpleIntegerProperty(nameOrder.next());

      init();

      CacheUtil.cache(MainFrameController.this, tab, () -> this);
    }

    private void init() {
      // name
      name.bind(Bindings.when(file.isNull())
          .then(Bindings.createStringBinding(() -> "new" + order.get(), order))
          .otherwise(toStringBinding(map(file, f -> f.getName()))));

      // graphics
      tab.setGraphic(icon);
      icon.setStyleClass("tab-icon");
      icon.setIcon(FontAwesomeIcon.SAVE);
      // Hold the object in cache to avoid gc
      StringBinding state = CacheUtil.cache(this, "state",
          () -> Bindings.when(Bindings.equal(toObjectBinding(currentTabEntity()), this))
              .then(Bindings.when(manager.modifiedProperty())
                  .then("selected-modified")
                  .otherwise("selected"))
              .otherwise(Bindings.when(manager.modifiedProperty())
                  .then("modified")
                  .otherwise("")));
      state.addListener((ob, o, n) -> {
        uncatch(() -> icon.pseudoClassStateChanged(PseudoClass.getPseudoClass(o), false));
        uncatch(() -> icon.pseudoClassStateChanged(PseudoClass.getPseudoClass(n), true));
      });

      // recent
      file.addListener((ob, o, n) -> todoAll(
          () -> ifThat(n != null).todo(() -> recentSupport.setLastFile(n)),
          () -> ifThat(o == null && n != null).todo(() -> releaseName())
          ));
    }

    void loadFile() {
      loadFile(file.get());
    }

    void loadFile(File file) {
      uncatch(() -> {
        codeArea.replaceText(new String(Files.readAllBytes(file.toPath()), Options.charset.get()));
        codeArea.moveTo(0);
        codeArea.getUndoManager().forgetHistory();
        manager.saved();
      });
    }

    /**
     * Rename as "new i"
     * 
     * @param i
     * @return success or not
     */
    boolean renameNew(int i) {
      if (file.get() == null) {
        releaseName();
        if (nameOrder.use(i)) {
          order.set(i);
          return true;
        }
      }
      return false;
    }

    void releaseName() {
      nameOrder.release(order.get());
    }
  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    initField();
    initMenu();
    initComp();
    initBind();
    throwToReturn(() -> openLastFile()).ifPresent(e -> log.error("Load last closed file fail.", e));
  }

  private void initField() {
    tabList = FXCollections.observableArrayList();
    // codeAreaManager = new CodeAreaManager(codeArea);
    statusBarManager = new StatusBarManager(statusBar, currentCodeArea(), nestProp(currentManager(),
        m -> m.overrideProperty()));
    tabPane.getTabs().clear();// DELETE
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
    searchBar = new SearchBar(currentCodeArea());
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
    BooleanBinding nullArea = isNull(currentCodeArea());
    suggestItem.disableProperty().bind(nullArea);
    formatItem.disableProperty().bind(nullArea);
    commentItem.disableProperty().bind(nullArea);
    findItem.disableProperty().bind(nullArea);
    undoItem.disableProperty().bind(nullArea.or(not(getCodeAreaValue(c -> c.undoAvailableProperty()))));
    redoItem.disableProperty().bind(nullArea.or(not(getCodeAreaValue(c -> c.redoAvailableProperty()))));
    undoButton.disableProperty().bind(undoItem.disableProperty());
    redoButton.disableProperty().bind(redoItem.disableProperty());
    closeItem.disableProperty().bind(nullArea);
    saveAsItem.disableProperty().bind(nullArea);
    saveItem.disableProperty().bind(saveAsItem.disableProperty().or(not(modifiedProperty())));
    saveButton.disableProperty().bind(saveItem.disableProperty());
    revertItem.disableProperty().bind(isNull(currentFile())
        .or(not(modifiedProperty()))
        .or(Bindings.createBooleanBinding(() -> isNewFile(), currentFile())));

    // scroll bar
    DoubleBinding codeAreaTextHeight = toDoubleBinding(getCodeAreaValue(c -> c.totalHeightEstimateProperty()));
    DoubleBinding codeAreaHeight = toDoubleBinding(getCodeAreaValue(c -> c.heightProperty()));
    verticalScrollBar.maxProperty().bind(codeAreaTextHeight.subtract(codeAreaHeight));
    verticalScrollBar.visibleAmountProperty().bind(
        Bindings.max(0, toDoubleBinding(verticalScrollBar.maxProperty())
            .multiply(codeAreaHeight).divide(codeAreaTextHeight)));
    BidirectionalBinding.bindNumber(verticalScrollBar.valueProperty(),
        getCodeAreaProperty(c -> c.estimatedScrollYProperty()));
    verticalScrollBar.visibleProperty().bind(isNotNull(currentCodeArea())
        .and(verticalScrollBar.maxProperty().greaterThan(verticalScrollBar.visibleAmountProperty())));
    verticalScrollBar.managedProperty().bind(Bindings.createBooleanBinding(
        () -> andFinal(() -> verticalScrollBar.isVisible(), v -> verticalScrollBar.getParent().layout()),
        verticalScrollBar.visibleProperty()));

    DoubleBinding codeAreaTextWidth = toDoubleBinding(getCodeAreaValue(c -> c.totalWidthEstimateProperty()));
    DoubleBinding codeAreaWidth = toDoubleBinding(getCodeAreaValue(c -> c.widthProperty()));
    horizontalScrollBar.maxProperty().bind(codeAreaTextWidth.subtract(codeAreaWidth));
    horizontalScrollBar.visibleAmountProperty().bind(
        Bindings.max(0, horizontalScrollBar.maxProperty()
            .multiply(codeAreaWidth).divide(codeAreaTextWidth)));
    BidirectionalBinding.bindNumber(horizontalScrollBar.valueProperty(),
        getCodeAreaProperty(c -> c.estimatedScrollXProperty()));
    horizontalScrollBar.visibleProperty().bind(Options.wrapText.property().not().and(isNotNull(currentCodeArea()))
        .and(horizontalScrollBar.maxProperty().greaterThan(horizontalScrollBar.visibleAmountProperty())));
    horizontalScrollBar.managedProperty().bind(Bindings.createBooleanBinding(
        () -> andFinal(() -> horizontalScrollBar.isVisible(), v -> horizontalScrollBar.getParent().layout()),
        horizontalScrollBar.visibleProperty()));

    // tabList
    CollectionUtil.bind(tabList, tabPane.getTabs(),
        tabEntity -> tabEntity.tab,
        tab -> findEntity(tab).orElseThrow(() -> new RuntimeException("Tab not found " + tab)));
    tabList.addListener(new ListChangeListener<TabEntity>() {
      @Override
      public void onChanged(Change<? extends TabEntity> c) {
        while (c.next()) {
          if (c.wasRemoved()) {
            for (TabEntity te : c.getRemoved()) {
              te.releaseName();
            }
          }
        }
      }
    });
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
    if (isNewFile()) {
      return saveAs();
    } else {
      return saveToFile(currentFile().getValue());
    }
  }

  @FXML
  public boolean saveAs() {
    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle("Save");
    fileChooser.getExtensionFilters().add(new ExtensionFilter("Style Sheet", "*.css"));
    fileChooser.setInitialDirectory(recentSupport.getLastFile().getParentFile());
    fileChooser.setInitialFileName(currentTabEntity().getValue().name.get());
    File selectedFile = fileChooser.showSaveDialog(stage);
    if (selectedFile == null) {
      return false;
    } else {
      return andFinal(() -> saveToFile(selectedFile),
          b -> ifThat(b).todo(() -> currentTabEntity().getValue().file.setValue(selectedFile)));
    }
  }

  @FXML
  public void revert() {
    currentTabEntity().getValue().loadFile();
  }

  @FXML
  public void exit() {
    if (Options.openLastFile.get()) {
      uncheck(() -> FileUtil.createDirectory(lastFilePath));
      uncheck(() -> Files.newDirectoryStream(lastFilePath, "*.tmp").forEach(p -> uncheck(() -> Files.delete(p))));
      ListUtil.forEach(tabList, (te, i) -> {
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
    currentCodeArea().getValue().undo();
  }

  @FXML
  public void redo() {
    currentCodeArea().getValue().redo();
  }

  @FXML
  public void find() {
    searchBar.toggle();
  }

  @FXML
  public void suggest() {
    currentManager().getValue().suggest();
  }

  @FXML
  public void format() {
    currentManager().getValue().format();
  }

  @FXML
  public void comment() {
    currentManager().getValue().comment();
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

  protected boolean isNewFile() {
    return currentFile().getValue() == null;
  }

  protected boolean askToSaveAndShouldContinue() {
    if (isModified()) {
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
      Files.write(file.toPath(), currentCodeArea().getValue().getText().getBytes(Options.charset.get()));
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
      int index = getFileIndex(file);
      if (index != -1) {
        tabPane.getSelectionModel().select(index);
        return tabList.get(index);
      }
    }
    TabEntity tabEntity = new TabEntity();
    tabEntity.file.set(file);
    tabEntity.loadFile();

    Tab tab = tabEntity.tab;

    tab.setOnCloseRequest(e -> andFinal(() -> close(), () -> e.consume()));
    tab.setContent(tabEntity.codeArea);
    tab.textProperty().bind(tabEntity.name);

    tabList.add(tabEntity);
    tabPane.getSelectionModel().select(tab);

    return tabEntity;
  }

  public void setStage(Stage stage) {
    this.stage = stage;
    stage.setTitle("CSS Editor FX");
    currentTabEntity().addListener((ob, o, n) -> {
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

  private ObservableValue<Boolean> modifiedProperty() {
    return CacheUtil.cache(this, "modified", () -> nestValue(currentManager(), m -> m.modifiedProperty()));
  }

  private boolean isModified() {
    return tabList.isEmpty() == false && modifiedProperty().getValue();
  }

  private void saved() {
    currentManager().getValue().saved();
  }

  private int getFileIndex(File file) {
    for (int i = 0; i < tabList.size(); i++) {
      if (Objects.equals(tabList.get(i).file.get(), file)) {
        return i;
      }
    }
    return -1;
  }

  Optional<TabEntity> findEntity(Tab t) {
    return CacheUtil.get(MainFrameController.this, t);
  }

  private ObservableValue<TabEntity> currentTabEntity() {
    return CacheUtil.cache(this, "currentTabEntity", () -> {
      ObjectProperty<TabEntity> op = new SimpleObjectProperty<>();
      op.bind(map(tabPane.getSelectionModel().selectedIndexProperty(),
          i -> uncatch(() -> tabList.get(i.intValue()))));
      return op;
    });
  }

  private ObservableValue<File> currentFile() {
    return CacheUtil.cache(this, "currentFile", () -> nestValue(currentTabEntity(), t -> t.file));
  }

  private ObservableValue<CodeAreaManager> currentManager() {
    return CacheUtil.cache(this, "currentManager", () -> map(currentTabEntity(), t -> t.manager));
  }

  private ObservableValue<CodeArea> currentCodeArea() {
    return CacheUtil.cache(this, "currentCodeArea", () -> {
      ObjectProperty<CodeArea> op = new SimpleObjectProperty<CodeArea>();
      op.bind(map(currentManager(), m -> uncatch(() -> m.getCodeArea())));
      return op;
    });
  }

  private <T> Property<T> getCodeAreaProperty(Function<CodeArea, Property<T>> func) {
    return nestProp(currentCodeArea(), c -> func.apply(c));
  }

  private <T> ObservableValue<T> getCodeAreaValue(Function<CodeArea, ObservableValue<T>> func) {
    return nestValue(currentCodeArea(), c -> func.apply(c));
  }
}
