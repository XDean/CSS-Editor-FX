package xdean.css.editor;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.stereotype.Component;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import xdean.css.editor.control.CssEditor;
import xdean.css.editor.controller.MainFrameController;
import xdean.css.editor.feature.CssAppFeature;
import xdean.css.editor.service.ContextService;
import xdean.css.editor.service.NodeFactoryService;
import xdean.jfx.spring.FxApplication;
import xdean.jfx.spring.FxmlResult;
import xdean.jfx.spring.context.FxContext;
import xdean.jfxex.bean.annotation.CheckNull;

@Component
public class FxCssApplication implements FxApplication, ContextService {

  private final ObjectProperty<@CheckNull CssEditor> editor = new SimpleObjectProperty<>(this, "editor");

  private @Inject FxmlResult<MainFrameController, Parent> mainFrame;

  private @Inject NodeFactoryService nodeFactory;

  private @Inject List<CssAppFeature> features = Collections.emptyList();

  private final Stage stage;

  @Inject
  public FxCssApplication(@Named(FxContext.FX_PRIMARY_STAGE) Stage stage) {
    this.stage = stage;
  }

  @Override
  public void start(Stage stage) throws Exception {
    Scene scene = nodeFactory.createScene(mainFrame.getRoot());
    stage.setScene(scene);

    features.forEach(f -> f.bind(stage));

    stage.setMaximized(true);
    stage.show();

    stage.getIcons().add(new Image(FxCssApplication.class.getClassLoader().getResourceAsStream("image/icon.jpg")));
  }

  @Override
  public ObjectProperty<@CheckNull CssEditor> activeEditorProperty() {
    return editor;
  }

  @Override
  public Stage stage() {
    return stage;
  }
}
