package xdean.css.editor.feature;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

import xdean.jex.util.string.StringUtil;
import xdean.jex.util.task.TaskUtil;

import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;
import com.google.common.collect.TreeRangeMap;

/**
 * Remind to set -Xss1m to VM for large file if use regex
 * 
 * @author XDean
 *
 */
public class CSSHighLight {
  public interface Element {
    String CSS = "css";
    String SELECTOR = "selector";
    String SELECTOR_ID = "selector-id";
    String SELECTOR_CLASS = "selector-class";
    String SELECTOR_STATE = "selector-state";
    String SELECTOR_JAVACLASS = "selector-java-class";
    String ENTRIES = "entries";
    String KEY = "key";
    String VALUE = "value";
    String MUTI_COMMENT = "muticomment";
    String ENTRY = "entry";
    String LEFT_BRACE = "leftbrace";
    String RIGHT_BRACE = "rightbrace";
    String COLON = "colon";
    String SEMICOLON = "semicolon";
  }

  public static StyleSpans<Collection<String>> computeHighlighting(String text) {
    // return computeHighlightingSimply(text);
    return computeHighlightingByRegex(text);
  }

  public static StyleSpans<Collection<String>> computeHighlightingSimply(String text) {
    StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<Collection<String>>();
    RangeMap<Integer, Collection<String>> map = TreeRangeMap.create();
    int offset = 0;
    while (offset < text.length()) {
      Range<Integer> comment = findComment(text, offset);
      if (comment == null) {
        break;
      }
      map.put(Range.closed(comment.lowerEndpoint(), comment.upperEndpoint()), Collections.singleton(Element.MUTI_COMMENT));
      offset = comment.upperEndpoint();
    }
    offset = 0;
    Iterator<Range<Integer>> iterator = new ArrayList<>(map.asMapOfRanges().keySet()).iterator();
    Range<Integer> comment = TaskUtil.uncatch(() -> iterator.next());
    Range<Integer> css = findCSS(text, offset);
    while (offset < text.length()) {
      if (css == null) {
        break;
      }
      while (comment != null && comment.upperEndpoint() < css.lowerEndpoint()) {
        comment = TaskUtil.uncatch(() -> iterator.next());
      }
      if (comment != null) {
        if (comment.contains(css.lowerEndpoint())) {
          // [COMMEN{CST]S}
          offset = comment.upperEndpoint() + 1;
          css = findCSS(text, offset);
          continue;
        }
        if (comment.contains(css.upperEndpoint())) {
          // {CS[CS}OMMENT]
          int newEnd = text.indexOf('}', comment.upperEndpoint());
          if (newEnd == -1) {
            break;
          }
          css = Range.closed(css.lowerEndpoint(), newEnd + 1);
          continue;
        }
        if (css.encloses(comment)) {
          // {C[COMMENT]S[COMMES}NT]
          if (map.get(css.upperEndpoint()) != null) {
            int newEnd = text.indexOf('}', map.getEntry(css.upperEndpoint()).getKey().upperEndpoint());
            if (newEnd == -1) {
              break;
            }
            css = Range.closed(css.lowerEndpoint(), newEnd + 1);
            continue;
          }
          // {C[COMMENT]S[COMMENT]S}
          int lower = css.lowerEndpoint();
          while (comment != null && comment.upperEndpoint() < css.upperEndpoint()) {
            map.put(Range.closed(lower, comment.lowerEndpoint()), Collections.singleton(Element.SELECTOR_CLASS));
            lower = comment.upperEndpoint();
            comment = TaskUtil.uncatch(() -> iterator.next());
          }
          map.put(Range.closed(lower, css.upperEndpoint()), Collections.singleton(Element.SELECTOR_CLASS));
        } else {
          // {CSS}[COMMENT]
          map.put(css, Collections.singleton(Element.SELECTOR_CLASS));
        }
      } else {
        // {CSS} no comment
        map.put(css, Collections.singleton(Element.SELECTOR_CLASS));
      }
      offset = css.upperEndpoint();
      css = findCSS(text, offset);
    }
    offset = 0;
    Map<Range<Integer>, Collection<String>> rangeMap = map.asMapOfRanges();
    for (Range<Integer> range : rangeMap.keySet()) {
      spansBuilder.add(Collections.emptyList(), range.lowerEndpoint() - offset);
      spansBuilder.add(rangeMap.get(range), range.upperEndpoint() - range.lowerEndpoint());
      offset = range.upperEndpoint();
    }
    spansBuilder.add(Collections.emptyList(), text.length() - offset);
    return spansBuilder.create();
  }

  private static Range<Integer> findComment(String text, int offset) {
    int start = text.indexOf("/*", offset);
    int end = text.indexOf("*/", start);
    if (start == -1 || end == -1) {
      return null;
    }
    return Range.closed(start, end + 2);
  }

  private static Range<Integer> findCSS(String text, int offset) {
    int start = text.indexOf("{", offset);
    int end = text.indexOf("}", start);
    if (start > end || start == -1 || end == -1) {
      return null;
    }
    start = offset + StringUtil.lastIndexOf(text.substring(offset, start), "{", "}") + 1;
    return Range.closed(start, end + 1);
  }

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

  public static StyleSpans<Collection<String>> computeHighlightingByRegex(String text) {
    StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<Collection<String>>();
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
