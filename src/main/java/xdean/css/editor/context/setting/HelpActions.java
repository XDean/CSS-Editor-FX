package xdean.css.editor.context.setting;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javafx.scene.input.KeyCodeCombination;
import xdean.css.editor.context.setting.SettingKeys.Help;
import xdean.css.editor.context.setting.model.CssEditorActionKeyOption;

@Configuration
public class HelpActions {

  @Bean(Help.SETTINGS)
  public CssEditorActionKeyOption settings() {
    return new CssEditorActionKeyOption(Help.SETTINGS, KeyCodeCombination.NO_MATCH);
  }
  @Bean(Help.ABOUT)
  public CssEditorActionKeyOption about() {
    return new CssEditorActionKeyOption(Help.ABOUT, KeyCodeCombination.NO_MATCH);
  }
  @Bean(Help.HELP)
  public CssEditorActionKeyOption help() {
    return new CssEditorActionKeyOption(Help.HELP, KeyCodeCombination.NO_MATCH);
  }
}
