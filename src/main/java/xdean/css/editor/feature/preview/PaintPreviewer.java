package xdean.css.editor.feature.preview;

import static xdean.jex.util.lang.ExceptionUtil.uncatch;

import java.util.function.Function;

import org.springframework.stereotype.Service;

import com.sun.javafx.css.Stylesheet;
import com.sun.javafx.css.converters.PaintConverter.LinearGradientConverter;
import com.sun.javafx.css.converters.PaintConverter.RadialGradientConverter;
import com.sun.javafx.css.parser.CSSParser;
import com.sun.javafx.css.parser.DeriveColorConverter;
import com.sun.javafx.css.parser.LadderConverter;

import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import javafx.css.ParsedValue;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Paint;
import javafx.scene.paint.RadialGradient;
import javafx.scene.text.Font;
import javafx.scene.transform.Affine;
import xdean.css.editor.model.CssContext;

@Service
@SuppressWarnings({ "rawtypes", "unchecked" })
public class PaintPreviewer implements CssElementPreviewer<Paint> {

  private final Node inlineNode = new Group();

  @Override
  public Maybe<Paint> parse(CssContext context, String str) {
    return Observable.<Function<String, Paint>> just(
        text -> Color.web(text, 1),
        text -> LinearGradient.valueOf(text),
        text -> RadialGradient.valueOf(text),
        text -> context.lookup(text),
        text -> DeriveColorConverter.getInstance().convert(resolve(context, text), Font.getDefault()),
        text -> LadderConverter.getInstance().convert(resolve(context, text), Font.getDefault()),
        text -> LinearGradientConverter.getInstance().convert(resolve(context, text), Font.getDefault()),
        text -> RadialGradientConverter.getInstance().convert(resolve(context, text), Font.getDefault()))
        .observeOn(Schedulers.computation())
        .flatMapMaybe(t -> Maybe.fromCallable(() -> uncatch(() -> t.apply(str))).onErrorComplete())
        .firstElement();
  }

  @Override
  public void preview(GraphicsContext graphics, Paint paint, double width, double height) {
    graphics.setTransform(new Affine());
    graphics.setFill(paint);
    graphics.fillRect(0, 0, width, height);
  }

  private ParsedValue resolve(CssContext context, String text) {
    inlineNode.setStyle("-fx-color:" + text);
    Stylesheet css = new CSSParser().parseInlineStyle(inlineNode);
    ParsedValue parsedValue = uncatch(() -> css.getRules().get(0).getDeclarations().get(0).getParsedValue());
    return context.resolve(parsedValue);
  }
}
