package xdean.css.editor.context.setting;

import java.nio.file.Path;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import xdean.css.editor.context.setting.ActionKeys.File;
import xdean.css.editor.context.setting.model.CssEditorKeyActionOption;
import xdean.css.editor.context.setting.model.action.Action;
import xdean.css.editor.context.setting.model.action.SimpleAction;
import xdean.css.editor.context.setting.model.action.VoidAction;

@Configuration
public class FileActions {

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
  public CssEditorKeyActionOption saveAs() {
    return new CssEditorKeyActionOption(File.SAVE_AS,
        new KeyCodeCombination(KeyCode.S, KeyCombination.ALT_DOWN, KeyCombination.CONTROL_DOWN));
  }

  @Bean(File.CLOSE)
  public CssEditorKeyActionOption close() {
    return new CssEditorKeyActionOption(File.CLOSE, new KeyCodeCombination(KeyCode.W, KeyCombination.CONTROL_DOWN));
  }
}
