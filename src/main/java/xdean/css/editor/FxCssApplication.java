package xdean.css.editor;

import javax.inject.Inject;

import org.springframework.stereotype.Component;

import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import xdean.css.editor.controller.MainFrameController;
import xdean.css.editor.controller.service.SkinService;
import xdean.jfx.spring.FxApplication;

@Component
public class FxCssApplication implements FxApplication {

  @Inject
  MainFrameController mainFrame;

  @Inject
  SkinService skinManager;

  @Override
  public void start(Stage stage) throws Exception {
    Scene scene = skinManager.createScene(mainFrame.getRoot());

    stage.setScene(scene);
    stage.setMaximized(true);
    stage.show();

    stage.getIcons().add(new Image(FxCssApplication.class.getClassLoader().getResourceAsStream("image/icon.png")));
  }
}
