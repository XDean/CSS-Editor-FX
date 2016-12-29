package xdean.css.editor.config.option;

public interface ConstraintOption<T> extends Option<T> {
  boolean isValid(T t);
}
