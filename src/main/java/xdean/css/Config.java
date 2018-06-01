package xdean.css;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Properties;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Config {
  private static final Properties CONFIG = new Properties();
  private static Path configFile;

  public static void locate(Path configPath, Path defaultConfig) {
    try {
      if (Files.notExists(configPath)) {
        if (Files.exists(defaultConfig)) {
          Files.copy(defaultConfig, configPath);
        } else {
          Files.createFile(configPath);
        }
      }
      CONFIG.load(Files.newBufferedReader(configPath));
    } catch (IOException e) {
      log.error("IOException", e);
    }
    log.debug("Load last config: " + CONFIG.toString());
    configFile = configPath;
  }

  public static Optional<String> getProperty(String key) {
    return Optional.ofNullable(CONFIG.getProperty(key));
  }

  public static Optional<String> getProperty(Object key) {
    return getProperty(key.toString());
  }

  public static String getProperty(Object key, String defaultValue) {
    return getProperty(key.toString(), defaultValue);
  }

  public static String getProperty(String key, String defaultValue) {
    return getProperty(key).orElse(defaultValue);
  }

  public static void setProperty(Object key, String value) {
    setProperty(key.toString(), value);
  }

  public static void setProperty(String key, String value) {
    CONFIG.setProperty(key, value);
    save();
  }

  public static void setIfAbsent(Object key, String value) {
    setIfAbsent(key.toString(), value);
  }

  public static void setIfAbsent(String key, String value) {
    if (getProperty(key).isPresent() == false) {
      setProperty(key, value);
    }
  }

  private static synchronized void save() {
    if (configFile == null) {
      return;
    }
    try {
      CONFIG.store(Files.newOutputStream(configFile), "");
    } catch (IOException e) {
      log.error("", e);
    }
  }
}
