package xdean.css.editor.control;

import org.fxmisc.richtext.CodeArea;

import xdean.css.context.CSSContext;

public class CssCodeArea extends CodeArea {
  public final CSSContext context = CSSContext.createByDefault();
}
