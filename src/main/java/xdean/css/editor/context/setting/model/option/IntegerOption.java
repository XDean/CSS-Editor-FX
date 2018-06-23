package xdean.css.editor.context.setting.model.option;

import static xdean.jex.util.cache.CacheUtil.cache;
import static xdean.jfxex.bean.BeanConvertUtil.toInteger;

import javafx.beans.property.IntegerProperty;
import xdean.jfxex.util.StringConverters;

public class IntegerOption extends RangeOption<Integer> {

  public IntegerOption(String key, int defaultValue) {
    super(key, defaultValue, StringConverters.forInteger(defaultValue));
  }

  public IntegerProperty intProperty() {
    return cache(this, () -> toInteger(valueProperty()));
  }
}