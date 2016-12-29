package xdean.css.context;

import javafx.css.ParsedValue;

import com.google.common.base.Supplier;

class Util {

  static <V, T> ParsedValue<V, T> createParsedValue(T result) {
    return new SimpleParsedValue<>(result);
  }

  static <V, T> ParsedValue<V, T> createParsedValue(Supplier<T> resultFactory) {
    return new FunctionParsedValue<>(resultFactory);
  }
}
