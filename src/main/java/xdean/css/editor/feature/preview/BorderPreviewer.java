package xdean.css.editor.feature.preview;

import io.reactivex.Maybe;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Border;
import xdean.css.context.CSSContext;

// TODO
// @Service
public class BorderPreviewer implements CssElementPreviewer<Border> {

  @Override
  public Maybe<Border> parse(CSSContext context, String text) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void preview(GraphicsContext gc, Border value, double width, double height) {
    // TODO Auto-generated method stub

  }
}
