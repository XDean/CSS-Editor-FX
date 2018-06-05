package xdean.css.editor.controller.service;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.stage.Window;

@Service
public class MessageService {

  @Inject
  SkinService skinService;

  public void showMessageDialog(Window window, String title, String message) {
    Dialog<ButtonType> dialog = skinService.createDialog();
    if (window != null) {
      dialog.initOwner(window);
    }
    dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK);
    dialog.setTitle(title);
    dialog.setContentText(message);
    dialog.showAndWait();
  }

  public boolean showConfirmDialog(Window window, String title, String message) {
    Dialog<ButtonType> dialog = skinService.createDialog();
    if (window != null) {
      dialog.initOwner(window);
    }
    dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
    dialog.setTitle(title);
    dialog.setContentText(message);
    return dialog.showAndWait().orElse(ButtonType.CANCEL).equals(ButtonType.OK);
  }

  /**
   * @param window
   * @param title
   * @param message
   * @return {@code ButtonType.YES, ButtonType.NO, ButtonType.CANCEL}
   */
  public ButtonType showConfirmCancelDialog(Window window, String title, String message) {
    Dialog<ButtonType> dialog = skinService.createDialog();
    if (window != null) {
      dialog.initOwner(window);
    }
    dialog.getDialogPane().getButtonTypes().addAll(ButtonType.YES, ButtonType.NO, ButtonType.CANCEL);
    dialog.setTitle(title);
    dialog.setContentText(message);
    return dialog.showAndWait().orElse(ButtonType.CANCEL);
  }
}