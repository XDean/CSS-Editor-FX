package xdean.css.editor.context.setting;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import xdean.css.editor.context.setting.SettingKeys.File;
import xdean.css.editor.context.setting.model.CssEditorKeyActionOption;

@Configuration
public class FileActions {

  @Bean(File.NEW)
  public CssEditorKeyActionOption newFile() {
    return new CssEditorKeyActionOption(File.NEW, new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN));
  }

  @Bean(File.OPEN)
  public CssEditorKeyActionOption open() {
    return new CssEditorKeyActionOption(File.OPEN, new KeyCodeCombination(KeyCode.O, KeyCombination.CONTROL_DOWN));
  }

  @Bean(File.SAVE)
  public CssEditorKeyActionOption save() {
    return new CssEditorKeyActionOption(File.SAVE, new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN));
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
