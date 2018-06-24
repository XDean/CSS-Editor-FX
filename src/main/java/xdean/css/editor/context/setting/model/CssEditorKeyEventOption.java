package xdean.css.editor.context.setting.model;

import java.util.Optional;

import io.reactivex.rxjavafx.observables.JavaFxObservable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.Event;
import javafx.event.EventType;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.util.StringConverter;
import xdean.css.editor.context.setting.model.option.Option;
import xdean.css.editor.control.CssEditor;
import xdean.jfxex.bean.property.ObjectPropertyEX;
import xdean.jfxex.util.StringConverters;

public class CssEditorKeyEventOption implements Option<KeyCombination> {

  private static final StringConverter<KeyCombination> CONVERTER = StringConverters.create(KeyCombination::valueOf);

  private final String key;
  private final EventType<Event> eventType;
  private final ObjectPropertyEX<KeyCombination> value = new ObjectPropertyEX<>(this, "value");
  private final KeyCombination defaultValue;
  private final BooleanProperty enable = new SimpleBooleanProperty(this, "enable", true);

  public CssEditorKeyEventOption(String key, KeyCombination defaultValue) {
    this.key = key;
    this.defaultValue = defaultValue;
    this.eventType = new EventType<>(Event.ANY, key);
  }

  @Override
  public String getKey() {
    return key;
  }

  public EventType<Event> getEventType() {
    return eventType;
  }

  public BooleanProperty enableProperty() {
    return enable;
  }

  @Override
  public Property<KeyCombination> valueProperty() {
    return value;
  }

  @Override
  public KeyCombination getDefaultValue() {
    return defaultValue;
  }

  @Override
  public StringConverter<KeyCombination> getConverter() {
    return CONVERTER;
  }

  public void bind(CssEditor editor) {
    JavaFxObservable.eventsOf(editor, KeyEvent.KEY_PRESSED)
        .filter(e -> e.isConsumed() == false)
        .filter(getValue()::match)
        .doOnNext(KeyEvent::consume)
        .subscribe(e -> fire(editor));
  }

  public void fire(CssEditor editor) {
    editor.fireEvent(new Event(editor, editor, eventType));
  }

  public void fire(Optional<CssEditor> editor) {
    editor.ifPresent(this::fire);
  }
}
