package xdean.css.editor.context.setting.model;

import static xdean.jfxex.event.EventHandlers.consumeIf;

import io.reactivex.rxjavafx.observables.JavaFxObservable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.EventType;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.util.StringConverter;
import xdean.css.editor.context.setting.model.option.Option;
import xdean.css.editor.control.CssEditor;
import xdean.css.editor.control.CssEditorEvent;
import xdean.jfxex.bean.property.ObjectPropertyEX;
import xdean.jfxex.util.StringConverters;

public class CssEditorKeyOption<T>  implements Option<KeyCombination> {

  private static final StringConverter<KeyCombination> CONVERTER = StringConverters.create(KeyCombination::valueOf);

  private final String key;
  private final EventType<CssEditorEvent<T>> eventType;
  private final ObjectPropertyEX<KeyCombination> value = new ObjectPropertyEX<>(this, "value");
  private final KeyCombination defaultValue;
  private final BooleanProperty disable = new SimpleBooleanProperty(this, "disable", false);

  public CssEditorKeyOption(String key, KeyCombination defaultValue) {
    this.key = key;
    this.defaultValue = defaultValue;
    this.eventType = new EventType<>(CssEditorEvent.ANY, key);
    this.value.defaultForNull(KeyCombination.NO_MATCH);
  }

  @Override
  public String getKey() {
    return key;
  }

  public EventType<CssEditorEvent<T>> getEventType() {
    return eventType;
  }

  public BooleanProperty disableProperty() {
    return disable;
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
    editor.addEventFilter(eventType, consumeIf(e -> disable.get()));
    JavaFxObservable.eventsOf(editor, KeyEvent.KEY_PRESSED)
        .filter(e -> getValue().match(e))
        .doOnNext(KeyEvent::consume)
        .subscribe(e -> editor.fireEvent(getEvent(editor)));
  }

  public CssEditorEvent<T> getEvent(CssEditor editor) {
    return new CssEditorEvent<>(editor, getEventType());
  }

  public CssEditorEvent<T> getEvent(CssEditor editor, T data) {
    return new CssEditorEvent<>(editor, getEventType(), data);
  }
}
