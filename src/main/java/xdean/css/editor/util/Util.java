package xdean.css.editor.util;

import com.sun.javafx.tk.Toolkit;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.text.Font;
import lombok.extern.slf4j.Slf4j;
import xdean.css.editor.config.Options;

@Slf4j
public class Util {
  public static void printAllWithId(Node n, int space) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < space; i++) {
      sb.append("-");
    }
    sb.append(String.format("id:%s  %s", n.getId(), n));
    log.debug(sb.toString());
    if (n instanceof Parent) {
      ((Parent) n).getChildrenUnmodifiable().forEach(node -> {
        printAllWithId(node, space + 1);
      });
    }
  }

  public static double getTextSize(String text) {
    return Toolkit.getToolkit().getFontLoader().computeStringWidth(text,
        Font.font(Options.fontFamily.get(), Options.fontSize.get()));
  }
}
