package xdean.css.editor.controller;

import javax.inject.Inject;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import xdean.css.editor.context.setting.EditActions;
import xdean.css.editor.context.setting.FileActions;
import xdean.css.editor.context.setting.model.CssEditorKeyOption;
import xdean.css.editor.service.ContextService;
import xdean.jfx.spring.FxInitializable;
import xdean.jfx.spring.annotation.FxController;

@FxController(fxml = "/fxml/ToolBar.fxml")
public class ToolBarController implements FxInitializable {

  private @FXML Button newButton;
  private @FXML Button openButton;
  private @FXML Button saveButton;
  private @FXML Button undoButton;
  private @FXML Button redoButton;

  private @Inject ContextService contextService;
  private @Inject FileActions fileActions;
  private @Inject EditActions editActions;

  @Override
  public void initAfterFxSpringReady() {
    bind(newButton, fileActions.newFile());
    bind(openButton, fileActions.open());
    bind(saveButton, fileActions.save());
    bind(undoButton, editActions.undo());
    bind(redoButton, editActions.redo());
  }

  private void bind(Button button, CssEditorKeyOption<?> key) {
    button.disableProperty().bind(key.disableProperty());
    button.setOnAction(e -> contextService.fire(key));
  }
}
