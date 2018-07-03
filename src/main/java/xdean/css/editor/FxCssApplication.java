package xdean.css.editor;

import static xdean.jfxex.bean.BeanUtil.map;
import static xdean.jfxex.event.EventHandlers.consume;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.stereotype.Component;

import javafx.beans.property.ObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import xdean.css.editor.context.setting.FileActions;
import xdean.css.editor.control.CssEditor;
import xdean.css.editor.controller.MainFrameController;
import xdean.css.editor.feature.CssAppFeature;
import xdean.css.editor.service.ContextService;
import xdean.css.editor.service.SkinService;
import xdean.jfx.spring.FxApplication;
import xdean.jfx.spring.FxmlResult;
import xdean.jfx.spring.context.FxContext;
import xdean.jfxex.bean.annotation.CheckNull;
import xdean.jfxex.bean.property.ObjectPropertyEX;

@Component
public class FxCssApplication implements FxApplication, ContextService {

  private @Inject FxmlResult<MainFrameController, Parent> mainFrame;
  private @Inject SkinService skinService;
  private @Inject FileActions fileActions;
  private @Inject List<CssAppFeature> features = Collections.emptyList();

  private final Stage stage;
  private final ObservableList<CssEditor> editors = FXCollections.observableArrayList();
  private final ObjectPropertyEX<@CheckNull CssEditor> activeEditor = new ObjectPropertyEX<CssEditor>(this, "editor")
      .in(editors, false);

  @Inject
  public FxCssApplication(@Named(FxContext.FX_PRIMARY_STAGE) Stage stage) {
    this.stage = stage;
    activeEditor.addListener((ob, o, n) -> {
      if (o != null) {
        o.activeProperty().set(false);
      }
      if (n != null) {
        n.activeProperty().set(true);
      }
    });
  }

  @Override
  public void start(Stage stage) throws Exception {
    Scene scene = skinService.bind(new Scene(mainFrame.getRoot()));
    scene.getStylesheets().add("/css/global.css");
    stage.setScene(scene);

    features.forEach(f -> f.bind(stage));

    stage.titleProperty()
        .bind(map(activeEditorProperty(), e -> (e == null ? "" : e.fileProperty().get().getFileName()) + " - CSS Editor FX"));
    stage.setOnCloseRequest(consume(e -> fire(fileActions.exit())));
    stage.setMaximized(true);
    stage.show();

    stage.getIcons().add(new Image(FxCssApplication.class.getClassLoader().getResourceAsStream("image/icon.jpg")));
  }

  @Override
  public ObservableList<CssEditor> editorList() {
    return editors;
  }

  @Override
  public ObjectProperty<@CheckNull CssEditor> activeEditorProperty() {
    return activeEditor;
  }

  @Override
  public Stage stage() {
    return stage;
  }
}
