package xdean.css.editor.feature.preview;

import io.reactivex.Maybe;
import javafx.scene.canvas.GraphicsContext;
import xdean.css.editor.model.CSSContext;

public interface CssElementPreviewer<T> {
  Maybe<T> parse(CSSContext context, String text);

  void preview(GraphicsContext gc, T value, double width, double height);
}
