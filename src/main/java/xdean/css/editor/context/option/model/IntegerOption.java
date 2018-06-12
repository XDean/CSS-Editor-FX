package xdean.css.editor.context.option.model;

import com.sun.javafx.binding.BidirectionalBinding;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

public class IntegerOption extends RangeOption<Integer> {

  private final IntegerProperty property;

  IntegerOption(int defaultValue, String describe) {
    super(defaultValue, describe);
    property = new SimpleIntegerProperty();
    BidirectionalBinding.bindNumber(property, writableProperty());
  }

  public IntegerProperty intProperty() {
    return property;
  }

  @Override
  public Integer get() {
    return property.get();
  }
}