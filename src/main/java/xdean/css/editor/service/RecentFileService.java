package xdean.css.editor.service;

import java.util.Arrays;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.stereotype.Service;

import xdean.css.editor.context.setting.SettingKeys;
import xdean.css.editor.context.setting.model.option.StringOption;
import xdean.jfxex.support.RecentFileMenuSupport;

@Service
public class RecentFileService extends RecentFileMenuSupport {

  @Inject
  @Named(SettingKeys.RECENT_LOC)
  StringOption recent;

  @PostConstruct
  private void init() {
    load();
  }

  @Override
  public List<String> doLoad() {
    return Arrays.asList(recent.getValue().split(","));
  }

  @Override
  public void doSave(List<String> s) {
    recent.setValue(String.join(", ", s));
  }
}
