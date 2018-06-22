package xdean.css.editor.control;

import org.fxmisc.richtext.CodeArea;

import xdean.css.editor.model.CssContext;

public class CssCodeArea extends CodeArea {
  public final CssContext context = CssContext.createByDefault();
}
