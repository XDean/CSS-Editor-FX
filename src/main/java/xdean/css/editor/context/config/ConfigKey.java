package xdean.css.editor.context.config;

import java.util.Arrays;
import java.util.function.UnaryOperator;

import javax.inject.Inject;

import org.springframework.stereotype.Component;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.util.StringConverter;
import xdean.css.editor.context.option.model.Option;
import xdean.jfxex.util.StringConverters;

public enum ConfigKey implements Option<String> {
  LANGUAGE("language", "en_US"),
  CHARSET("charset", "UTF-8"),
  SKIN("skin", ""),
  RECENT_LOCATIONS("recent-location", "");

  public final String key;
  public final String defaultValue;
  private final StringProperty value = new SimpleStringProperty(this, "value");

  private ConfigKey(String key, String defaultValue) {
    this.key = key;
    this.defaultValue = defaultValue;
  }

  @Override
  public String getKey() {
    return key;
  }

  @Override
  public String getDefaultValue() {
    return defaultValue;
  }

  @Override
  public StringProperty valueProperty() {
    return value;
  }

  @Component
  public static class Injector {
    @Inject
    public void bind(Config config) {
      Arrays.stream(ConfigKey.values()).forEach(k -> k.bind(config));
    }
  }

  @Override
  public StringConverter<String> getConverter() {
    return StringConverters.create(UnaryOperator.identity());
  }
}
