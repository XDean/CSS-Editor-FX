package xdean.css.editor.service;

import javax.annotation.Nullable;

import javafx.scene.Node;
import xdean.css.editor.context.setting.model.CssEditorKeyEventOption;
import xdean.css.editor.control.CssEditor;
import xdean.css.editor.control.CssEditorEvent;

public interface ContextService {
  @Nullable
  CssEditor activeEditor();

  Node eventNode();

  default void fire(@Nullable CssEditor editor, CssEditorEvent event) {
    if (editor == null) {
      eventNode().fireEvent(event);
    } else {
      editor.fireEvent(event);
    }
  }

  default void fire(@Nullable CssEditor editor, CssEditorKeyEventOption keyOption) {
    fire(editor, keyOption.getEvent(editor));
  }

  default void fire(CssEditorEvent event) {
    fire(activeEditor(), event);
  }

  default void fire(CssEditorKeyEventOption keyOption) {
    fire(activeEditor(), keyOption);
  }
}
