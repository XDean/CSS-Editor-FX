package xdean.css.editor.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.sun.javafx.util.Logging;

import io.reactivex.rxjavafx.observables.JavaFxObservable;
import javafx.application.Platform;
import lombok.extern.slf4j.Slf4j;
import sun.util.logging.PlatformLogger.Level;
import xdean.css.editor.util.Util;
import xdean.jex.util.file.FileUtil;
import xdean.jex.util.lang.ExceptionUtil;
import xdean.jex.util.string.StringUtil;
import xdean.jfxex.support.skin.SkinManager;
import xdean.jfxex.support.skin.SkinStyle;

/**
 * LOG, CONFIG, SKIN
 *
 * @author XDean
 *
 */
@Slf4j
@SuppressWarnings("restriction")
public class Context {
  public static final Path HOME_PATH = Paths.get(System.getProperty("user.home"), ".xdean", "css");
  public static final Path TEMP_PATH = HOME_PATH.resolve("temp");
  public static final Path CONFIG_PATH = HOME_PATH.resolve("config.properties");
  public static final Path DEFAULT_CONFIG_PATH = Paths.get("/default_config.properties");
  public static final Path LAST_FILE_PATH = Context.TEMP_PATH.resolve("last");

  public static final SkinManager SKIN = new SkinManager();
  static {
    prepare();
    loadConfig();
    loadSkin();
  }

  public static void loadClass() {
  }

  private static void prepare() {
    // create directories
    try {
      FileUtil.createDirectory(HOME_PATH);
      FileUtil.createDirectory(TEMP_PATH);
    } catch (IOException e) {
      log.error("Create folder fail.", e);
    }
    // close CSS logger
    Logging.getCSSLogger().setLevel(Level.OFF);
    // handle uncaught exception
    Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
      log.error("Uncaught: ", e);
      if (e instanceof Error) {
        System.exit(1);
      }
      Platform.runLater(() -> Util.showMessageDialog(null, "ERROR", ExceptionUtil.getStackTraceString(e)));
    });
  }

  private static void loadConfig() {
    Config.locate(CONFIG_PATH, DEFAULT_CONFIG_PATH);
  }

  private static void loadSkin() {
    // load default skins
    for (SkinStyle ss : DefaultSkin.values()) {
      SKIN.addSkin(ss);
    }
    // load skin files in /skin
    Path skinFolder = HOME_PATH.resolve("skin");
    try {
      if (Files.notExists(skinFolder)) {
        Files.createDirectory(skinFolder);
      } else {
        Files.newDirectoryStream(skinFolder).forEach(path -> {
          String fileName = path.getFileName().toString();
          if (!Files.isDirectory(path) && (fileName.endsWith(".css") ||
              fileName.endsWith(".bss"))) {
            String url = path.toUri().toString();
            String name = StringUtil.upperFirst(fileName.substring(0, fileName.length() - 4));
            SKIN.addSkin(new SkinStyle() {
              @Override
              public String getURL() {
                return url;
              }

              @Override
              public String getName() {
                return name;
              }
            });
          }
        });
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    SKIN.getSkinList().stream().map(SkinStyle::getURL).map(s -> "loaded skin: " +
        s).forEach(log::debug);

    SKIN.changeSkin(DefaultSkin.CLASSIC);
    String configSkin = Config.getProperty(ConfigKey.SKIN, null);
    if (configSkin != null) {
      SKIN.getSkinList().stream()
          .filter(s -> s.getName().equals(configSkin))
          .findAny()
          .ifPresent(s -> SKIN.changeSkin(s));
    }
    JavaFxObservable.valuesOf(SKIN.skinProperty())
        .subscribe(skin -> Config.setProperty(ConfigKey.SKIN, skin.getName()));
  }
}
