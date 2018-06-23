package xdean.css.editor.context.action;

import java.nio.file.Path;

import javax.inject.Inject;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import xdean.css.editor.context.action.ActionKeys.File;
import xdean.css.editor.context.action.model.Action;
import xdean.css.editor.context.action.model.CssCodeAreaKeyAction;
import xdean.css.editor.context.action.model.SimpleAction;
import xdean.css.editor.context.action.model.VoidAction;
import xdean.css.editor.context.setting.KeySettings;

@Configuration
public class FileActions {

  @Inject
  KeySettings keys;

  @Bean(File.NEW)
  public VoidAction newFile() {
    return new VoidAction(File.NEW);
  }

  @Bean(File.OPEN)
  public Action<Path> open() {
    return new SimpleAction<>(File.OPEN);
  }

  @Bean(File.SAVE)
  public VoidAction save() {
    return new VoidAction(File.SAVE);
  }

  @Bean(File.SAVE_AS)
  public CssCodeAreaKeyAction saveAs() {
    return new CssCodeAreaKeyAction(File.SAVE_AS, keys.close());
  }

  @Bean(File.CLOSE)
  public CssCodeAreaKeyAction close() {
    return new CssCodeAreaKeyAction(File.CLOSE, keys.close());
  }
}
