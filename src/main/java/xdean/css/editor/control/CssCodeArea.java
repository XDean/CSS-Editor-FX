package xdean.css.editor.control;

import org.fxmisc.richtext.CodeArea;

import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;
import xdean.css.editor.model.CssContext;

public class CssCodeArea extends CodeArea {
  public final CssContext context = CssContext.createByDefault();

  public enum Action {
    SUGGEST,
    FORMAT,
    COMMENT,
    FIND,
    CLOSE;

    public Subject<CssCodeArea> subject = PublishSubject.create();
  }
}
