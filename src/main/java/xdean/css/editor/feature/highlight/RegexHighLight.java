package xdean.css.editor.feature.highlight;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

public class RegexHighLight implements CssHighLight {

  private static final String CSS_REGEX = "(([^\\{\\}/])*)(\\{)(([^}])*)(\\})";
  private static final String SELECTOR_REGEX = "([.#: ]|\\n)([A-Za-z0-9-_]+)";
  private static final String MUTI_COMMENT_REGEX = "/\\*+([^*]|(\\*+[^*/]))*\\*+/";
  private static final String ENTRY_REGEX = "([A-Za-z0-9-_]+)(\\s*:\\s*)(([^;]|\\R)*)(;)";

  private static final Pattern CSS_PATTERN = Pattern.compile(String.format(
      "(?<%s>%s)|(?<%s>%s)", Element.MUTI_COMMENT, MUTI_COMMENT_REGEX, Element.CSS, CSS_REGEX));
  private static final Pattern ENTRY_PATTERN = Pattern.compile(String.format(
      "(?<%s>%s)|(?<%s>%s)", Element.MUTI_COMMENT, MUTI_COMMENT_REGEX, Element.ENTRY, ENTRY_REGEX));
  private static final Pattern SELECTOR_PATTERN = Pattern.compile(SELECTOR_REGEX);

  private static final int GROUP_BEFORE = 5;

  private static final int GROUP_KEY = GROUP_BEFORE + 0;
  private static final int GROPU_COLON = GROUP_BEFORE + 1;
  private static final int GROUP_VALUE = GROUP_BEFORE + 2;
  private static final int GROUP_SEMICOLON = GROUP_BEFORE + 4;

  private static final int GROUP_SELECTOR = GROUP_BEFORE + 0;
  private static final int GROUP_LEFT_BRACE = GROUP_BEFORE + 2;
  private static final int GROUP_VALUES = GROUP_BEFORE + 3;
  private static final int GROUP_RIGHT_BRACE = GROUP_BEFORE + 5;

  @Override
  public StyleSpans<Collection<String>> compute(String text) {
    StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();
    Matcher cssMatcher = CSS_PATTERN.matcher(text);
    int lastKwEnd = 0;
    while (cssMatcher.find()) {
      spansBuilder.add(Collections.emptyList(), cssMatcher.start() - lastKwEnd);
      if (cssMatcher.group(Element.MUTI_COMMENT) != null) {
        spansBuilder.add(Collections.singleton(Element.MUTI_COMMENT), cssMatcher.end() - cssMatcher.start());
      } else {
        if (cssMatcher.group(Element.CSS) != null) {
          String selectorText = cssMatcher.group(GROUP_SELECTOR);
          if (!selectorText.isEmpty()) {
            lastKwEnd = 0;
            Matcher selectorMatcher = SELECTOR_PATTERN.matcher(selectorText);
            while (selectorMatcher.find()) {
              spansBuilder.add(Collections.emptyList(), selectorMatcher.start() - lastKwEnd);
              spansBuilder.add(Collections.emptyList(), selectorMatcher.end(1) - selectorMatcher.start(1));
              String prefix = selectorMatcher.group(1);
              List<String> list = new ArrayList<>();
              switch (prefix) {
              case "#":
                list.add(Element.SELECTOR_ID);
                break;
              case ".":
                list.add(Element.SELECTOR_CLASS);
                break;
              case ":":
                list.add(Element.SELECTOR_STATE);
                break;
              case " ":
              case "\n":
                list.add(Element.SELECTOR_JAVACLASS);
                break;
              }
              spansBuilder.add(list, selectorMatcher.end(2) - selectorMatcher.start(2));
              lastKwEnd = selectorMatcher.end();
            }
            if (selectorText.length() > lastKwEnd) {
              spansBuilder.add(Collections.emptyList(), selectorText.length() - lastKwEnd);
            }
          }
          spansBuilder.add(Collections.singleton(Element.LEFT_BRACE),
              cssMatcher.end(GROUP_LEFT_BRACE) - cssMatcher.start(GROUP_LEFT_BRACE));
          String entriesText = cssMatcher.group(GROUP_VALUES);
          if (!entriesText.isEmpty()) {
            lastKwEnd = 0;
            Matcher entryMatcher = ENTRY_PATTERN.matcher(entriesText);
            while (entryMatcher.find()) {
              spansBuilder.add(Collections.emptyList(), entryMatcher.start() - lastKwEnd);
              if (entryMatcher.group(Element.MUTI_COMMENT) != null) {
                spansBuilder
                    .add(Collections.singleton(Element.MUTI_COMMENT), entryMatcher.end() - entryMatcher.start());
              } else {
                if (entryMatcher.group(Element.ENTRY) != null) {
                  spansBuilder.add(Collections.singleton(Element.KEY),
                      entryMatcher.end(GROUP_KEY) - entryMatcher.start(GROUP_KEY));
                  spansBuilder.add(Collections.singleton(Element.COLON),
                      entryMatcher.end(GROPU_COLON) - entryMatcher.start(GROPU_COLON));
                  spansBuilder.add(Collections.singleton(Element.VALUE),
                      entryMatcher.end(GROUP_VALUE) - entryMatcher.start(GROUP_VALUE));
                  spansBuilder.add(Collections.singleton(Element.SEMICOLON),
                      entryMatcher.end(GROUP_SEMICOLON) - entryMatcher.start(GROUP_SEMICOLON));
                }
              }
              lastKwEnd = entryMatcher.end();
            }
            if (entriesText.length() > lastKwEnd) {
              spansBuilder.add(Collections.emptyList(), entriesText.length() - lastKwEnd);
            }
          }

          lastKwEnd = cssMatcher.end(GROUP_VALUES);

          spansBuilder.add(Collections.singleton(Element.RIGHT_BRACE), cssMatcher.end(GROUP_RIGHT_BRACE) - lastKwEnd);
        }
      }
      lastKwEnd = cssMatcher.end();
    }
    spansBuilder.add(Collections.emptyList(), text.length() - lastKwEnd);
    return spansBuilder.create();
  }
}
