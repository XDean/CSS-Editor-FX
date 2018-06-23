package xdean.css.editor.context.setting.model.option;

import com.google.common.collect.BoundType;
import com.google.common.collect.Range;

import javafx.util.StringConverter;

public abstract class RangeOption<T extends Comparable<T>> extends SimpleConstraintOption<T> {

  Range<T> range;

  RangeOption(String key, T defaultValue, StringConverter<T> converter) {
    super(key, defaultValue, converter);
    range = Range.all();
  }

  @Override
  public boolean isValid(T t) {
    return range.contains(t);
  }

  public Range<T> getRange() {
    return range;
  }

  public T getMin() {
    return range.lowerEndpoint();
  }

  public void setMin(T min, BoundType type) {
    range = Range.range(min, type, range.upperEndpoint(), range.upperBoundType());
  }

  public T getMax() {
    return range.upperEndpoint();
  }

  public void setMax(T max, BoundType type) {
    range = Range.range(range.lowerEndpoint(), range.lowerBoundType(), max, type);
  }

  public void setRange(T min, BoundType minType, T max, BoundType maxType) {
    range = Range.range(min, minType, max, maxType);
  }
}
