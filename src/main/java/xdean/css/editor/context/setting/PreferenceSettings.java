package xdean.css.editor.context.setting;

import static xdean.css.editor.context.setting.SettingKeys.GENERAL;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.common.collect.BoundType;

import javafx.scene.text.Font;
import xdean.css.editor.context.setting.SettingKeys.General;
import xdean.css.editor.context.setting.model.option.BooleanOption;
import xdean.css.editor.context.setting.model.option.IntegerOption;
import xdean.css.editor.context.setting.model.option.OptionGroup;
import xdean.css.editor.context.setting.model.option.ValueOption;
import xdean.jfxex.util.StringConverters;

@Configuration
public class PreferenceSettings {

  @Bean(GENERAL)
  public OptionGroup general() {
    OptionGroup general = new OptionGroup(GENERAL);
    general.add(common());
    general.add(text());
    return general;
  }

  @Bean(General.COMMON)
  public OptionGroup common() {
    OptionGroup common = new OptionGroup(General.COMMON);
    common.add(autoSuggest());
    common.add(showLineNo());
    common.add(openLast());
    common.add(charset());
    return common;
  }

  @Bean(General.Common.AUTO_SUGGEST)
  public BooleanOption autoSuggest() {
    return new BooleanOption(General.Common.AUTO_SUGGEST, true);
  }

  @Bean(General.Common.SHOW_LINE)
  public BooleanOption showLineNo() {
    return new BooleanOption(General.Common.SHOW_LINE, true);
  }

  @Bean(General.Common.OPEN_LAST)
  public BooleanOption openLast() {
    return new BooleanOption(General.Common.OPEN_LAST, true);
  }

  @Bean(General.Common.CHARSET)
  public ValueOption<Charset> charset() {
    ValueOption<Charset> charset = new ValueOption<>(General.Common.CHARSET, DefaultValue.DEFAULT_CHARSET,
        StringConverters.create(this::safeCharSet));
    charset.values.setAll(Charset.availableCharsets().values());
    return charset;
  }

  @Bean(General.TEXT)
  public OptionGroup text() {
    OptionGroup text = new OptionGroup(General.TEXT);
    text.add(fontFamily());
    text.add(fontSize());
    text.add(wrapText());
    return text;
  }

  @Bean(General.Text.FONT_FAMILY)
  public ValueOption<String> fontFamily() {
    ValueOption<String> fontFamily = new ValueOption<>(General.Text.FONT_FAMILY, DefaultValue.DEFAULT_FONT_FAMILY,
        StringConverters.create(this::safeFontFamily));
    fontFamily.values.setAll(Arrays.asList(DefaultValue.DEF_FONT_FAMILIES));
    fontFamily.values.addAll(Font.getFamilies());
    return fontFamily;
  }

  @Bean(General.Text.FONT_SIZE)
  public IntegerOption fontSize() {
    IntegerOption fontSize = new IntegerOption(General.Text.FONT_SIZE, DefaultValue.DEFAULT_FONT_SIZE);
    fontSize.setRange(DefaultValue.MIN_FONT_SIZE, BoundType.CLOSED, DefaultValue.MAX_FONT_SIZE, BoundType.CLOSED);
    return fontSize;
  }

  @Bean(General.Text.WRAP_TEXT)
  public BooleanOption wrapText() {
    return new BooleanOption(General.Text.WRAP_TEXT, true);
  }

  /**
   * Check whether font family is null or invalid (family not available on system) and search for an
   * available family.
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
}
