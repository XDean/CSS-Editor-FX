package xdean.css.editor.config.option;

import javafx.beans.property.Property;

public interface Option<T> {

  public Property<T> property();

  public default T get() {
    return property().getValue();
  }

  public void set(T t);

  public T getDefault();

  /**
   * Describe should be unique
   * 
   * @return
   */
  public String getDescribe();

  /********************************* Factory ***************************************/
  static <T> Option<T> create(T defaultValue, String describe) {
    return new SimpleOption<>(defaultValue, describe);
  }

  static <T> ValueOption<T> createValue(T defaultValue, String describe) {
    return new ValueOption<>(defaultValue, describe);
  }

  static IntegerOption create(int defaultValue, String describe) {
    return new IntegerOption(defaultValue, describe);
  }

  static BooleanOption create(boolean defaultValue, String describe) {
    return new BooleanOption(defaultValue, describe);
  }
}