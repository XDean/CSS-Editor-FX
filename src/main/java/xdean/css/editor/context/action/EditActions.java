package xdean.css.editor.context.action;

import static xdean.css.editor.context.action.ActionKeys.*;

import javax.inject.Inject;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import xdean.css.editor.context.action.model.CssEditorKeyAction;
import xdean.css.editor.context.setting.KeySettings;

@Configuration
public class EditActions {

  @Inject
  KeySettings keys;

  @Bean(Edit.UNDO)
  public CssEditorKeyAction undo() {
    return new CssEditorKeyAction(Edit.UNDO, keys.undo());
  }

  @Bean(Edit.REDO)
  public CssEditorKeyAction redo() {
    return new CssEditorKeyAction(Edit.REDO, keys.redo());
  }

  @Bean(Edit.SUGGEST)
  public CssEditorKeyAction suggest() {
    return new CssEditorKeyAction(Edit.SUGGEST, keys.suggest());
  }

  @Bean(Edit.FORMAT)
  public CssEditorKeyAction format() {
    return new CssEditorKeyAction(Edit.FORMAT, keys.format());
  }

  @Bean(Edit.COMMENT)
  public CssEditorKeyAction comment() {
    return new CssEditorKeyAction(Edit.COMMENT, keys.comment());
  }

  @Bean(Edit.FIND)
  public CssEditorKeyAction find() {
    return new CssEditorKeyAction(Edit.FIND, keys.find());
  }
}
