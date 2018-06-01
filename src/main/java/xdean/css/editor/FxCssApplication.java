package xdean.css.editor;

import javax.inject.Inject;

import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import xdean.css.editor.controller.MainFrameController;
import xdean.jfx.spring.FxApplication;

public class FxCssApplication implements FxApplication {
  
  @Inject
  MainFrameController mainFrame;
  
  @Override
  public void start(Stage stage) throws Exception {
    mainFrame.setStage(stage);

    Scene scene = Util.createScene(mainFrame.getRoot());

    stage.setScene(scene);
    stage.setMaximized(true);
    stage.show();

    stage.getIcons().add(new Image(FxCssApplication.class.getClassLoader().getResourceAsStream("image/icon.png")));
  }
}
