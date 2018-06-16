package xdean.css.editor.feature.suggestion;

import java.util.Collection;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import xdean.css.editor.model.CSSContext;
import xdean.jex.util.string.StringUtil;

public class RegexSuggestion extends SimpleSuggestion {
  private static Pattern pattern = Pattern.compile("([^a-zA-Z0-9-_]*)([a-zA-Z0-9-_]+)");

  @Inject
  CssSuggestionsFilter filter;

  @Override
  public Collection<String> getSuggestion(String text, int caretPos, CSSContext context) {
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
          return filter.filter(context.getKeys(), keyWord);
        }
      } else {
        if (lastMatch1.indexOf('.') > -1) {
          return filter.filter(context.getClasses(), keyWord);
        } else if (lastMatch1.indexOf('#') > -1) {
          return filter.filter(context.getIds(), keyWord);
        } else if (lastMatch1.indexOf(':') > -1) {
          return filter.filter(context.getStates(), keyWord);
        } else {
          return filter.filter(context.getJavaClasses(), keyWord);
        }
      }
    }
    return Collections.emptyList();
  }
}
