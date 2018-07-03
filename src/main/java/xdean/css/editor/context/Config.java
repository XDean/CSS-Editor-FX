package xdean.css.editor.context;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.springframework.stereotype.Service;

import io.reactivex.subjects.Subject;
import io.reactivex.subjects.UnicastSubject;
import xdean.jex.log.Logable;
import xdean.jfx.spring.splash.PreloadReporter;
import xdean.jfx.spring.splash.PreloadReporter.SubReporter;

@Service
public class Config implements Logable {
  private static final Path CONFIG_FILE = Context.HOME_PATH.resolve("config.properties");
  private static final Path DEFAULT_CONFIG_PATH = Paths.get("/default_config.properties");

  private final Properties properties = new Properties();
  private final Subject<String> saveSubject = UnicastSubject.create();
  private @Inject PreloadReporter preload;

  @PostConstruct
  public void init() {
    try {
      SubReporter sub = preload.load("Loading settings...");
      sub.setCount(3);
      sub.load("Check user setting");
      if (Files.notExists(CONFIG_FILE)) {
        if (Files.exists(DEFAULT_CONFIG_PATH)) {
          Files.copy(DEFAULT_CONFIG_PATH, CONFIG_FILE);
        } else {
          Files.createFile(CONFIG_FILE);
        }
      }
      sub.load("Loading settings file");
      properties.load(Files.newBufferedReader(CONFIG_FILE));
      debug("Load last config: " + properties.toString());
      sub.load("Apply settings");
      saveSubject
          .debounce(1000, TimeUnit.MILLISECONDS)
          .subscribe(e -> saveToFile());
    } catch (IOException e) {
      error("IOException", e);
    }
  }

  public Optional<String> getProperty(String key) {
    return Optional.ofNullable(properties.getProperty(key));
  }

  public Optional<String> getProperty(Object key) {
    return getProperty(key.toString());
  }

  public String getProperty(Object key, String defaultValue) {
    return getProperty(key.toString(), defaultValue);
  }

  public String getProperty(String key, String defaultValue) {
    return getProperty(key).orElse(defaultValue);
  }

  public void setProperty(Object key, String value) {
    setProperty(key.toString(), value);
  }

  public void setProperty(String key, String value) {
    properties.setProperty(key, value);
    saveSubject.onNext(key);
  }

  public void setIfAbsent(Object key, String value) {
    setIfAbsent(key.toString(), value);
  }

  public void setIfAbsent(String key, String value) {
    if (getProperty(key).isPresent() == false) {
      setProperty(key, value);
    }
  }

  public synchronized void saveToFile() {
    try {
      properties.store(Files.newOutputStream(CONFIG_FILE), "");
    } catch (IOException e) {
      error(e);
    }
  }
}
