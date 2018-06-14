package xdean.css.editor.config;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

import com.google.common.collect.BoundType;

import javafx.scene.text.Font;
import lombok.extern.slf4j.Slf4j;
import xdean.css.editor.config.option.BooleanOption;
import xdean.css.editor.config.option.IntegerOption;
import xdean.css.editor.config.option.Option;
import xdean.css.editor.config.option.OptionGroup;
import xdean.css.editor.config.option.ValueOption;

@Slf4j
public class Options {

  public static final OptionGroup ALL = new OptionGroup("All"),
      GENERAL = ALL.add(new OptionGroup("General")),
      KEY = ALL.add(new OptionGroup("Key")),
      COMMON = GENERAL.add(new OptionGroup("Common")),
      TEXT = GENERAL.add(new OptionGroup("Text")),
      OTHER = ALL.add(new OptionGroup("Other")),
      FIND = OTHER.add(new OptionGroup("Find"));

  public static final BooleanOption autoSuggest = COMMON.add(Option.create(true, "Auto Completion"));
  public static final BooleanOption showLineNo = COMMON.add(Option.create(true, "Show Line Number"));
  public static final BooleanOption openLastFile = COMMON.add(Option.create(true, "Auto Open Last Closed File"));
  public static final ValueOption<Charset> charset = COMMON.add(Option.createValue(DefaultValue.DEFAULT_CHARSET, "Charset"));
  public static final ValueOption<String> fontFamily = TEXT
      .add(Option.createValue(DefaultValue.DEFAULT_FONT_FAMILY, "Font Family"));
  public static final IntegerOption fontSize = TEXT.add(Option.create(DefaultValue.DEFAULT_FONT_SIZE, "Font Size"));
  public static final BooleanOption wrapText = TEXT.add(Option.create(true, "Wrap text"));
  public static final BooleanOption findRegex = FIND.add(Option.create(false, "Find Regex"));
  public static final BooleanOption findWrapText = FIND.add(Option.create(true, "Find Wrap Text"));
  public static final BooleanOption findCaseSensitive = FIND.add(Option.create(false, "Find Case Sensitive"));

  static {
    Key.values();
  }

  public static final class DefaultValue {
    public static final int DEFAULT_FONT_SIZE = 16;
    public static final int MIN_FONT_SIZE = 8;
    public static final int MAX_FONT_SIZE = 36;
    public static final String DEFAULT_FONT_FAMILY = "Monospaced";
    public static final Charset DEFAULT_CHARSET = Charset.defaultCharset();
    public static final String[] DEF_FONT_FAMILIES = {
        "Consolas",
        "DejaVu Sans Mono",
        "Lucida Sans Typewriter",
        "Lucida Console",
    };
  }

  static {
    fontSize.setRange(DefaultValue.MIN_FONT_SIZE, BoundType.CLOSED, DefaultValue.MAX_FONT_SIZE, BoundType.CLOSED);
    fontFamily.addAll(Arrays.asList(DefaultValue.DEF_FONT_FAMILIES));
    fontFamily.addAll(Font.getFamilies());
    charset.addAll(Charset.availableCharsets().values());

    bindBool(autoSuggest);
    bindBool(showLineNo);
    bindBool(openLastFile);
    bind(charset, Options::safeCharSet);
    bind(fontFamily, Options::safeFontFamily);
    bindInt(fontSize);
    bindBool(wrapText);
    bindBool(findRegex);
    bindBool(findCaseSensitive);
    bindBool(findWrapText);
  }

  /**
   * Bind the option with configuration by the converter
   */
  static <T, O extends Option<T>> O bind(O o, Function<String, T> converter, Function<T, String> toString) {
    String key = o.getDescribe();
    try {
      o.set(converter.apply(Config.getProperty(key).get()));
    } catch (Exception e) {
      log.trace(String.format("Option \"%s\" init fail.", o.getDescribe()), e);
      o.set(o.getDefault());
      Config.setProperty(key, toString.apply(o.get()));
    }
    o.property().addListener((ob, old, n) -> Config.setProperty(key, toString.apply(o.get())));
    return o;
  }

  static <T, O extends Option<T>> O bind(O o, Function<String, T> converter) {
    return bind(o, converter, t -> t.toString());
  }

  static <O extends Option<String>> O bind(O o) {
    return bind(o, UnaryOperator.identity());
  }

  static <O extends Option<Boolean>> O bindBool(O o) {
    return bind(o, Boolean::valueOf);
  }

  static <O extends Option<Integer>> O bindInt(O o) {
    return bind(o, Integer::valueOf);
  }

  static <T> void filter(Option<T> o, Predicate<T> filter) {
    o.property().addListener((ob, old, n) -> {
      if (filter.test(n) == false) {
        o.set(old);
      }
    });
  }

  static <T> void normalize(Option<T> o, UnaryOperator<T> normalize) {
    o.property().addListener((ob, old, n) -> {
      T normal = normalize.apply(n);
      if (normal.equals(n) == false) {
        o.set(normal);
      }
    });
  }

  /**
   * Check whether font family is null or invalid (family not available on system) and search for an
   * available family.
   */
  private static String safeFontFamily(String fontFamily) {
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

  private static Charset safeCharSet(String charsetName) {
    if (Charset.isSupported(charsetName)) {
      return Charset.forName(charsetName);
    } else {
      return Charset.defaultCharset();
    }
  }

}
