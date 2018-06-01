package xdean.css.editor;

import java.io.IOException;
import java.net.URL;
import java.util.function.Consumer;

import com.sun.javafx.tk.Toolkit;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Pair;
import lombok.extern.slf4j.Slf4j;
import xdean.css.editor.config.Context;
import xdean.css.editor.config.Options;

@Slf4j
public class Util {
  /************************* JavaFX ***********************************/

  public static Stage createStage() {
    Stage stage = new Stage();
    stage.getIcons().add(new Image(Util.class.getClassLoader().getResourceAsStream("icon/icon.png")));
    return stage;
  }

  public static Scene createScene(Parent root) {
    Scene scene = new Scene(root);
    Context.SKIN.bind(scene);
    return scene;
  }

  public static <T> Dialog<T> createDialog() {
    Dialog<T> dialog = new Dialog<>();
    // ((Stage) dialog.getDialogPane().getScene().getWindow()).getIcons().add(
    // new
    // Image(Util.class.getClassLoader().getResourceAsStream("icon/icon.png")));
    Context.SKIN.bind(dialog);
    return dialog;
  }

  public static Tab getSimpleTab(String text, Node graphic, Node content) {
    Tab tab = new Tab();
    if (text != null) {
      tab.setText(text);
    }
    if (graphic != null) {
      tab.setGraphic(graphic);
    }
    if (content != null) {
      tab.setContent(content);
    }
    return tab;
  }

  public static MenuItem getSimpleMenuItem(String text, Node graphic, Consumer<ActionEvent> eventHandler) {
    MenuItem item = new MenuItem();
    if (text != null) {
      item.setText(text);
    }
    if (graphic != null) {
      item.setGraphic(graphic);
    }
    if (eventHandler != null) {
      item.setOnAction(eventHandler::accept);
    }
    return item;
  }

  public static void showMessageDialog(Window window, String title, String message) {
    Dialog<ButtonType> dialog = createDialog();
    if (window != null) {
      dialog.initOwner(window);
    }
    dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK);
    dialog.setTitle(title);
    dialog.setContentText(message);
    dialog.showAndWait();
  }

  public static boolean showConfirmDialog(Window window, String title, String message) {
    Dialog<ButtonType> dialog = createDialog();
    if (window != null) {
      dialog.initOwner(window);
    }
    dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
    dialog.setTitle(title);
    dialog.setContentText(message);
    return dialog.showAndWait().orElse(ButtonType.CANCEL).equals(ButtonType.OK);
  }

  /**
   * 
   * @param window
   * @param title
   * @param message
   * @return {@code ButtonType.YES, ButtonType.NO, ButtonType.CANCEL}
   */
  public static ButtonType showConfirmCancelDialog(Window window, String title, String message) {
    Dialog<ButtonType> dialog = createDialog();
    if (window != null) {
      dialog.initOwner(window);
    }
    dialog.getDialogPane().getButtonTypes().addAll(ButtonType.YES, ButtonType.NO, ButtonType.CANCEL);
    dialog.setTitle(title);
    dialog.setContentText(message);
    return dialog.showAndWait().orElse(ButtonType.CANCEL);
  }

  public static void printAllWithId(Node n, int space) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < space; i++) {
      sb.append("-");
    }
    sb.append(String.format("id:%s  %s", n.getId(), n));
    log.debug(sb.toString());
    if (n instanceof Parent) {
      ((Parent) n).getChildrenUnmodifiable().forEach(node -> {
        printAllWithId(node, space + 1);
      });
    }
  }

  public static URL getFxmlResource(String fileName) throws IOException {
    if (!fileName.endsWith(".fxml")) {
      fileName += ".fxml";
    }
    return Util.class.getClassLoader().getResource("fxml/" + fileName);
  }

  public static <C extends Initializable, P> Pair<C, P> renderFxml(Class<C> controllerClass) throws IOException {
    String clzName = controllerClass.getSimpleName();
    String suffix = "Controller";
    if (!clzName.endsWith(suffix)) {
      throw new IOException("Class must named like \"xxxController\".");
    } else {
      return renderFxml(clzName.substring(0, clzName.length() - suffix.length()));
    }
  }

  public static <C extends Initializable, P> Pair<C, P> renderFxml(String fileName) throws IOException {
    FXMLLoader loader = new FXMLLoader(Util.getFxmlResource(fileName));
    P filterPane = loader.load();
    C controller = loader.getController();
    return new Pair<>(controller, filterPane);
  }

  public static void textAreaFitHeight(TextArea textArea) {
    Text text = new Text();
    textArea.textProperty().addListener((observable, o, n) -> {
      text.setWrappingWidth(textArea.getWidth());
      text.setText(n);
      textArea.setPrefHeight(text.getLayoutBounds().getHeight() + 9);
    });
  }

  public static double getTextSize(String text) {
    return Toolkit.getToolkit().getFontLoader()
        .computeStringWidth(text, Font.font(Options.fontFamily.get(), Options.fontSize.get()));
  }
}
