package xdean.css.editor.config.option;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;

public class SimpleOption<T> implements Option<T> {

  private final Property<T> property;
  private final T defaultValue;
  private final String describe;

  SimpleOption(T defaultValue, String describe) {
    this.property = new SimpleObjectProperty<>();
    this.defaultValue = defaultValue;
    this.describe = describe;

    property.setValue(defaultValue);
  }

  protected Property<T> writableProperty() {
    return property;
  }

  @Override
  public Property<T> property() {
    return property;
  }

  @Override
  public void set(T t) {
    property.setValue(t);
  }

  @Override
  public T getDefault() {
    return defaultValue;
  }

  @Override
  public String getDescribe() {
    return describe;
  }

  @Override
  public String toString() {
    return "SimpleOption [property=" + property + ", defaultValue=" + defaultValue + ", describe=" + describe + "]";
  }

}