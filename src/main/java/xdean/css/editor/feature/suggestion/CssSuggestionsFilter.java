package xdean.css.editor.feature.suggestion;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import io.reactivex.Observable;

@Service
public class CssSuggestionsFilter {
  @Inject
  Collection<CssSuggestionFilter> filters;

  public Collection<String> filter(Collection<String> suggestions,
      String keyWord) {
    String input = keyWord.toLowerCase();// XXX lower can be option
    List<String> list = new ArrayList<>();
    Observable.fromIterable(suggestions)
        .distinct()
        .groupBy(s -> filters.stream()
            .filter(f -> f.filter(s, input))
            .findFirst())
        .filter(g -> !g.getKey().isPresent())
        .sorted((g1, g2) -> g1.getKey().get().getOrder() - g2.getKey().get().getOrder())
        .subscribe(g -> g.forEach(list::add));
    return list;
  }
}
