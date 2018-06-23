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
import xdean.css.editor.control.CssEditor;
import xdean.css.editor.model.FileWrapper;
import xdean.jfx.spring.annotation.FxComponent;
import xdean.jfxex.bean.annotation.CheckNull;
import xdean.jfxex.bean.property.ObjectPropertyEX;

@FxComponent
public class MainFrameModel {

  private @Inject Provider<CssEditorTab> tabFactory;

  final ObservableList<CssEditorTab> tabEntities = FXCollections.observableArrayList();
  final ObjectPropertyEX<@CheckNull CssEditorTab> currentTabEntity = new ObjectPropertyEX<>(this, "currentTabEntity");
  final ObjectBinding<@CheckNull CssEditor> currentEditor = map(currentTabEntity, t -> t == null ? null : t.getEditor());
  final ObjectBinding<@CheckNull FileWrapper> currentFile = nestValue(currentEditor, t -> t.fileProperty());
  final BooleanBinding currentModified = nestBooleanValue(currentEditor, m -> m.modifiedProperty());

  public CssEditorTab newTab(FileWrapper file) {
    CssEditorTab te = tabFactory.get();
    te.getEditor().activeProperty().bind(currentTabEntity.isEqualTo(te));
    te.getEditor().fileProperty().set(file);
    tabEntities.add(te);
    return te;
  }

  public int nextNewOrder() {
    for (int i = 1;; i++) {
      int ii = i;
      if (tabEntities.stream()
          .map(t -> t.getEditor().fileProperty().get())
          .filter(f -> f.isNewFile())
          .map(f -> f.getNewOrder().get())
          .allMatch(n -> n != ii)) {
        return i;
      }
    }
  }
}
