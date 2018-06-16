package xdean.css.editor.control;

import org.fxmisc.richtext.CodeArea;

import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;
import xdean.css.editor.model.CSSContext;

public class CssCodeArea extends CodeArea {
  public final CSSContext context = CSSContext.createByDefault();

  public enum Action {
    SUGGEST,
    FORMAT,
    COMMENT,
    FIND,
    CLOSE;

    public Subject<CssCodeArea> subject = PublishSubject.create();
  }
}
