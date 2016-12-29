package xdean.css.parser;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import javafx.css.ParsedValue;
import javafx.scene.layout.Border;
import javafx.scene.layout.Region;
import xdean.jex.util.cache.CacheUtil;

@SuppressWarnings("unused")
public class CSSBorderPaser {

  private final Region inlineNode = new Region();

  public List<Function<String, Border>> getTasks() {
    return CacheUtil.cache(CSSPaintPaser.class, () -> Arrays.asList(

        ));
  }

  @SuppressWarnings("rawtypes")
  private ParsedValue getParsedValue(String text) {
    return null;
  }
}
