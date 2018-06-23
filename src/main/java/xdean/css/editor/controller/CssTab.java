package xdean.css.editor.controller;

import static xdean.jex.util.lang.ExceptionUtil.uncatch;
import static xdean.jfxex.bean.BeanUtil.map;
import static xdean.jfxex.bean.BeanUtil.mapToBoolean;

import java.nio.file.Files;

import javax.inject.Inject;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.css.PseudoClass;
import javafx.scene.control.Tab;
import xdean.css.editor.context.setting.PreferenceSettings;
import xdean.css.editor.model.FileWrapper;
import xdean.css.editor.service.RecentFileService;
import xdean.jex.util.cache.CacheUtil;
import xdean.jfx.spring.FxInitializable;
import xdean.jfx.spring.annotation.FxComponent;

@FxComponent
public class CssTab extends Tab implements FxInitializable {
  @Inject
  RecentFileService recentFileService;

  @Inject
  CssEditorController manager;

  @Inject
  PreferenceSettings options;

  ObjectProperty<FileWrapper> file = new SimpleObjectProperty<>(this, "file", FileWrapper.newFile(0));
  ObjectBinding<String> name = map(file, f -> f.getFileName());
  BooleanBinding isNew = mapToBoolean(file, f -> f.isNewFile());
  BooleanProperty active = new SimpleBooleanProperty(this, "active");

  @Override
  public void initAfterFxSpringReady() {
    textProperty().bind(name);
    // graphics
    FontAwesomeIconView icon = new FontAwesomeIconView();
    setGraphic(icon);
    icon.setStyleClass("tab-icon");
    icon.setIcon(FontAwesomeIcon.SAVE);
    // Hold the object in cache to avoid gc
    StringBinding state = CacheUtil.cache(this, "state",
        () -> Bindings.when(active)
            .then(Bindings.when(manager.modifiedProperty())
                .then("selected-modified")
                .otherwise("selected"))
            .otherwise(Bindings.when(manager.modifiedProperty())
                .then("modified")
                .otherwise("")));
    state.addListener((ob, o, n) -> {
      uncatch(() -> icon.pseudoClassStateChanged(PseudoClass.getPseudoClass(o), false));
      uncatch(() -> icon.pseudoClassStateChanged(PseudoClass.getPseudoClass(n), true));
    });

    // recent
    file.addListener((ob, o, n) -> {
      if (n.isExistFile()) {
        recentFileService.setLatestFile(n.getExistFile().get());
        reload();
      }
    });

    setContent(manager.editor);
  }

  public void reload() {
    file.get().getExistFile().ifPresent(p -> uncatch(() -> {
      manager.editor.replaceText(new String(Files.readAllBytes(p), options.charset().getValue()));
      manager.editor.moveTo(0);
      manager.editor.getUndoManager().forgetHistory();
      manager.modify.saved();
    }));
  }

  public boolean isNew() {
    return file.get().isNewFile();
  }
}