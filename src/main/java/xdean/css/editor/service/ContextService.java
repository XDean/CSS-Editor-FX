package xdean.css.editor.service;

import javax.annotation.Nullable;

import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.scene.Node;
import xdean.css.editor.context.setting.model.CssEditorKeyOption;
import xdean.css.editor.control.CssEditor;
import xdean.jfxex.bean.annotation.CheckNull;

public interface ContextService {

  ObservableValue<@CheckNull CssEditor> activeEditorBinding();

  @Nullable
  default CssEditor getActiveEditor() {
    return activeEditorBinding().getValue();
  }

  Node eventNode();

  default void fire(@Nullable CssEditor editor, Event event) {
    if (editor == null) {
      eventNode().fireEvent(event);
    } else {
      editor.fireEvent(event);
    }
  }

  default void fire(@Nullable CssEditor editor, CssEditorKeyOption<?> keyOption) {
    fire(editor, keyOption.getEvent(editor));
  }

  default void fire(Event event) {
    fire(getActiveEditor(), event);
  }

  default void fire(CssEditorKeyOption<?> keyOption) {
    fire(getActiveEditor(), keyOption);
  }
}
