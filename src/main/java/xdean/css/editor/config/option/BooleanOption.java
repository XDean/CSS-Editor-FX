package xdean.css.editor.config.option;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

import com.sun.javafx.binding.BidirectionalBinding;

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