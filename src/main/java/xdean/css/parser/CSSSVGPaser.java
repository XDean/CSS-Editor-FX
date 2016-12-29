package xdean.css.parser;

import xdean.jex.util.string.StringUtil;
import xdean.jex.util.task.TaskUtil;

import com.sun.javafx.geom.Path2D;

public class CSSSVGPaser {

  public static boolean verify(String svg) {
    if (StringUtil.isEmpty(svg)) {
      return false;
    }
    return TaskUtil.uncatch(() -> new Path2D().appendSVGPath(svg));
  }
}
