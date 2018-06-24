package xdean.css.editor.context.setting;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import xdean.css.editor.context.setting.SettingKeys.Edit;
import xdean.css.editor.context.setting.model.CssEditorKeyActionOption;

@Configuration
public class EditActions {

  @Bean(Edit.UNDO)
  public CssEditorKeyActionOption undo() {
    return new CssEditorKeyActionOption(Edit.UNDO, new KeyCodeCombination(KeyCode.Z, KeyCombination.CONTROL_DOWN));
  }

  @Bean(Edit.REDO)
  public CssEditorKeyActionOption redo() {
    return new CssEditorKeyActionOption(Edit.REDO, new KeyCodeCombination(KeyCode.Y, KeyCombination.CONTROL_DOWN));
  }

  @Bean(Edit.SUGGEST)
  public CssEditorKeyActionOption suggest() {
    return new CssEditorKeyActionOption(Edit.SUGGEST, new KeyCodeCombination(KeyCode.SLASH, KeyCombination.ALT_DOWN));
  }

  @Bean(Edit.FORMAT)
  public CssEditorKeyActionOption format() {
    return new CssEditorKeyActionOption(Edit.FORMAT,
        new KeyCodeCombination(KeyCode.F, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN));
  }

  @Bean(Edit.COMMENT)
  public CssEditorKeyActionOption comment() {
    return new CssEditorKeyActionOption(Edit.COMMENT, new KeyCodeCombination(KeyCode.Q, KeyCombination.CONTROL_DOWN));
  }

  @Bean(Edit.FIND)
  public CssEditorKeyActionOption find() {
    return new CssEditorKeyActionOption(Edit.FIND, new KeyCodeCombination(KeyCode.F, KeyCombination.CONTROL_DOWN));
  }
}
