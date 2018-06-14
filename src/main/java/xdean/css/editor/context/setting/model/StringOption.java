package xdean.css.editor.context.setting.model;

import java.util.function.UnaryOperator;

import javafx.beans.property.StringProperty;
import xdean.jfxex.bean.BeanConvertUtil;
import xdean.jfxex.util.StringConverters;

public class StringOption extends SimpleOption<String> {

  public StringOption(String key, String defaultValue) {
    super(key, defaultValue, StringConverters.create(UnaryOperator.identity()));
  }

  public StringProperty stringProperty() {
    return BeanConvertUtil.toString(valueProperty());
  }
}
