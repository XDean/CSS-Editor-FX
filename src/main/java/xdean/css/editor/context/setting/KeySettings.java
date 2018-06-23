package xdean.css.editor.context.setting;

import static xdean.css.editor.context.setting.SettingKeys.KEY;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import xdean.css.editor.context.action.ActionKeys;
import xdean.css.editor.context.setting.SettingKeys.Key;
import xdean.css.editor.context.setting.model.KeyOption;
import xdean.css.editor.context.setting.model.OptionGroup;

@Configuration
public class KeySettings {

  @Bean(name = KEY)
  public OptionGroup keys() {
    OptionGroup key = new OptionGroup(KEY);
    key.add(undo());
    key.add(redo());
    key.add(suggest());
    key.add(format());
    key.add(comment());
    key.add(find());
    key.add(close());
    return key;
  }

  @Bean(Key.UNDO)
  public KeyOption undo() {
    return create(ActionKeys.Edit.UNDO, new KeyCodeCombination(KeyCode.Z, KeyCombination.CONTROL_DOWN));
  }

  @Bean(Key.REDO)
  public KeyOption redo() {
    return create(ActionKeys.Edit.REDO, new KeyCodeCombination(KeyCode.Y, KeyCombination.CONTROL_DOWN));
  }

  @Bean(Key.SUGGEST)
  public KeyOption suggest() {
    return create(ActionKeys.Edit.SUGGEST, new KeyCodeCombination(KeyCode.SLASH, KeyCombination.ALT_DOWN));
  }

  @Bean(Key.FORMAT)
  public KeyOption format() {
    return create(ActionKeys.Edit.FORMAT,
        new KeyCodeCombination(KeyCode.F, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN));
  }

  @Bean(Key.COMMENT)
  public KeyOption comment() {
    return create(ActionKeys.Edit.COMMENT, new KeyCodeCombination(KeyCode.Q, KeyCombination.CONTROL_DOWN));
  }

  @Bean(Key.FIND)
  public KeyOption find() {
    return create(ActionKeys.Edit.FIND, new KeyCodeCombination(KeyCode.F, KeyCombination.CONTROL_DOWN));
  }

  @Bean(Key.CLOSE)
  public KeyOption close() {
    return create(ActionKeys.File.CLOSE, new KeyCodeCombination(KeyCode.W, KeyCombination.CONTROL_DOWN));
  }

  private KeyOption create(String key, KeyCombination defaultKey) {
    return new KeyOption(key, defaultKey);
  }
}
