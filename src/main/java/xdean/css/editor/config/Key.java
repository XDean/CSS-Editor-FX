package xdean.css.editor.config;

import xdean.css.editor.config.option.Option;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;

public enum Key implements Option<KeyCombination> {
  SUGGEST("Completion Assist", new KeyCodeCombination(KeyCode.SLASH, KeyCombination.ALT_DOWN)),
  FORMAT("Format Code(Not support yet)", new KeyCodeCombination(KeyCode.F, KeyCombination.CONTROL_DOWN,
      KeyCombination.SHIFT_DOWN)),
  COMMENT("Toggle Comment", new KeyCodeCombination(KeyCode.Q, KeyCombination.CONTROL_DOWN)),
  FIND("Find", new KeyCodeCombination(KeyCode.F, KeyCombination.CONTROL_DOWN)),
  CLOSE("Close Tab", new KeyCodeCombination(KeyCode.W, KeyCombination.CONTROL_DOWN));

  ObjectProperty<KeyCombination> key = new SimpleObjectProperty<>();
  KeyCombination defaultValue;
  String describe;

  private Key(String describe, KeyCombination defaultValue) {
    this.describe = describe;
    this.defaultValue = defaultValue;
    key.set(defaultValue);
    Options.bind(this, KeyCombination::valueOf);
    Options.KEY.add(this);
  }

  @Override
  public KeyCombination get() {
    return key.get();
  }

  @Override
  public void set(KeyCombination value) {
    key.set(value);
  }

  @Override
  public ObjectProperty<KeyCombination> property() {
    return key;
  }

  @Override
  public KeyCombination getDefault() {
    return defaultValue;
  }

  @Override
  public String getDescribe() {
    return describe;
  }
}
