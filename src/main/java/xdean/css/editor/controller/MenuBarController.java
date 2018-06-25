package xdean.css.editor.controller;

import javax.inject.Inject;

import javafx.beans.binding.Bindings;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.ToggleGroup;
import xdean.css.editor.context.setting.EditActions;
import xdean.css.editor.context.setting.FileActions;
import xdean.css.editor.context.setting.model.CssEditorKeyOption;
import xdean.css.editor.service.ContextService;
import xdean.css.editor.service.SkinService;
import xdean.jfx.spring.FxInitializable;
import xdean.jfx.spring.annotation.FxController;
import xdean.jfxex.support.RecentFileMenuSupport;
import xdean.jfxex.support.skin.SkinStyle;

@FxController(fxml = "/fxml/MenuBar.fxml")
public class MenuBarController implements FxInitializable {

  private @FXML Menu recentMenu;
  private @FXML Menu skinMenu;
  private @FXML MenuItem newItem;
  private @FXML MenuItem openItem;
  private @FXML MenuItem closeItem;
  private @FXML MenuItem saveItem;
  private @FXML MenuItem saveAsItem;
  private @FXML MenuItem revertItem;

  private @FXML MenuItem undoItem;
  private @FXML MenuItem redoItem;
  private @FXML MenuItem findItem;
  private @FXML MenuItem commentItem;
  private @FXML MenuItem formatItem;

  private @Inject RecentFileMenuSupport recentSupport;
  private @Inject SkinService skinManager;
  private @Inject ContextService contextService;
  private @Inject EditActions editActions;
  private @Inject FileActions fileActions;

  @Override
  public void initAfterFxSpringReady() {
    initRecentMenu();
    initSkinMenu();

    bind(newItem, fileActions.newFile());
    bind(openItem, fileActions.open());
    bind(closeItem, fileActions.close());
    bind(saveItem, fileActions.save());
    bind(saveAsItem, fileActions.saveAs());
    bind(revertItem, fileActions.revert());

    bind(undoItem, editActions.undo());
    bind(redoItem, editActions.redo());
    // bind(suggestItem, editActions.suggest());
    bind(findItem, editActions.find());
    bind(commentItem, editActions.comment());
    bind(formatItem, editActions.format());
  }

  private void initRecentMenu() {
    ObservableList<MenuItem> items = recentSupport.toMenuItems();
    Bindings.bindContent(recentMenu.getItems(), items);
  }

  private void initSkinMenu() {
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

  private void bind(MenuItem item, CssEditorKeyOption<?> key) {
    item.acceleratorProperty().bind(key.valueProperty());
    item.disableProperty().bind(key.disableProperty());
    item.setOnAction(e -> contextService.fire(key));
  }

  @FXML
  public void exit() {
  }

  @FXML
  public void option() {
  }

  @FXML
  public void about() {
  }

  @FXML
  public void help() {
  }
}
