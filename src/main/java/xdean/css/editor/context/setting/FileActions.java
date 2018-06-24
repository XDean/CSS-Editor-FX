package xdean.css.editor.context.setting;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import xdean.css.editor.context.setting.SettingKeys.File;
import xdean.css.editor.context.setting.model.CssEditorKeyEventOption;

@Configuration
public class FileActions {

  @Bean(File.NEW)
  public CssEditorKeyEventOption newFile() {
    return new CssEditorKeyEventOption(File.NEW, new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN));
  }

  @Bean(File.OPEN)
  public CssEditorKeyEventOption open() {
    return new CssEditorKeyEventOption(File.OPEN, new KeyCodeCombination(KeyCode.O, KeyCombination.CONTROL_DOWN));
  }

  @Bean(File.SAVE)
  public CssEditorKeyEventOption save() {
    return new CssEditorKeyEventOption(File.SAVE, new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN));
  }

  @Bean(File.SAVE_AS)
  public CssEditorKeyEventOption saveAs() {
    return new CssEditorKeyEventOption(File.SAVE_AS,
        new KeyCodeCombination(KeyCode.S, KeyCombination.ALT_DOWN, KeyCombination.CONTROL_DOWN));
  }

  @Bean(File.CLOSE)
  public CssEditorKeyEventOption close() {
    return new CssEditorKeyEventOption(File.CLOSE, new KeyCodeCombination(KeyCode.W, KeyCombination.CONTROL_DOWN));
  }

  @Bean(File.REVERT)
  public CssEditorKeyEventOption revert() {
    return new CssEditorKeyEventOption(File.REVERT, null);
  }
}
