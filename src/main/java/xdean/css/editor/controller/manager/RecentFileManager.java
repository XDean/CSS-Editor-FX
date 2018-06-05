package xdean.css.editor.controller.manager;

import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Component;

import xdean.css.editor.config.Config;
import xdean.css.editor.config.ConfigKey;
import xdean.jfxex.support.RecentFileMenuSupport;

@Component
public class RecentFileManager extends RecentFileMenuSupport {

  @Override
  public List<String> load() {
    return Arrays.asList(Config.getProperty(ConfigKey.RECENT_LOCATIONS, "").split(","));
  }

  @Override
  public void save(List<String> s) {
    Config.setProperty(ConfigKey.RECENT_LOCATIONS, String.join(", ", s));
  }
}
