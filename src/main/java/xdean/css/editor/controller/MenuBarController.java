package xdean.css.editor.controller;

import javax.inject.Inject;

import javafx.fxml.FXML;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import xdean.css.editor.context.setting.EditActions;
import xdean.css.editor.context.setting.FileActions;
import xdean.css.editor.context.setting.model.CssEditorKeyEventOption;
import xdean.css.editor.service.ContextService;
import xdean.jfx.spring.FxInitializable;
import xdean.jfx.spring.annotation.FxController;

@FxController(fxml = "/fxml/MenuBar.fxml")
public class MenuBarController implements FxInitializable {

  private @FXML Menu openRecentMenu;
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

  private @Inject ContextService contextService;
  private @Inject EditActions editActions;
  private @Inject FileActions fileActions;

  @Override
  public void initAfterFxSpringReady() {
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

  private void bind(MenuItem item, CssEditorKeyEventOption key) {
    item.acceleratorProperty().bind(key.valueProperty());
    item.disableProperty().bind(key.disableProperty());
  }

  @FXML
  public void newFile() {
    onAction(fileActions.newFile());
  }

  @FXML
  public void open() {
    onAction(fileActions.open());
  }

  @FXML
  public void clearRecent() {
  }

  @FXML
  public void close() {
    onAction(fileActions.close());
  }

  @FXML
  public void save() {
    onAction(fileActions.save());
  }

  @FXML
  public void saveAs() {
    onAction(fileActions.saveAs());
  }

  @FXML
  public void revert() {
    onAction(fileActions.revert());
  }

  @FXML
  public void exit() {
  }

  @FXML
  public void undo() {
    onAction(editActions.undo());
  }

  @FXML
  public void redo() {
    onAction(editActions.redo());
  }

  @FXML
  public void find() {
    onAction(editActions.find());
  }

  @FXML
  public void comment() {
    onAction(editActions.comment());
  }

  @FXML
  public void format() {
    onAction(editActions.format());
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

  private void onAction(CssEditorKeyEventOption keyOption) {
    contextService.fire(keyOption);
  }
}
