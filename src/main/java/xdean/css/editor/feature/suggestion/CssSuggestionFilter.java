package xdean.css.editor.feature.suggestion;

import java.util.function.BiPredicate;

import org.springframework.core.Ordered;

/**
 * Suggestion Filter. High order filter will be first execute and the accepted suggestion also has
 * high order.
 * 
 * 
 * @author Dean Xu (XDean@github.com)
 */
public interface CssSuggestionFilter extends Ordered {
  /**
   * Filter the suggestion.
   * 
   * @param suggestion the candidate suggestion
   * @param input the input text
   * @return return true to accept the suggestion, return false to skip the suggestion (to next
   *         suggestion).
   */
  boolean filter(String suggestion, String input);

  static CssSuggestionFilter create(int order, BiPredicate<String, String> filter) {
    return new CssSuggestionFilter() {
      @Override
      public int getOrder() {
        return order;
      }

      @Override
      public boolean filter(String suggestion, String input) {
        return filter.test(suggestion, input);
      }
    };
  }
}
