package xdean.css.editor.service;

import static xdean.jex.util.lang.ExceptionUtil.getStackTraceString;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Window;

@Service
public class DialogService {

  @Inject
  NodeFactoryService nodeFactory;

  public void showError(String title, Throwable throwable) {
    Alert alert = new Alert(AlertType.ERROR);
    alert.setTitle(title);
    alert.setHeaderText("Error happened");

    Label label = new Label("The exception stacktrace was:");

    TextArea textArea = new TextArea(getStackTraceString(throwable));
    textArea.setEditable(false);
    textArea.setWrapText(true);

    textArea.setMaxWidth(Double.MAX_VALUE);
    textArea.setMaxHeight(Double.MAX_VALUE);
    GridPane.setVgrow(textArea, Priority.ALWAYS);
    GridPane.setHgrow(textArea, Priority.ALWAYS);

    GridPane expContent = new GridPane();
    expContent.setMaxWidth(Double.MAX_VALUE);
    expContent.add(label, 0, 0);
    expContent.add(textArea, 0, 1);
    alert.getDialogPane().setExpandableContent(expContent);
    alert.showAndWait();
  }

  public void showMessageDialog(Window window, String title, String message) {
    Dialog<ButtonType> dialog = nodeFactory.createDialog();
    if (window != null) {
      dialog.initOwner(window);
    }
    dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK);
    dialog.setTitle(title);
    dialog.setContentText(message);
    dialog.showAndWait();
  }

  public boolean showConfirmDialog(Window window, String title, String message) {
    Dialog<ButtonType> dialog = nodeFactory.createDialog();
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
    Dialog<ButtonType> dialog = nodeFactory.createDialog();
    if (window != null) {
      dialog.initOwner(window);
    }
    dialog.getDialogPane().getButtonTypes().addAll(ButtonType.YES, ButtonType.NO, ButtonType.CANCEL);
    dialog.setTitle(title);
    dialog.setContentText(message);
    return dialog.showAndWait().orElse(ButtonType.CANCEL);
  }
}
