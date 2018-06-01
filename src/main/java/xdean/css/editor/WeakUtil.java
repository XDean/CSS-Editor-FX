package xdean.css.editor;

import java.lang.ref.WeakReference;
import java.util.function.Consumer;

public class WeakUtil {
  public static <T> Runnable weak(T t, Consumer<T> con) {
    Weak<T> weak = new Weak<>(t);
    return () -> weak.doIfPresent(con);
  }

  private static class Weak<T> extends WeakReference<T> {
    Weak(T referent) {
      super(referent);
    }

    void doIfPresent(Consumer<T> consumer) {
      T t = get();
      if (t != null) {
        consumer.accept(t);
      }
    }
  }
}
