package xdean.css.editor.context.setting;

import static xdean.css.editor.context.setting.SettingKeys.FIND;
import static xdean.css.editor.context.setting.SettingKeys.LANGUAGE;
import static xdean.css.editor.context.setting.SettingKeys.RECENT_LOC;
import static xdean.css.editor.context.setting.SettingKeys.SKIN;

import java.util.Locale;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import xdean.css.editor.context.DefaultSkin;
import xdean.css.editor.context.setting.SettingKeys.Find;
import xdean.css.editor.context.setting.model.BooleanOption;
import xdean.css.editor.context.setting.model.Option;
import xdean.css.editor.context.setting.model.OptionGroup;
import xdean.css.editor.context.setting.model.SimpleOption;
import xdean.css.editor.context.setting.model.StringOption;
import xdean.jfxex.util.StringConverters;

@Configuration
public class OtherSettings {

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

  @Bean(FIND)
  public OptionGroup find() {
    return new OptionGroup(FIND);
  }

  @Bean(Find.WRAP_SEARCH)
  public BooleanOption wrapSearch() {
    return find().add(new BooleanOption(Find.WRAP_SEARCH, true));
  }

  @Bean(Find.REGEX)
  public BooleanOption regexSearch() {
    return find().add(new BooleanOption(Find.REGEX, false));
  }

  @Bean(Find.CASE_SENSITIVE)
  public BooleanOption caseSensitive() {
    return find().add(new BooleanOption(Find.CASE_SENSITIVE, false));
  }
}
