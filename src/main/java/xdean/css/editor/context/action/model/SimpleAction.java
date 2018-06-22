package xdean.css.editor.context.action.model;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

public class SimpleAction<T> implements Action<T> {

  private final BooleanProperty enable = new SimpleBooleanProperty(this, "enable");
  private final Subject<T> subject = PublishSubject.create();
  private final String key;

  public SimpleAction(String key) {
    this.key = key;
  }

  @Override
  public Observable<T> consumer() {
    return subject;
  }

  @Override
  public Observer<T> producer() {
    return new Observer<T>() {
      @Override
      public void onSubscribe(Disposable d) {
        subject.onSubscribe(d);
      }

      @Override
      public void onNext(T t) {
        if (enable.get()) {
          subject.onNext(t);
        }
      }

      @Override
      public void onError(Throwable e) {
        subject.onError(e);
      }

      @Override
      public void onComplete() {
        subject.onComplete();
      }
    };
  }

  @Override
  public BooleanProperty enable() {
    return enable;
  }

  @Override
  public String key() {
    return key;
  }
}
