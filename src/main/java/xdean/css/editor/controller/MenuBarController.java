package xdean.css.editor.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import xdean.jfx.spring.FxInitializable;
import xdean.jfx.spring.annotation.FxController;

@FxController(fxml = "/fxml/MenuBar.fxml")
public class MenuBarController implements FxInitializable {

  private @FXML Menu openRecentMenu;
  private @FXML Menu skinMenu;
  private @FXML MenuItem closeItem;
  private @FXML MenuItem saveItem;
  private @FXML MenuItem saveAsItem;
  private @FXML MenuItem revertItem;
  private @FXML MenuItem undoItem;
  private @FXML MenuItem redoItem;
  private @FXML MenuItem findItem;
  private @FXML MenuItem commentItem;
  private @FXML MenuItem formatItem;

  @Override
  public void initAfterFxSpringReady() {

  }

  @FXML
  public void newFile() {
  }

  @FXML
  public void open() {
  }

  @FXML
  public void clearRecent() {
  }

  @FXML
  public void close() {
  }

  @FXML
  public void save() {
  }

  @FXML
  public void saveAs() {
  }

  @FXML
  public void revert() {
  }

  @FXML
  public void exit() {
  }

  @FXML
  public void undo() {
  }

  @FXML
  public void redo() {
  }

  @FXML
  public void find() {
  }

  @FXML
  public void comment() {
  }

  @FXML
  public void format() {
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
