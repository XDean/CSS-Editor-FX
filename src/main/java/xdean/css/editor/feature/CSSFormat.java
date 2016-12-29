package xdean.css.editor.feature;

import java.util.stream.Stream;

public class CSSFormat {

  private static final String LINE_COMMENT_PATTERN = "^\\s*/\\*.*\\*/\\s*$";

  public static String format(String cssText) {
    return null;
  }

  public static String toggleComment(String text) {
    String[] split = text.split("\\R");
    boolean allMatch = Stream.of(split).allMatch(s -> s.matches(LINE_COMMENT_PATTERN));
    if (allMatch) {
      return Stream.of(split).map(s -> clearComment(s))
          .reduce((s1, s2) -> String.join(System.lineSeparator(), s1, s2)).orElse(text);
    } else {
      return Stream.of(split).map(s -> addComment(s))
          .reduce((s1, s2) -> String.join(System.lineSeparator(), s1, s2)).orElse(text);
    }
  }

  private static String addComment(String line) {
    return "/* " + line + " */";
  }

  private static String clearComment(String line) {
    String trim = line.trim();
    String replace = trim.replaceAll("^/\\* ?", "").replaceAll(" ?\\*/$", "");
    return line.replace(trim, replace);
  }
}
