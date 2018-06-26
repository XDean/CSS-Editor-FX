package xdean.css.editor.controller;

import static xdean.jex.util.lang.ExceptionUtil.uncatch;
import static xdean.jfxex.event.EventHandlers.consume;

import javax.inject.Inject;

import org.springframework.util.Assert;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.css.PseudoClass;
import javafx.scene.control.Tab;
import xdean.css.editor.context.setting.FileActions;
import xdean.css.editor.control.CssEditor;
import xdean.jex.util.cache.CacheUtil;
import xdean.jex.util.task.If;
import xdean.jfx.spring.annotation.FxNode;

@FxNode
public class CssEditorTab extends Tab {

  public static CssEditorTab get(CssEditor editor) {
    return (CssEditorTab) editor.getProperties().get(CssEditorTab.class);
  }

  private @Inject FileActions fileActions;
  private CssEditor editor;

  public CssEditorTab bind(CssEditor editor) {
    Assert.isNull(this.editor, "Can't bind twice.");
    this.editor = editor;
    editor.getProperties().put(CssEditorTab.class, this);

    setOnCloseRequest(consume(e -> editor.fire(fileActions.close())));

    this.tabPaneProperty().addListener((ob, o, n) -> {
      if (n != null) {
        editor.activeProperty().softBind(n.getSelectionModel().selectedItemProperty().isEqualTo(this));
        editor.activeProperty().addListener((obe, oe, ne) -> If.that(ne).todo(() -> n.getSelectionModel().select(this)));
      }
    });

    textProperty().bind(editor.nameBinding());
    // graphics
    FontAwesomeIconView icon = new FontAwesomeIconView();
    setGraphic(icon);
    icon.setStyleClass("tab-icon");
    icon.setIcon(FontAwesomeIcon.SAVE);
    // Hold the object in cache to avoid gc
    StringBinding state = CacheUtil.cache(this, "state",
        () -> Bindings.when(editor.activeProperty().normalize())
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
    return this;
  }

  public CssEditor getEditor() {
    return editor;
  }
}