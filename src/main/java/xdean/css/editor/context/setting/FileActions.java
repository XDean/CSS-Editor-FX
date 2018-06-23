package xdean.css.editor.context.setting;

import java.nio.file.Path;

import javax.inject.Inject;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import xdean.css.editor.context.setting.ActionKeys.File;
import xdean.css.editor.context.setting.model.action.Action;
import xdean.css.editor.context.setting.model.action.CssEditorKeyAction;
import xdean.css.editor.context.setting.model.action.SimpleAction;
import xdean.css.editor.context.setting.model.action.VoidAction;

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
  public CssEditorKeyAction saveAs() {
    return new CssEditorKeyAction(File.SAVE_AS, keys.close());
  }

  @Bean(File.CLOSE)
  public CssEditorKeyAction close() {
    return new CssEditorKeyAction(File.CLOSE, keys.close());
  }
}
