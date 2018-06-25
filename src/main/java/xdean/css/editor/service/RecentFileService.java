package xdean.css.editor.service;

import static xdean.jex.util.function.Predicates.isEquals;
import static xdean.jfxex.bean.ListenerUtil.addListenerAndInvoke;
import static xdean.jfxex.bean.ListenerUtil.list;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.stereotype.Service;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import xdean.css.editor.context.setting.SettingKeys;
import xdean.css.editor.context.setting.model.option.StringOption;
import xdean.jex.log.Logable;

@Service
public class RecentFileService implements Logable {

  @Inject
  @Named(SettingKeys.RECENT_LOC)
  StringOption recent;

  private final ObservableList<Path> recentFiles = FXCollections.observableArrayList();

  @PostConstruct
  private void init() {
    recentFiles.addListener(list(b -> b.onAdd(c -> save())));
    addListenerAndInvoke(recent.valueProperty(), (ob, o, n) -> load());
  }

  public ObservableList<Path> getRecentFiles() {
    return recentFiles;
  }

  public void load() {
    try {
      recentFiles.setAll(Arrays.asList(recent.getValue().split(","))
          .stream()
          .map(String::trim)
          .filter(s -> !s.isEmpty())
          .map(Paths::get)
          .collect(Collectors.toList()));
    } catch (Exception e) {
      error().log("Error to load recent location", e);
    }
  }

  public void save() {
    recent.setValue(String.join(", ", recentFiles.stream()
        .map(Path::toString)
        .collect(Collectors.toList())));
  }

  public Optional<Path> getLatestFile() {
    return recentFiles.stream().findFirst();
  }

  public void setLatestFile(Path path) {
    recentFiles.removeIf(isEquals(path));
    recentFiles.add(0, path);
  }

  public void clear() {
    recentFiles.clear();
  }
}
