package xdean.css.editor.controller;

import static xdean.jex.util.lang.ExceptionUtil.uncatch;
import static xdean.jex.util.task.TaskUtil.todoAll;
import static xdean.jfxex.bean.BeanConvertUtil.toStringBinding;
import static xdean.jfxex.bean.BeanUtil.map;
import static xdean.jfxex.bean.BeanUtil.nestBooleanValue;
import static xdean.jfxex.bean.BeanUtil.nestValue;

import java.nio.file.Files;
import java.nio.file.Path;

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
import javafx.css.PseudoClass;
import javafx.scene.control.Tab;
import lombok.experimental.FieldDefaults;
import xdean.css.editor.config.Options;
import xdean.css.editor.controller.manager.CodeAreaManager;
import xdean.css.editor.controller.manager.RecentFileManager;
import xdean.css.editor.util.IntSequence;
import xdean.jex.util.cache.CacheUtil;
import xdean.jex.util.task.If;
import xdean.jfx.spring.annotation.FxComponent;
import xdean.jfxex.bean.property.ListPropertyEX;
import xdean.jfxex.bean.property.ObjectPropertyEX;

@FxComponent
public class MainFrameModel {

  @Inject
  RecentFileManager recentSupport;

  final IntSequence nameOrder = new IntSequence(1);

  final ListPropertyEX<TabEntity> tabEntities = new ListPropertyEX<>(this, "tabEntities");
  final ObjectPropertyEX<TabEntity> currentTabEntity = new ObjectPropertyEX<>(this, "currentTabEntity");
  final ObjectBinding<Path> currentFile = nestValue(currentTabEntity, t -> t.file);
  final ObjectBinding<CodeAreaManager> currentManager = map(currentTabEntity, t -> t.manager);
  final ObjectBinding<CodeArea> currentCodeArea = map(currentTabEntity, t -> t.codeArea);
  final BooleanBinding currentModified = nestBooleanValue(currentManager, m -> m.modifiedProperty());

  final TabEntity EMPTY = new TabEntity();
  
  public MainFrameModel() {
    currentTabEntity.defaultForNull(EMPTY);
  }

  @FieldDefaults(makeFinal = true)
  class TabEntity {
    Tab tab = new Tab();
    CodeAreaManager manager = new CodeAreaManager(new CodeArea());
    CodeArea codeArea = manager.getCodeArea();
    StringProperty name = new SimpleStringProperty();
    ObjectProperty<Path> file = new SimpleObjectProperty<>();
    FontAwesomeIconView icon = new FontAwesomeIconView();
    IntegerProperty order = new SimpleIntegerProperty(nameOrder.next());
    BooleanBinding isNew = file.isNull();

    TabEntity() {
      // name
      name.bind(Bindings.when(file.isNull())
          .then(Bindings.createStringBinding(() -> "new" + order.get(), order))
          .otherwise(toStringBinding(map(file, f -> f.getFileName().toString()))));

      // graphics
      tab.setGraphic(icon);
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
      file.addListener((ob, o, n) -> todoAll(
          () -> If.that(n != null).todo(() -> recentSupport.setLatestFile(n)),
          () -> If.that(o == null && n != null).todo(() -> releaseName())));

      tab.setUserData(this);
      // CacheUtil.cache(this.mainFrameController, tab, () -> this);
    }

    void loadFile() {
      loadFile(file.get());
    }

    void loadFile(Path file) {
      uncatch(() -> {
        codeArea.replaceText(new String(Files.readAllBytes(file), Options.charset.get()));
        codeArea.moveTo(0);
        codeArea.getUndoManager().forgetHistory();
        manager.saved();
      });
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
      return file.get() == null;
    }
  }
}
