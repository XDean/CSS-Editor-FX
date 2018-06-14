package xdean.css.editor.context.option;

import static xdean.css.editor.context.option.OptionKeys.GENERAL;
import static xdean.css.editor.context.option.OptionKeys.KEY;
import static xdean.css.editor.context.option.OptionKeys.OTHER;
import static xdean.css.editor.context.option.OptionKeys.ROOT;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.common.collect.BoundType;

import javafx.scene.text.Font;
import javafx.util.StringConverter;
import xdean.css.editor.context.option.OptionKeys.General;
import xdean.css.editor.context.option.OptionKeys.Other;
import xdean.css.editor.context.option.OptionKeys.Other.Find;
import xdean.css.editor.context.option.model.BooleanOption;
import xdean.css.editor.context.option.model.IntegerOption;
import xdean.css.editor.context.option.model.OptionGroup;
import xdean.css.editor.context.option.model.ValueOption;
import xdean.jfxex.util.StringConverters;

@Configuration
public class Options {

  @Bean(ROOT)
  public OptionGroup root() {
    return new OptionGroup(ROOT);
  }

  @Bean(GENERAL)
  public OptionGroup general() {
    return root().add(new OptionGroup(GENERAL));
  }

  @Bean(name = KEY)
  public OptionGroup key() {
    return root().add(new OptionGroup(KEY));
  }

  @Bean(General.COMMON)
  public OptionGroup common() {
    return general().add(new OptionGroup(General.COMMON));
  }

  @Bean(General.TEXT)
  public OptionGroup text() {
    return general().add(new OptionGroup(General.TEXT));
  }

  @Bean(OTHER)
  public OptionGroup other() {
    return root().add(new OptionGroup(OTHER));
  }

  @Bean(Other.FIND)
  public OptionGroup find() {
    return other().add(new OptionGroup(Other.FIND));
  }

  @Bean(General.Common.AUTO_SUGGEST)
  public BooleanOption autoSuggest() {
    return common().add(create(General.Common.AUTO_SUGGEST, true));
  }

  @Bean(General.Common.SHOW_LINE)
  public BooleanOption showLineNo() {
    return common().add(create(General.Common.SHOW_LINE, true));
  }

  @Bean(General.Common.OPEN_LAST)
  public BooleanOption openLast() {
    return common().add(create(General.Common.OPEN_LAST, true));
  }

  @Bean(General.Common.CHARSET)
  public ValueOption<Charset> charset() {
    ValueOption<Charset> charset = createValue(General.Common.CHARSET, DefaultValue.DEFAULT_CHARSET,
        StringConverters.create(this::safeCharSet));
    charset.values.setAll(Charset.availableCharsets().values());
    return common().add(charset);
  }

  @Bean(General.Text.FONT_FAMILY)
  public ValueOption<String> fontFamily() {
    ValueOption<String> fontFamily = createValue(General.Text.FONT_FAMILY, DefaultValue.DEFAULT_FONT_FAMILY,
        StringConverters.create(this::safeFontFamily));
    fontFamily.values.setAll(Arrays.asList(DefaultValue.DEF_FONT_FAMILIES));
    fontFamily.values.setAll(Font.getFamilies());
    return text().add(fontFamily);
  }

  @Bean(General.Text.FONT_SIZE)
  public IntegerOption fontSize() {
    IntegerOption fontSize = create(General.Text.FONT_SIZE, DefaultValue.DEFAULT_FONT_SIZE);
    fontSize.setRange(DefaultValue.MIN_FONT_SIZE, BoundType.CLOSED, DefaultValue.MAX_FONT_SIZE, BoundType.CLOSED);
    return text().add(fontSize);
  }

  @Bean(General.Text.WRAP_TEXT)
  public BooleanOption wrapText() {
    return text().add(create(General.Text.WRAP_TEXT, true));
  }

  @Bean(Find.WRAP_SEARCH)
  public BooleanOption wrapSearch() {
    return find().add(create(Find.WRAP_SEARCH, true));
  }

  @Bean(Find.REGEX)
  public BooleanOption regexSearch() {
    return find().add(create(Find.REGEX, false));
  }

  @Bean(Find.CASE_SENSITIVE)
  public BooleanOption caseSensitive() {
    return find().add(create(Find.CASE_SENSITIVE, false));
  }

  /**
   * Check whether font family is null or invalid (family not available on
   * system) and search for an available family.
   */
  private String safeFontFamily(String fontFamily) {
    List<String> fontFamilies = Font.getFamilies();
    if (fontFamily != null && fontFamilies.contains(fontFamily)) {
      return fontFamily;
    }

    for (String family : DefaultValue.DEF_FONT_FAMILIES) {
      if (fontFamilies.contains(family)) {
        return family;
      }
    }
    return DefaultValue.DEFAULT_FONT_FAMILY;
  }

  private Charset safeCharSet(String charsetName) {
    if (Charset.isSupported(charsetName)) {
      return Charset.forName(charsetName);
    } else {
      return Charset.defaultCharset();
    }
  }

  private BooleanOption create(String key, boolean defaultValue) {
    return new BooleanOption(key, defaultValue);
  }

  private IntegerOption create(String key, int defaultValue) {
    return new IntegerOption(key, defaultValue);
  }

  private <T> ValueOption<T> createValue(String key, T defaultValue, StringConverter<T> converter) {
    return new ValueOption<>(key, defaultValue, converter);
  }
}
