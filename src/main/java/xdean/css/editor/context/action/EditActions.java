package xdean.css.editor.context.action;

import static xdean.css.editor.context.action.ActionKeys.*;

import javax.inject.Inject;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import xdean.css.editor.context.action.model.CssCodeAreaKeyAction;
import xdean.css.editor.context.setting.KeySettings;

@Configuration
public class EditActions {

  @Inject
  KeySettings keys;

  @Bean(Edit.UNDO)
  public CssCodeAreaKeyAction undo() {
    return new CssCodeAreaKeyAction(Edit.UNDO, keys.undo());
  }

  @Bean(Edit.REDO)
  public CssCodeAreaKeyAction redo() {
    return new CssCodeAreaKeyAction(Edit.REDO, keys.redo());
  }

  @Bean(Edit.SUGGEST)
  public CssCodeAreaKeyAction suggest() {
    return new CssCodeAreaKeyAction(Edit.SUGGEST, keys.suggest());
  }

  @Bean(Edit.FORMAT)
  public CssCodeAreaKeyAction format() {
    return new CssCodeAreaKeyAction(Edit.FORMAT, keys.format());
  }

  @Bean(Edit.COMMENT)
  public CssCodeAreaKeyAction comment() {
    return new CssCodeAreaKeyAction(Edit.COMMENT, keys.comment());
  }

  @Bean(Edit.FIND)
  public CssCodeAreaKeyAction find() {
    return new CssCodeAreaKeyAction(Edit.FIND, keys.find());
  }
}
