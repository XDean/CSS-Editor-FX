package xdean.css.editor.controller;

import static xdean.jex.util.lang.ExceptionUtil.uncatch;
import static xdean.jfxex.bean.BeanConvertUtil.toStringBinding;
import static xdean.jfxex.bean.BeanUtil.map;
import static xdean.jfxex.bean.BeanUtil.mapToBoolean;
import static xdean.jfxex.bean.BeanUtil.nestBooleanValue;
import static xdean.jfxex.bean.BeanUtil.nestValue;

import java.nio.file.Files;

import javax.inject.Inject;

import org.fxmisc.richtext.CodeArea;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.scene.control.Tab;
import lombok.experimental.FieldDefaults;
import xdean.css.editor.config.Options;
import xdean.css.editor.controller.manager.CodeAreaManager;
import xdean.css.editor.controller.manager.RecentFileManager;
import xdean.css.editor.domain.FileWrapper;
import xdean.css.editor.util.IntSequence;
import xdean.jex.util.cache.CacheUtil;
import xdean.jfx.spring.annotation.FxComponent;
import xdean.jfxex.bean.annotation.CheckNull;
import xdean.jfxex.bean.property.ObjectPropertyEX;

@FxComponent
public class MainFrameModel {

  @Inject
  RecentFileManager recentSupport;

  final IntSequence nameOrder = new IntSequence(1);

  final ObservableList<TabEntity> tabEntities = FXCollections.observableArrayList();
  final ObjectPropertyEX<@CheckNull TabEntity> currentTabEntity = new ObjectPropertyEX<>(this, "currentTabEntity");
  final ObjectBinding<@CheckNull FileWrapper> currentFile = nestValue(currentTabEntity, t -> t.file);
  final ObjectBinding<@CheckNull CodeAreaManager> currentManager = map(currentTabEntity, t -> t == null ? null : t.manager);
  final ObjectBinding<@CheckNull CodeArea> currentCodeArea = map(currentTabEntity, t -> t == null ? null : t.codeArea);
  final BooleanBinding currentModified = nestBooleanValue(currentManager, m -> m.modifiedProperty());

  @FieldDefaults(makeFinal = true)
  class TabEntity extends Tab {
    CodeAreaManager manager = new CodeAreaManager(new CodeArea());
    CodeArea codeArea = manager.getCodeArea();
    ObjectProperty<FileWrapper> file = new SimpleObjectProperty<>(this, "file", FileWrapper.newFile(0));
    ObjectBinding<String> name = map(file, f -> f.getFileName());
    FontAwesomeIconView icon = new FontAwesomeIconView();
    IntegerProperty order = new SimpleIntegerProperty(nameOrder.next());
    BooleanBinding isNew = mapToBoolean(file, f -> f.isNewFile());

    TabEntity() {
      // graphics
      setGraphic(icon);
      icon.setStyleClass("tab-icon");
      icon.setIcon(FontAwesomeIcon.SAVE);
      // Hold the object in cache to avoid gc
      StringBinding state = CacheUtil.cache(this, "state",
          () -> Bindings.when(currentTabEntity.isEqualTo(this))
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
          recentSupport.setLatestFile(n.getExistFile().get());
          if (o.isNewFile()) {
            releaseName();
          }
        }
      });

      reload();
      // CacheUtil.cache(this.mainFrameController, tab, () -> this);
    }

    void reload() {
      file.get().getExistFile().ifPresent(p -> uncatch(() -> {
        codeArea.replaceText(new String(Files.readAllBytes(p), Options.charset.get()));
        codeArea.moveTo(0);
        codeArea.getUndoManager().forgetHistory();
        manager.saved();
      }));
    }

    /**
     * Rename as "new i"
     */
    boolean renameNew(int i) {
      if (file.get() == null) {
        releaseName();
        if (nameOrder.use(i)) {
          order.set(i);
          return true;
        }
      }
      return false;
    }

    void releaseName() {
      nameOrder.release(order.get());
    }

    boolean isNew() {
      return file.get().isNewFile();
    }
  }
}
