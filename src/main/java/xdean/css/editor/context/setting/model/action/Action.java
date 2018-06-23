package xdean.css.editor.context.setting.model.action;

import io.reactivex.Observable;
import io.reactivex.Observer;
import javafx.beans.property.BooleanProperty;

public interface Action<T> {

  Observable<T> consumer();

  Observer<T> producer();

  BooleanProperty enable();

  String getKey();

  default void onAction(T t) {
    producer().onNext(t);
  }
}
