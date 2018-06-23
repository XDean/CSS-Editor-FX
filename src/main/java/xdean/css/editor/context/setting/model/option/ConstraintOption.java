package xdean.css.editor.context.setting.model.option;

public interface ConstraintOption<T> extends Option<T> {
  boolean isValid(T t);

  @Override
  public default void setValue(T t) {
    if (isValid(t)) {
      valueProperty().setValue(t);
    }
  }
}
