package xdean.css.editor.model;

import javafx.css.ParsedValue;
import javafx.scene.text.Font;

class SimpleParsedValue<V, T> extends ParsedValue<V, T> {
  T result;

  SimpleParsedValue(T actualResult) {
    super(null, null);
    result = actualResult;
  }

  @Override
  public T convert(Font font) {
    return result;
  }
}