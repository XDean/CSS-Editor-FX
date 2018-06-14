package xdean.css.editor.controller;

import static xdean.jfxex.bean.BeanUtil.map;
import static xdean.jfxex.bean.BeanUtil.nestBooleanValue;
import static xdean.jfxex.bean.BeanUtil.nestValue;

import javax.inject.Inject;
import javax.inject.Provider;

import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.ObjectBinding;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import xdean.css.editor.control.CssCodeArea;
import xdean.css.editor.domain.FileWrapper;
import xdean.jfx.spring.annotation.FxComponent;
import xdean.jfxex.bean.annotation.CheckNull;
import xdean.jfxex.bean.property.ObjectPropertyEX;

@FxComponent
public class MainFrameModel {

  @Inject
  Provider<CssTab> tabFactory;

  final ObservableList<CssTab> tabEntities = FXCollections.observableArrayList();
  final ObjectPropertyEX<@CheckNull CssTab> currentTabEntity = new ObjectPropertyEX<>(this, "currentTabEntity");
  final ObjectBinding<@CheckNull FileWrapper> currentFile = nestValue(currentTabEntity, t -> t.file);
  final ObjectBinding<@CheckNull CssCodeAreaController> currentManager = map(currentTabEntity, t -> t == null ? null : t.manager);
  final ObjectBinding<@CheckNull CssCodeArea> currentCodeArea = map(currentManager, m -> m == null ? null : m.codeArea);
  final BooleanBinding currentModified = nestBooleanValue(currentManager, m -> m.modifiedProperty());

  public CssTab newTab(FileWrapper file) {
    CssTab te = tabFactory.get();
    te.active.bind(currentTabEntity.isEqualTo(te));
    te.file.set(file);
    tabEntities.add(te);
    return te;
  }

  public int nextNewOrder() {
    for (int i = 1;; i++) {
      int ii = i;
      if (tabEntities.stream()
          .map(t -> t.file.get())
          .filter(f -> f.isNewFile())
          .map(f -> f.getNewOrder().get())
          .allMatch(n -> n != ii)) {
        return i;
      }
    }
  }
}
