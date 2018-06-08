package xdean.css.editor.feature.suggestion;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CssSuggestionConfig {

  @Bean
  public CssSuggestionService service() {
    return new SimpleSuggestion();
  }

  @Bean
  public CssSuggestionFilter filterByEqual() {
    return CssSuggestionFilter.create(0, (suggestion, input) -> suggestion.equalsIgnoreCase(input));
  }

  @Bean
  public CssSuggestionFilter filterByStart() {
    return CssSuggestionFilter.create(1, (suggestion, input) -> suggestion.toLowerCase().startsWith(input));
  }

  @Bean
  public CssSuggestionFilter filterByContain() {
    return CssSuggestionFilter.create(2, (suggestion, input) -> suggestion.toLowerCase().contains(input));
  }

  @Bean
  public CssSuggestionFilter filterByInclude() {
    return CssSuggestionFilter.create(3, (suggestion, input) -> {
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
    });
  }
}
