package xdean.css.editor.controller;

import static xdean.jfxex.bean.BeanUtil.mapList;

import javax.inject.Inject;
import javax.inject.Provider;

import org.springframework.stereotype.Component;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import xdean.css.editor.control.CssEditor;
import xdean.css.editor.model.FileWrapper;
import xdean.jex.util.task.If;
import xdean.jfxex.bean.annotation.CheckNull;
import xdean.jfxex.bean.property.ObjectPropertyEX;

@Component
public class MainFrameModel {

  private @Inject Provider<CssEditor> editorFactory;
  private @Inject Provider<CssEditorTab> editorTabFactory;

  final ObservableList<CssEditor> editors = FXCollections.observableArrayList();
  final ObservableList<CssEditorTab> tabs = mapList(editors, e -> editorTabFactory.get().bind(e));
  final ObjectPropertyEX<@CheckNull CssEditor> activeEditor = new ObjectPropertyEX<>(this, "currentEditor");

  public CssEditor newTab(FileWrapper file) {
    CssEditor editor = editorFactory.get();
    editors.add(editor);
    editor.activeProperty().addListener((ob, o, n) -> If.that(n).todo(() -> activeEditor.set(editor)));
    editor.fileProperty().set(file);
    return editor;
  }

  public int nextNewOrder() {
    for (int i = 1;; i++) {
      int ii = i;
      if (editors.stream()
          .map(t -> t.fileProperty().get())
          .filter(f -> f.isNewFile())
          .map(f -> f.getNewOrder().get())
          .allMatch(n -> n != ii)) {
        return i;
      }
    }
  }
}
