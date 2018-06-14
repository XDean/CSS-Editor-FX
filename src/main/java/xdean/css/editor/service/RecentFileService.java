package xdean.css.editor.service;

import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Service;

import xdean.css.editor.config.Config;
import xdean.css.editor.config.ConfigKey;
import xdean.jfxex.support.RecentFileMenuSupport;

@Service
public class RecentFileService extends RecentFileMenuSupport {

  @Override
  public List<String> load() {
    return Arrays.asList(Config.getProperty(ConfigKey.RECENT_LOCATIONS, "").split(","));
  }

  @Override
  public void save(List<String> s) {
    Config.setProperty(ConfigKey.RECENT_LOCATIONS, String.join(", ", s));
  }
}
