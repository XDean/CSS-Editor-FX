package xdean.css.editor.feature.preview;

import static xdean.jex.util.lang.ExceptionUtil.uncatch;

import org.springframework.stereotype.Service;

import com.sun.javafx.geom.Path2D;

import io.reactivex.Maybe;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.scene.transform.Affine;
import xdean.css.context.CSSContext;
import xdean.jex.util.string.StringUtil;

@Service
public class SvgPreviewer implements CssElementPreviewer<String> {

  public static boolean verify(String svg) {
    if (StringUtil.isEmpty(svg)) {
      return false;
    }
    return uncatch(() -> new Path2D().appendSVGPath(svg));
  }

  @Override
  public Maybe<String> parse(CSSContext context, String svg) {
    svg = StringUtil.unWrap(svg, "\"", "\"");
    try {
      new Path2D().appendSVGPath(svg);
    } catch (Exception e) {
      return Maybe.empty();
    }
    return Maybe.just(svg);
  }

  @Override
  public void preview(GraphicsContext graphics, String svg, double width, double height) {
    SVGPath svgPath = new SVGPath();
    svgPath.setContent(svg);
    double scaleRate = Math.min(width / svgPath.getBoundsInLocal().getWidth(), height / svgPath.getBoundsInLocal().getHeight());

    graphics.setTransform(new Affine(Affine.scale(scaleRate, scaleRate)));
    graphics.setFill(Color.BLACK);
    graphics.beginPath();
    graphics.appendSVGPath(svg);
    graphics.closePath();
    graphics.fill();
  }
}
