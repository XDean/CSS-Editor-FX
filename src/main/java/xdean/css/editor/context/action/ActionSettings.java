package xdean.css.editor.context.action;

import static xdean.css.editor.context.action.ActionKeys.CLOSE;
import static xdean.css.editor.context.action.ActionKeys.COMMENT;
import static xdean.css.editor.context.action.ActionKeys.FIND;
import static xdean.css.editor.context.action.ActionKeys.FORMAT;
import static xdean.css.editor.context.action.ActionKeys.SUGGEST;

import javax.inject.Inject;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import xdean.css.editor.context.action.model.CssCodeAreaKeyAction;
import xdean.css.editor.context.setting.KeySettings;

@Configuration
public class ActionSettings {

  @Inject
  KeySettings keys;

  @Bean(SUGGEST)
  public CssCodeAreaKeyAction suggest() {
    return new CssCodeAreaKeyAction(SUGGEST, keys.suggest());
  }

  @Bean(FORMAT)
  public CssCodeAreaKeyAction format() {
    return new CssCodeAreaKeyAction(FORMAT, keys.format());
  }

  @Bean(COMMENT)
  public CssCodeAreaKeyAction comment() {
    return new CssCodeAreaKeyAction(COMMENT, keys.comment());
  }

  @Bean(FIND)
  public CssCodeAreaKeyAction find() {
    return new CssCodeAreaKeyAction(FIND, keys.find());
  }

  @Bean(CLOSE)
  public CssCodeAreaKeyAction close() {
    return new CssCodeAreaKeyAction(CLOSE, keys.close());
  }
}
