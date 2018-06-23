package xdean.css.editor.context.setting.model.option;

import javafx.util.StringConverter;

public abstract class SimpleConstraintOption<T> extends SimpleOption<T> implements ConstraintOption<T> {

  SimpleConstraintOption(String key, T defaultValue, StringConverter<T> conveter) {
    super(key, defaultValue, conveter);
  }

  @Override
  public void setValue(T t) {
    if (isValid(t)) {
      valueProperty().setValue(t);
    }
  }
}
