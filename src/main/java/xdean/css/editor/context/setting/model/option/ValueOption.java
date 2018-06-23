package xdean.css.editor.context.setting.model.option;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.util.StringConverter;

public class ValueOption<T> extends SimpleConstraintOption<T> {

  public final ObservableList<T> values = FXCollections.observableArrayList();

  public ValueOption(String key, T defaultValue, StringConverter<T> conveter) {
    super(key, defaultValue, conveter);
  }

  @Override
  public boolean isValid(T t) {
    return values.contains(t);
  }
}
