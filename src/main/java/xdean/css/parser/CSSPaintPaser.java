package xdean.css.parser;

import static xdean.jex.util.lang.ExceptionUtil.uncatch;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import com.sun.javafx.css.Stylesheet;
import com.sun.javafx.css.converters.PaintConverter.LinearGradientConverter;
import com.sun.javafx.css.converters.PaintConverter.RadialGradientConverter;
import com.sun.javafx.css.parser.CSSParser;
import com.sun.javafx.css.parser.DeriveColorConverter;
import com.sun.javafx.css.parser.LadderConverter;

import io.reactivex.Observable;
import javafx.css.ParsedValue;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Paint;
import javafx.scene.paint.RadialGradient;
import javafx.scene.text.Font;
import xdean.css.context.CSSContext;
import xdean.jex.util.cache.CacheUtil;
import xdean.jex.util.task.TaskUtil;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class CSSPaintPaser {

  private final CSSContext context;
  private final Node inlineNode;
  private final Function<String, ParsedValue> factory;

  public CSSPaintPaser(CSSContext context) {
    this.context = context;
    this.inlineNode = new Group();
    this.factory = text -> context.resolve(CacheUtil.cache(this, text, () -> getParsedValue(text)));
  }

  public List<Function<String, Paint>> getTasks() {
    return CacheUtil.cache(CSSPaintPaser.class, () -> Arrays.asList(
        text -> Color.web(text, 1),
        text -> LinearGradient.valueOf(text),
        text -> RadialGradient.valueOf(text),
        text -> context.lookup(text),
        text -> DeriveColorConverter.getInstance().convert(factory.apply(text), Font.getDefault()),
        text -> LadderConverter.getInstance().convert(factory.apply(text), Font.getDefault()),
        text -> LinearGradientConverter.getInstance().convert(factory.apply(text), Font.getDefault()),
        text -> RadialGradientConverter.getInstance().convert(factory.apply(text), Font.getDefault())));
  }

  public Optional<Color> parseColor(String text) {
    return Optional.ofNullable(TaskUtil.firstSuccess(
        () -> Color.web(text, 1),
        () -> DeriveColorConverter.getInstance().convert(factory.apply(text), Font.getDefault()),
        () -> LadderConverter.getInstance().convert(factory.apply(text), Font.getDefault()),
        () -> context.lookup(text)));
  }

  public Optional<Paint> parsePaint(String text) {
    return Observable.fromIterable(getTasks())
        .map(f -> uncatch(() -> f.apply(text)))
        .filter(p -> p != null)
        .first(null)
        .map(Optional::ofNullable)
        .blockingGet();
  }

  private ParsedValue<?, ?> getParsedValue(String text) {
    inlineNode.setStyle("-fx-color:" + text);
    Stylesheet css = new CSSParser().parseInlineStyle(inlineNode);
    ParsedValue parsedValue = uncatch(() -> css.getRules().get(0).getDeclarations().get(0).getParsedValue());
    return parsedValue;
  }
}
