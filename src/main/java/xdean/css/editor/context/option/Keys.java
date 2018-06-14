package xdean.css.editor.context.option;

import static xdean.css.editor.context.option.OptionKeys.KEY;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import xdean.css.editor.context.option.OptionKeys.Key;
import xdean.css.editor.context.option.model.Option;
import xdean.css.editor.context.option.model.OptionGroup;
import xdean.css.editor.context.option.model.SimpleOption;
import xdean.jfxex.util.StringConverters;

@Configuration
public class Keys {

  @Inject
  @Named(KEY)
  private OptionGroup key;

  @Bean(Key.SUGGEST)
  public Option<KeyCombination> suggest() {
    return key.add(create(Key.SUGGEST, new KeyCodeCombination(KeyCode.SLASH, KeyCombination.ALT_DOWN)));
  }

  @Bean(Key.FORMAT)
  public Option<KeyCombination> format() {
    return key.add(create(Key.FORMAT, new KeyCodeCombination(KeyCode.F, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN)));
  }

  @Bean(Key.COMMENT)
  public Option<KeyCombination> comment() {
    return key.add(create(Key.COMMENT, new KeyCodeCombination(KeyCode.Q, KeyCombination.CONTROL_DOWN)));
  }

  @Bean(Key.FIND)
  public Option<KeyCombination> find() {
    return key.add(create(Key.FIND, new KeyCodeCombination(KeyCode.F, KeyCombination.CONTROL_DOWN)));
  }

  @Bean(Key.CLOSE)
  public Option<KeyCombination> close() {
    return key.add(create(Key.CLOSE, new KeyCodeCombination(KeyCode.W, KeyCombination.CONTROL_DOWN)));
  }

  private Option<KeyCombination> create(String key, KeyCombination defaultKey) {
    return new SimpleOption<>(key, defaultKey, StringConverters.create(KeyCombination::valueOf));
  }
}
