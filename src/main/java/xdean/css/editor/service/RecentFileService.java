package xdean.css.editor.service;

import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Service;

import xdean.css.editor.context.config.ConfigKey;
import xdean.jfxex.support.RecentFileMenuSupport;

@Service
public class RecentFileService extends RecentFileMenuSupport {

  @Override
  public List<String> load() {
    return Arrays.asList(ConfigKey.RECENT_LOCATIONS.getValue().split(","));
  }

  @Override
  public void save(List<String> s) {
    ConfigKey.RECENT_LOCATIONS.setValue(String.join(", ", s));
  }
}
