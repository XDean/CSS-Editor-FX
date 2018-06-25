package xdean.css.editor.context.setting;

import java.nio.file.Path;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import xdean.css.editor.context.setting.SettingKeys.File;
import xdean.css.editor.context.setting.model.CssEditorActionKeyOption;
import xdean.css.editor.context.setting.model.CssEditorKeyOption;

@Configuration
public class FileActions {

  @Bean(File.NEW)
  public CssEditorActionKeyOption newFile() {
    return new CssEditorActionKeyOption(File.NEW, new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN));
  }

  @Bean(File.OPEN)
  public CssEditorKeyOption<Path> open() {
    return new CssEditorKeyOption<>(File.OPEN, new KeyCodeCombination(KeyCode.O, KeyCombination.CONTROL_DOWN));
  }

  @Bean(File.SAVE)
  public CssEditorActionKeyOption save() {
    return new CssEditorActionKeyOption(File.SAVE, new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN));
  }

  @Bean(File.SAVE_AS)
  public CssEditorKeyOption<Path> saveAs() {
    return new CssEditorKeyOption<>(File.SAVE_AS,
        new KeyCodeCombination(KeyCode.S, KeyCombination.ALT_DOWN, KeyCombination.CONTROL_DOWN));
  }

  @Bean(File.CLOSE)
  public CssEditorActionKeyOption close() {
    return new CssEditorActionKeyOption(File.CLOSE, new KeyCodeCombination(KeyCode.W, KeyCombination.CONTROL_DOWN));
  }

  @Bean(File.REVERT)
  public CssEditorActionKeyOption revert() {
    return new CssEditorActionKeyOption(File.REVERT, KeyCodeCombination.NO_MATCH);
  }

  @Bean(File.EXIT)
  public CssEditorActionKeyOption exit() {
    return new CssEditorActionKeyOption(File.EXIT, KeyCodeCombination.NO_MATCH);
  }
}
