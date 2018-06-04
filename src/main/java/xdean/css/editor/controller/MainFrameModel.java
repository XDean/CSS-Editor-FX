package xdean.css.editor.controller;

import static xdean.jex.util.lang.ExceptionUtil.uncatch;
import static xdean.jex.util.task.TaskUtil.todoAll;
import static xdean.jfxex.bean.BeanConvertUtil.toObjectBinding;
import static xdean.jfxex.bean.BeanConvertUtil.toStringBinding;
import static xdean.jfxex.bean.BeanUtil.map;
import static xdean.jfxex.bean.BeanUtil.nestBooleanValue;
import static xdean.jfxex.bean.BeanUtil.nestValue;

import java.io.File;
import java.nio.file.Files;

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
import xdean.css.editor.util.IntSequence;
import xdean.jex.util.cache.CacheUtil;
import xdean.jex.util.task.If;
import xdean.jfxex.bean.annotation.CheckNull;
import xdean.jfxex.bean.property.ObjectPropertyEX;

public class MainFrameModel {

  final ObjectPropertyEX<@CheckNull TabEntity> currentTabEntity = new ObjectPropertyEX<>(this, "currentTabEntity");
  final ObjectBinding<File> currentFile = nestValue(currentTabEntity, t -> t.file);
  final ObjectBinding<CodeAreaManager> currentManager = map(currentTabEntity, t -> t.manager);
  final ObjectBinding<CodeArea> currentCodeArea = map(currentTabEntity, t -> t.codeArea);
  final BooleanBinding currentModified = nestBooleanValue(currentManager, m -> m.modifiedProperty());

  IntSequence nameOrder = new IntSequence(1);

  @FieldDefaults(makeFinal = true)
  class TabEntity {
    MainFrameController mainFrameController;
    Tab tab = new Tab();
    CodeAreaManager manager = new CodeAreaManager(new CodeArea());
    CodeArea codeArea = manager.getCodeArea();
    StringProperty name = new SimpleStringProperty();
    ObjectProperty<File> file = new SimpleObjectProperty<>();
    FontAwesomeIconView icon = new FontAwesomeIconView();
    IntegerProperty order = new SimpleIntegerProperty(nameOrder.next());
    BooleanBinding isNew = file.isNull();

    TabEntity(MainFrameController mainFrameController) {
      this.mainFrameController = mainFrameController;
      init();
      tab.setUserData(this);
      // CacheUtil.cache(this.mainFrameController, tab, () -> this);
    }

    private void init() {
      // name
      name.bind(Bindings.when(file.isNull())
          .then(Bindings.createStringBinding(() -> "new" + order.get(), order))
          .otherwise(toStringBinding(map(file, f -> f.getName()))));

      // graphics
      tab.setGraphic(icon);
      icon.setStyleClass("tab-icon");
      icon.setIcon(FontAwesomeIcon.SAVE);
      // Hold the object in cache to avoid gc
      StringBinding state = CacheUtil.cache(this, "state",
          () -> Bindings.when(Bindings.equal(toObjectBinding(currentTabEntity), this))
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
          () -> If.that(n != null).todo(() -> this.mainFrameController.recentSupport.setLastFile(n)),
          () -> If.that(o == null && n != null).todo(() -> releaseName())));
    }

    void loadFile() {
      loadFile(file.get());
    }

    void loadFile(File file) {
      uncatch(() -> {
        codeArea.replaceText(new String(Files.readAllBytes(file.toPath()), Options.charset.get()));
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
