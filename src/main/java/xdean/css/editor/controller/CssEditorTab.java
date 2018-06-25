package xdean.css.editor.controller;

import static xdean.jex.util.lang.ExceptionUtil.uncatch;

import javax.inject.Inject;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.css.PseudoClass;
import javafx.scene.control.Tab;
import xdean.css.editor.control.CssEditor;
import xdean.jex.util.cache.CacheUtil;
import xdean.jfx.spring.FxInitializable;
import xdean.jfx.spring.annotation.FxNode;

@FxNode
public class CssEditorTab extends Tab implements FxInitializable {

  private @Inject CssEditor editor;

  @Override
  public void initAfterFxSpringReady() {
    textProperty().bind(editor.nameBinding());
    // graphics
    FontAwesomeIconView icon = new FontAwesomeIconView();
    setGraphic(icon);
    icon.setStyleClass("tab-icon");
    icon.setIcon(FontAwesomeIcon.SAVE);
    // Hold the object in cache to avoid gc
    StringBinding state = CacheUtil.cache(this, "state",
        () -> Bindings.when(editor.activeProperty())
            .then(Bindings.when(editor.modifiedProperty())
                .then("selected-modified")
                .otherwise("selected"))
            .otherwise(Bindings.when(editor.modifiedProperty())
                .then("modified")
                .otherwise("")));
    state.addListener((ob, o, n) -> {
      uncatch(() -> icon.pseudoClassStateChanged(PseudoClass.getPseudoClass(o), false));
      uncatch(() -> icon.pseudoClassStateChanged(PseudoClass.getPseudoClass(n), true));
    });

    setContent(editor);
  }

  public CssEditor getEditor() {
    return editor;
  }
}