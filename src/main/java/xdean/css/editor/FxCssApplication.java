package xdean.css.editor;

import javax.inject.Inject;

import org.springframework.stereotype.Component;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import xdean.css.editor.controller.MainFrameController;
import xdean.css.editor.service.NodeFactoryService;
import xdean.jfx.spring.FxApplication;
import xdean.jfx.spring.FxmlResult;

@Component
public class FxCssApplication implements FxApplication {

  @Inject
  FxmlResult<MainFrameController, Parent> mainFrame;

  @Inject
  NodeFactoryService nodeFactory;

  @Override
  public void start(Stage stage) throws Exception {
    Scene scene = nodeFactory.createScene(mainFrame.getRoot());

    stage.setScene(scene);
    stage.setMaximized(true);
    stage.show();

    stage.getIcons().add(new Image(FxCssApplication.class.getClassLoader().getResourceAsStream("image/icon.jpg")));
  }
}
