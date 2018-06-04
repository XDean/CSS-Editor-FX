package xdean.css.editor.controller.manager;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import javafx.scene.control.Menu;
import xdean.css.editor.config.Config;
import xdean.css.editor.config.ConfigKey;
import xdean.jfxex.support.RecentFileMenuSupport;

public class RecentFileManager extends RecentFileMenuSupport {
  public static final String RECENT_MENU = "recent_menu";

  @Inject
  public RecentFileManager(@Named(RECENT_MENU) Menu menu) {
    super(menu);
  }

  @Override
  public List<String> load() {
    return Arrays.asList(Config.getProperty(ConfigKey.RECENT_LOCATIONS, "").split(","));
  }

  @Override
  public void save(List<String> s) {
    Config.setProperty(ConfigKey.RECENT_LOCATIONS, String.join(", ", s));
  }
}
