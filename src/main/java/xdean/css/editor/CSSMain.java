package xdean.css.editor;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.util.Pair;
import xdean.css.editor.config.Context;
import xdean.css.editor.controller.MainFrameController;

public class CSSMain extends Application {

  public static void main(String[] args) {
    Context.loadClass();
    launch(args);
  }

  @Override
  public void start(Stage stage) throws Exception {

    Pair<MainFrameController, Pane> pair = Util.renderFxml(MainFrameController.class);
    Pane root = pair.getValue();
    pair.getKey().setStage(stage);

    Scene scene = Util.createScene(root);

    stage.setScene(scene);
    stage.setMaximized(true);
    stage.show();

    stage.getIcons().add(new Image(CSSMain.class.getClassLoader().getResourceAsStream("image/icon.png")));
  }
}
