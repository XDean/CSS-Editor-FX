package xdean.css.editor.context.setting;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import xdean.css.editor.context.setting.SettingKeys.Edit;
import xdean.css.editor.context.setting.model.CssEditorKeyEventOption;

@Configuration
public class EditActions {

  @Bean(Edit.UNDO)
  public CssEditorKeyEventOption undo() {
    return new CssEditorKeyEventOption(Edit.UNDO, new KeyCodeCombination(KeyCode.Z, KeyCombination.CONTROL_DOWN));
  }

  @Bean(Edit.REDO)
  public CssEditorKeyEventOption redo() {
    return new CssEditorKeyEventOption(Edit.REDO, new KeyCodeCombination(KeyCode.Y, KeyCombination.CONTROL_DOWN));
  }

  @Bean(Edit.SUGGEST)
  public CssEditorKeyEventOption suggest() {
    return new CssEditorKeyEventOption(Edit.SUGGEST, new KeyCodeCombination(KeyCode.SLASH, KeyCombination.ALT_DOWN));
  }

  @Bean(Edit.FORMAT)
  public CssEditorKeyEventOption format() {
    return new CssEditorKeyEventOption(Edit.FORMAT,
        new KeyCodeCombination(KeyCode.F, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN));
  }

  @Bean(Edit.COMMENT)
  public CssEditorKeyEventOption comment() {
    return new CssEditorKeyEventOption(Edit.COMMENT, new KeyCodeCombination(KeyCode.Q, KeyCombination.CONTROL_DOWN));
  }

  @Bean(Edit.FIND)
  public CssEditorKeyEventOption find() {
    return new CssEditorKeyEventOption(Edit.FIND, new KeyCodeCombination(KeyCode.F, KeyCombination.CONTROL_DOWN));
  }
}
