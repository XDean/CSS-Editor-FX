package xdean.css.editor.feature;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javafx.scene.control.IndexRange;
import rx.Observable;
import xdean.css.CSSConstants;
import xdean.css.context.CSSContext;
import xdean.jex.util.string.StringUtil;

@SuppressWarnings("unused")
public class CSSSuggestion {

  public static Collection<String> getSuggestion(String text, int caretPos, CSSContext context) {
    return getSuggestionSimply(text, caretPos, context);
    // return getSuggestionByRegex(text, caretPos);
  }

  public static IndexRange getReplaceRange(String text, int caretPos) {
    return getReplaceRangeSimply(text, caretPos);
  }

  private static Collection<String> getSuggestionSimply(String text, int caretPos, CSSContext context) {
    String prePart = text.substring(0, caretPos);
    IndexRange replaceRange = getReplaceRange(text, caretPos);
    char c;
    int index = replaceRange.getStart();
    while (true) {
      index--;
      c = text.charAt(index);
      if (c != ' ' && c != '\t') {
        break;
      }
    }
    String keyWord = text.substring(replaceRange.getStart(), caretPos);

    boolean inBrace = prePart.lastIndexOf('{') > prePart.lastIndexOf('}');
    if (inBrace) {
      if (c == ':') {
        // XXX Can be smarter?
        // return filterSuggestion(CSSRepository.GLOBAL.getKeys(), keyWord);
        return Collections.emptyList();
      } else if (c == ' ' || c == '\t' || c == '\n' | c == '{') {
        return filterSuggestion(context.getKeys(), keyWord);
      } else {
        return Collections.emptyList();
      }
    } else {
      if (c == '.') {
        return filterSuggestion(context.getClasses(), keyWord);
      } else if (c == '#') {
        return filterSuggestion(context.getIds(), keyWord);
      } else if (c == ':') {
        return filterSuggestion(context.getStates(), keyWord);
      } else {
        return filterSuggestion(context.getJavaClasses(), keyWord);
      }
    }
  }

  private static Pattern pattern = Pattern.compile("([^a-zA-Z0-9-_]*)([a-zA-Z0-9-_]+)");

  private static Collection<String> getSuggestionByRegex(String text, int caretPos, CSSContext context) {
    String prePart = text.substring(0, caretPos);
    int leftIndex = prePart.lastIndexOf('{');
    int rightIndex = prePart.lastIndexOf('}');
    if (leftIndex == rightIndex) {
      return Collections.emptyList();
    }
    prePart = prePart.substring(Math.max(leftIndex, rightIndex) + 1);

    Matcher matcher = pattern.matcher(prePart);
    String lastMatch1 = "";
    String lastMatch2 = null;
    while (matcher.find()) {
      lastMatch1 = matcher.group(1);
      lastMatch2 = matcher.group(2);
    }
    final String keyWord = lastMatch2;

    if (StringUtil.notEmpty(keyWord) && prePart.endsWith(keyWord)) {
      if (leftIndex > rightIndex) {
        if (lastMatch1.indexOf(':') > -1) {
          // TODO value suggestion
        } else {
          return filterSuggestion(context.getKeys(), keyWord);
        }
      } else {
        if (lastMatch1.indexOf('.') > -1) {
          return filterSuggestion(context.getClasses(), keyWord);
        } else if (lastMatch1.indexOf('#') > -1) {
          return filterSuggestion(context.getIds(), keyWord);
        } else if (lastMatch1.indexOf(':') > -1) {
          return filterSuggestion(context.getStates(), keyWord);
        } else {
          return filterSuggestion(context.getJavaClasses(), keyWord);
        }
      }
    }
    return Collections.emptyList();
  }

  private static IndexRange getReplaceRangeSimply(String text, int caretPos) {
    int start = caretPos - 1;
    int end = caretPos;
    while (start > -1 && CSSConstants.LEGAL_CHARS.contains(text.charAt(start))) {
      start--;
    }

    while (end < text.length() && CSSConstants.LEGAL_CHARS.contains(text.charAt(end))) {
      end++;
    }

    return new IndexRange(start + 1, end);
  }

  private static Collection<String> filterSuggestion(Collection<String> suggestions, String keyWord) {
    String input = keyWord.toLowerCase();// XXX lower can be option
    List<String> list = new ArrayList<>();
    Observable.from(suggestions)
        .distinct()
        .groupBy(s -> {
          if (filterByStart.test(s, input)) {
            return 0;
          } else if (filterByContain.test(s, input)) {
            return 1;
          } else if (filterByInclude.test(s, input)) {
            return 2;
          } else {
            return -1;
          }
        })
        .filter(g -> g.getKey() >= 0)
        .sorted((g1, g2) -> g1.getKey() - g2.getKey())
        .subscribe(g -> g.forEach(list::add));
    return list;
  }

  private static BiPredicate<String, String> filterByStart = (suggestion, input) -> {
    return suggestion.toLowerCase().startsWith(input);// lower can be option
  };

  private static BiPredicate<String, String> filterByContain = (suggestion, input) -> {
    return suggestion.toLowerCase().contains(input);// lower can be option
  };

  private static BiPredicate<String, String> filterByInclude = (suggestion, input) -> {
    suggestion = suggestion.toLowerCase();// lower can be option
    int index = 0;
    for (char c : input.toCharArray()) {
      index = suggestion.indexOf(c);
      if (index == -1) {
        break;
      }
      suggestion = suggestion.substring(index);
    }
    return index != -1;
  };
}
