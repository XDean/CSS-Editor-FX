package xdean.css.editor.service;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.stereotype.Service;

import xdean.css.editor.context.option.OptionKeys;
import xdean.css.editor.context.option.model.StringOption;
import xdean.jfxex.support.RecentFileMenuSupport;

@Service
public class RecentFileService extends RecentFileMenuSupport {

  @Inject
  @Named(OptionKeys.RECENT_LOC)
  StringOption recent;

  @Override
  public List<String> load() {
    return Arrays.asList(recent.getValue().split(","));
  }

  @Override
  public void save(List<String> s) {
    recent.setValue(String.join(", ", s));
  }
}
