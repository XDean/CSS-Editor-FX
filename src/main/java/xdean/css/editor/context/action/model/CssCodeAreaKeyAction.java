package xdean.css.editor.context.action.model;

import io.reactivex.rxjavafx.observables.JavaFxObservable;
import javafx.scene.input.KeyEvent;
import xdean.css.editor.context.setting.model.KeyOption;
import xdean.css.editor.control.CssCodeArea;

public class CssCodeAreaKeyAction extends SimpleAction<CssCodeArea> {

  private final KeyOption option;

  public CssCodeAreaKeyAction(String key, KeyOption option) {
    super(key);
    this.option = option;
  }

  public void bind(CssCodeArea codeArea) {
    JavaFxObservable.eventsOf(codeArea, KeyEvent.KEY_PRESSED)
        .filter(e -> e.isConsumed() == false)
        .filter(option.getValue()::match)
        .doOnNext(KeyEvent::consume)
        .subscribe(e -> producer().onNext(codeArea));
  }

  public KeyOption getOption() {
    return option;
  }
}
