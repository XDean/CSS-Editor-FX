package xdean.css.editor.context.setting;

import java.nio.charset.Charset;

public interface DefaultValue {
  int DEFAULT_FONT_SIZE = 16;
  int MIN_FONT_SIZE = 8;
  int MAX_FONT_SIZE = 36;
  String DEFAULT_FONT_FAMILY = "Monospaced";
  Charset DEFAULT_CHARSET = Charset.defaultCharset();
  String[] DEF_FONT_FAMILIES = {
      "Consolas",
      "DejaVu Sans Mono",
      "Lucida Sans Typewriter",
      "Lucida Console",
  };
}