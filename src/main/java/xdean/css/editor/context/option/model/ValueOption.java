package xdean.css.editor.context.option.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javafx.util.StringConverter;

public class ValueOption<T> extends SimpleConstraintOption<T> {

  List<T> values;

  public ValueOption(String key, T defaultValue, StringConverter<T> conveter) {
    super(key, defaultValue, conveter);
    values = new ArrayList<>();
  }

  @Override
  public boolean isValid(T t) {
    return values.contains(t);
  }

  public List<T> getValues() {
    return Collections.unmodifiableList(values);
  }

  public boolean add(T e) {
    return values.add(e);
  }

  public boolean remove(Object o) {
    return values.remove(o);
  }

  public boolean addAll(Collection<? extends T> c) {
    return values.addAll(c);
  }

  public boolean removeAll(Collection<?> c) {
    return values.removeAll(c);
  }

  public boolean setAll(Collection<? extends T> c) {
    clear();
    return addAll(c);
  }

  public void clear() {
    values.clear();
  }
}
