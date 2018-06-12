package xdean.css.editor.feature.highlight;

import static xdean.jex.util.lang.ExceptionUtil.uncatch;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;
import com.google.common.collect.TreeRangeMap;

import xdean.jex.util.string.StringUtil;

public class SimpleHighLight implements CssHighLight {

  @Override
  public StyleSpans<Collection<String>> compute(String text) {
    StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();
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
    Range<Integer> comment = uncatch(() -> iterator.next());
    Range<Integer> css = findCSS(text, offset);
    while (offset < text.length()) {
      if (css == null) {
        break;
      }
      while (comment != null && comment.upperEndpoint() < css.lowerEndpoint()) {
        comment = uncatch(() -> iterator.next());
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
            comment = uncatch(() -> iterator.next());
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

  private Range<Integer> findComment(String text, int offset) {
    int start = text.indexOf("/*", offset);
    int end = text.indexOf("*/", start);
    if (start == -1 || end == -1) {
      return null;
    }
    return Range.closed(start, end + 2);
  }

  private Range<Integer> findCSS(String text, int offset) {
    int start = text.indexOf("{", offset);
    int end = text.indexOf("}", start);
    if (start > end || start == -1 || end == -1) {
      return null;
    }
    start = offset + StringUtil.lastIndexOf(text.substring(offset, start), "{", "}") + 1;
    return Range.closed(start, end + 1);
  }
}
