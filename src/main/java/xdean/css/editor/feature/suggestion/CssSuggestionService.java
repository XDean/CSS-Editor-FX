package xdean.css.editor.feature.suggestion;

import java.util.Collection;

import javafx.scene.control.IndexRange;
import xdean.css.context.CSSContext;

public interface CssSuggestionService {
  Collection<String> getSuggestion(String text, int caretPos, CSSContext context);

  IndexRange getReplaceRange(String text, int caretPos, CSSContext context);
}
