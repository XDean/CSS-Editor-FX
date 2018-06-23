package xdean.css.editor.context.setting.model;

import io.reactivex.rxjavafx.observables.JavaFxObservable;
import javafx.beans.property.Property;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.util.StringConverter;
import xdean.css.editor.context.setting.model.action.SimpleAction;
import xdean.css.editor.context.setting.model.option.Option;
import xdean.css.editor.control.CssEditor;
import xdean.jfxex.bean.property.ObjectPropertyEX;
import xdean.jfxex.util.StringConverters;

public class CssEditorKeyActionOption extends SimpleAction<CssEditor> implements Option<KeyCombination> {

  private static final StringConverter<KeyCombination> CONVERTER = StringConverters.create(KeyCombination::valueOf);

  private final ObjectPropertyEX<KeyCombination> value = new ObjectPropertyEX<>(this, "value");
  private final KeyCombination defaultValue;

  public CssEditorKeyActionOption(String key, KeyCombination defaultValue) {
    super(key);
    this.defaultValue = defaultValue;
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
        .subscribe(e -> producer().onNext(editor));
  }
}
