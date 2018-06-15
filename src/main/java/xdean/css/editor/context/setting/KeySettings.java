package xdean.css.editor.context.setting;

import static xdean.css.editor.context.setting.SettingKeys.KEY;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import xdean.css.editor.context.setting.SettingKeys.Key;
import xdean.css.editor.context.setting.model.Option;
import xdean.css.editor.context.setting.model.OptionGroup;
import xdean.css.editor.context.setting.model.SimpleOption;
import xdean.jfxex.util.StringConverters;

@Configuration
public class KeySettings {

  @Bean(name = KEY)
  public OptionGroup keys() {
    OptionGroup key = new OptionGroup(KEY);
    key.add(suggest());
    key.add(format());
    key.add(comment());
    key.add(find());
    key.add(close());
    return key;
  }

  @Bean(Key.SUGGEST)
  public Option<KeyCombination> suggest() {
    return create(Key.SUGGEST, new KeyCodeCombination(KeyCode.SLASH, KeyCombination.ALT_DOWN));
  }

  @Bean(Key.FORMAT)
  public Option<KeyCombination> format() {
    return create(Key.FORMAT, new KeyCodeCombination(KeyCode.F, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN));
  }

  @Bean(Key.COMMENT)
  public Option<KeyCombination> comment() {
    return create(Key.COMMENT, new KeyCodeCombination(KeyCode.Q, KeyCombination.CONTROL_DOWN));
  }

  @Bean(Key.FIND)
  public Option<KeyCombination> find() {
    return create(Key.FIND, new KeyCodeCombination(KeyCode.F, KeyCombination.CONTROL_DOWN));
  }

  @Bean(Key.CLOSE)
  public Option<KeyCombination> close() {
    return create(Key.CLOSE, new KeyCodeCombination(KeyCode.W, KeyCombination.CONTROL_DOWN));
  }

  private Option<KeyCombination> create(String key, KeyCombination defaultKey) {
    return new SimpleOption<>(key, defaultKey, StringConverters.create(KeyCombination::valueOf));
  }
}
