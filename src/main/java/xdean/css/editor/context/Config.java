package xdean.css.editor.context;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.Properties;

import org.springframework.stereotype.Service;

import xdean.jex.log.Logable;

@Service
public class Config implements Logable {
  private static final Path CONFIG_FILE = Context.HOME_PATH.resolve("config.properties");
  private static final Path DEFAULT_CONFIG_PATH = Paths.get("/default_config.properties");

  private final Properties properties = new Properties();

  public Config() {
    try {
      if (Files.notExists(CONFIG_FILE)) {
        if (Files.exists(DEFAULT_CONFIG_PATH)) {
          Files.copy(DEFAULT_CONFIG_PATH, CONFIG_FILE);
        } else {
          Files.createFile(CONFIG_FILE);
        }
      }
      properties.load(Files.newBufferedReader(CONFIG_FILE));
    } catch (IOException e) {
      error("IOException", e);
    }
    debug("Load last config: " + properties.toString());
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
    save();
  }

  public void setIfAbsent(Object key, String value) {
    setIfAbsent(key.toString(), value);
  }

  public void setIfAbsent(String key, String value) {
    if (getProperty(key).isPresent() == false) {
      setProperty(key, value);
    }
  }

  private synchronized void save() {
    try {
      properties.store(Files.newOutputStream(CONFIG_FILE), "");
    } catch (IOException e) {
      error(e);
    }
  }
}
