package xdean.css.editor.context.setting.model;

import javafx.scene.input.KeyCombination;
import xdean.jfxex.util.StringConverters;

public class KeyOption extends SimpleOption<KeyCombination> {
  public KeyOption(String key, KeyCombination defaultKey) {
    super(key, defaultKey, StringConverters.create(KeyCombination::valueOf));
  }
}
