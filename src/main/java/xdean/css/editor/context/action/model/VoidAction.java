package xdean.css.editor.context.action.model;

public class VoidAction extends SimpleAction<VoidAction> {

  public VoidAction(String key) {
    super(key);
  }

  public void onAction() {
    super.onAction(this);
  }

}
