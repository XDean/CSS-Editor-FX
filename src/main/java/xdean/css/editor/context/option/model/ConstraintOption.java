package xdean.css.editor.context.option.model;

public interface ConstraintOption<T> extends Option<T> {
  boolean isValid(T t);
}
