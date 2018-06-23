package xdean.css.editor.context.action.model;

import io.reactivex.rxjavafx.observables.JavaFxObservable;
import javafx.scene.input.KeyEvent;
import xdean.css.editor.context.setting.model.KeyOption;
import xdean.css.editor.control.CssEditor;

public class CssEditorKeyAction extends SimpleAction<CssEditor> {

  private final KeyOption option;

  public CssEditorKeyAction(String key, KeyOption option) {
    super(key);
    this.option = option;
  }

  public void bind(CssEditor editor) {
    JavaFxObservable.eventsOf(editor, KeyEvent.KEY_PRESSED)
        .filter(e -> e.isConsumed() == false)
        .filter(option.getValue()::match)
        .doOnNext(KeyEvent::consume)
        .subscribe(e -> producer().onNext(editor));
  }

  public KeyOption getOption() {
    return option;
  }
}
