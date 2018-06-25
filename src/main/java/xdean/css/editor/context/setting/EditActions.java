package xdean.css.editor.context.setting;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import xdean.css.editor.context.setting.SettingKeys.Edit;
import xdean.css.editor.context.setting.model.CssEditorActionKeyOption;

@Configuration
public class EditActions {

  @Bean(Edit.UNDO)
  public CssEditorActionKeyOption undo() {
    return new CssEditorActionKeyOption(Edit.UNDO, new KeyCodeCombination(KeyCode.Z, KeyCombination.CONTROL_DOWN));
  }

  @Bean(Edit.REDO)
  public CssEditorActionKeyOption redo() {
    return new CssEditorActionKeyOption(Edit.REDO, new KeyCodeCombination(KeyCode.Y, KeyCombination.CONTROL_DOWN));
  }

  @Bean(Edit.SUGGEST)
  public CssEditorActionKeyOption suggest() {
    return new CssEditorActionKeyOption(Edit.SUGGEST, new KeyCodeCombination(KeyCode.SLASH, KeyCombination.ALT_DOWN));
  }

  @Bean(Edit.FORMAT)
  public CssEditorActionKeyOption format() {
    return new CssEditorActionKeyOption(Edit.FORMAT,
        new KeyCodeCombination(KeyCode.F, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN));
  }

  @Bean(Edit.COMMENT)
  public CssEditorActionKeyOption comment() {
    return new CssEditorActionKeyOption(Edit.COMMENT, new KeyCodeCombination(KeyCode.Q, KeyCombination.CONTROL_DOWN));
  }

  @Bean(Edit.FIND)
  public CssEditorActionKeyOption find() {
    return new CssEditorActionKeyOption(Edit.FIND, new KeyCodeCombination(KeyCode.F, KeyCombination.CONTROL_DOWN));
  }
}
