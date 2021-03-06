package xdean.css.editor.feature.suggestion;

import java.util.Collection;

import javafx.scene.control.IndexRange;
import xdean.css.editor.model.CssContext;

public interface CssSuggestionService {
  Collection<String> getSuggestion(String text, int caretPos, CssContext context);

  IndexRange getReplaceRange(String text, int caretPos, CssContext context);
}
