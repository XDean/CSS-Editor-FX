package xdean.css.editor.context.setting;

import static xdean.css.editor.context.setting.SettingKeys.LANGUAGE;
import static xdean.css.editor.context.setting.SettingKeys.RECENT_LOC;
import static xdean.css.editor.context.setting.SettingKeys.SKIN;

import java.util.Locale;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import xdean.css.editor.context.setting.DefaultValue.DefaultSkin;
import xdean.css.editor.context.setting.SettingKeys.Find;
import xdean.css.editor.context.setting.model.BooleanOption;
import xdean.css.editor.context.setting.model.Option;
import xdean.css.editor.context.setting.model.SimpleOption;
import xdean.css.editor.context.setting.model.StringOption;
import xdean.jfxex.util.StringConverters;

@Configuration
public class OtherSettings {

  @Bean(LANGUAGE)
  public Option<Locale> locale() {
    return new SimpleOption<>(LANGUAGE, DefaultValue.DEFAULT_LOCALE, StringConverters.create(Locale::forLanguageTag));
  }

  @Bean(SKIN)
  public StringOption skin() {
    return new StringOption(SKIN, DefaultSkin.CLASSIC.getName());
  }

  @Bean(RECENT_LOC)
  public StringOption recent() {
    return new StringOption(RECENT_LOC, "");
  }

  @Bean(Find.WRAP_SEARCH)
  public BooleanOption wrapSearch() {
    return new BooleanOption(Find.WRAP_SEARCH, true);
  }

  @Bean(Find.REGEX)
  public BooleanOption regexSearch() {
    return new BooleanOption(Find.REGEX, false);
  }

  @Bean(Find.CASE_SENSITIVE)
  public BooleanOption caseSensitive() {
    return new BooleanOption(Find.CASE_SENSITIVE, false);
  }
}
