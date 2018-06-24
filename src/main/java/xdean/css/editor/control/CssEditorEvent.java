package xdean.css.editor.control;

import java.util.Optional;

import javafx.event.Event;
import javafx.event.EventType;

public class CssEditorEvent extends Event {

  public static final EventType<CssEditorEvent> ANY = new EventType<>(Event.ANY, "css-editor");

  private final Optional<CssEditor> editor;

  public CssEditorEvent(CssEditor editor, EventType<? extends CssEditorEvent> eventType) {
    super(editor, editor, eventType);
    this.editor = Optional.ofNullable(editor);
  }

  public CssEditorEvent(EventType<? extends CssEditorEvent> eventType) {
    this(null, eventType);
  }

  public Optional<CssEditor> getEditor() {
    return editor;
  }
}
