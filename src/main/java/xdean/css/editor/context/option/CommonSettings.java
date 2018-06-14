package xdean.css.editor.context.option;

import static xdean.css.editor.context.option.OptionKeys.*;

import java.util.Locale;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import xdean.css.editor.context.DefaultSkin;
import xdean.css.editor.context.option.model.Option;
import xdean.css.editor.context.option.model.SimpleOption;
import xdean.css.editor.context.option.model.StringOption;
import xdean.jfxex.util.StringConverters;

@Configuration
public class CommonSettings {

  @Bean(LANGUAGE)
  public Option<Locale> locale() {
    return new SimpleOption<>(LANGUAGE, Locale.ENGLISH, StringConverters.create(Locale::forLanguageTag));
  }

  @Bean(SKIN)
  public StringOption skin() {
    return new StringOption(SKIN, DefaultSkin.CLASSIC.getName());
  }

  @Bean(RECENT_LOC)
  public StringOption recent() {
    return new StringOption(RECENT_LOC, "");
  }
}
