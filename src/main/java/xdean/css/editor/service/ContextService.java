package xdean.css.editor.service;

import java.util.Optional;

import javax.annotation.Nullable;

import javafx.beans.property.ObjectProperty;
import javafx.event.Event;
import javafx.stage.Stage;
import xdean.css.editor.context.setting.model.CssEditorKeyOption;
import xdean.css.editor.control.CssEditor;
import xdean.jfxex.bean.annotation.CheckNull;

public interface ContextService {

  ObjectProperty<@CheckNull CssEditor> activeEditorProperty();

  @Nullable
  default CssEditor getActiveEditor() {
    return activeEditorProperty().getValue();
  }

  default Optional<CssEditor> getActiveEditorSafe() {
    return Optional.ofNullable(getActiveEditor());
  }

  Stage stage();

  default void fire(@Nullable CssEditor editor, Event event) {
    if (editor == null) {
      stage().fireEvent(event);
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
