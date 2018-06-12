package xdean.css.editor.context.option.model;

import com.sun.javafx.binding.BidirectionalBinding;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

public class BooleanOption extends SimpleOption<Boolean> {

  BooleanProperty property;

  BooleanOption(boolean defaultValue, String describe) {
    super(defaultValue, describe);
    property = new SimpleBooleanProperty();
    BidirectionalBinding.bind(property, writableProperty());
  }

  @Override
  public BooleanProperty property() {
    return property;
  }
}