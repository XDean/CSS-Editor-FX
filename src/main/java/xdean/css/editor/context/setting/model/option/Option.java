package xdean.css.editor.context.setting.model.option;

import javafx.beans.property.Property;
import javafx.util.StringConverter;
import xdean.css.editor.context.Config;
import xdean.jex.extra.tryto.Try;

public interface Option<T> {

  Property<T> valueProperty();

  default T getValue() {
    return valueProperty().getValue();
  }

  default void setValue(T t) {
    valueProperty().setValue(t);
  }

  T getDefaultValue();

  String getKey();

  StringConverter<T> getConverter();

  default void bind(Config config) {
    StringConverter<T> converter = getConverter();
    valueProperty()
        .setValue(Try.to(() -> config.getProperty(getKey()).map(converter::fromString).get()).getOrElse(getDefaultValue()));
    valueProperty().addListener((ob, o, n) -> config.setProperty(getKey(), converter.toString(n)));
  }
}