package xdean.css.parser;

import static xdean.jex.util.lang.ExceptionUtil.uncatch;

import com.sun.javafx.geom.Path2D;

import xdean.jex.util.string.StringUtil;

public class CSSSVGPaser {

  public static boolean verify(String svg) {
    if (StringUtil.isEmpty(svg)) {
      return false;
    }
    return uncatch(() -> new Path2D().appendSVGPath(svg));
  }
}
