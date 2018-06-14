package xdean.css.editor.context.setting.model;

import javafx.util.StringConverter;
import xdean.jfxex.bean.property.ObjectPropertyEX;

public class SimpleOption<T> implements Option<T> {

  private final String key;
  private final ObjectPropertyEX<T> value = new ObjectPropertyEX<>(this, "value");
  private final T defaultValue;
  private final StringConverter<T> converter;

  public SimpleOption(String key, T defaultValue, StringConverter<T> converter) {
    this.defaultValue = defaultValue;
    this.key = key;
    this.converter = converter;

    value.defaultForNull(defaultValue);
  }

  @Override
  public String getKey() {
    return key;
  }

  @Override
  public ObjectPropertyEX<T> valueProperty() {
    return value;
  }

  @Override
  public T getDefaultValue() {
    return defaultValue;
  }

  @Override
  public StringConverter<T> getConverter() {
    return converter;
  }

  @Override
  public String toString() {
    return "Option [property=" + value + ", defaultValue=" + defaultValue + ", key=" + key + "]";
  }
}