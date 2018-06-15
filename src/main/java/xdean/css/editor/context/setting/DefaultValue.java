package xdean.css.editor.context.setting;

import java.nio.charset.Charset;
import java.util.Locale;

import xdean.jex.util.string.StringUtil;
import xdean.jfxex.support.skin.SkinStyle;

public interface DefaultValue {
  int DEFAULT_FONT_SIZE = 16;
  int MIN_FONT_SIZE = 8;
  int MAX_FONT_SIZE = 36;
  String DEFAULT_FONT_FAMILY = "Monospaced";
  Locale DEFAULT_LOCALE = Locale.ENGLISH;
  Charset DEFAULT_CHARSET = Charset.defaultCharset();
  String[] DEF_FONT_FAMILIES = {
      "Consolas",
      "DejaVu Sans Mono",
      "Lucida Sans Typewriter",
      "Lucida Console",
  };

  enum DefaultSkin implements SkinStyle {
    CLASSIC("classic"),
    DEFAULT("default"),
    METAL("metal"),
    PINK("pink");

    private static final String CSS_PATH = "/css/skin/";

    private String path;

    private DefaultSkin(String name) {
      try {
        this.path = DefaultSkin.class.getResource(CSS_PATH + name + ".bss").toExternalForm();
      } catch (NullPointerException e) {// If the resource not exist
        this.path = DefaultSkin.class.getResource(CSS_PATH + name + ".css").toExternalForm();
      }
    }

    @Override
    public String getURL() {
      return path;
    }

    @Override
    public String getName() {
      return StringUtil.upperFirst(toString());
    }
  }
}