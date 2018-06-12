package xdean.css.editor.config;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.inject.Inject;

import org.springframework.context.annotation.Configuration;

import com.sun.javafx.util.Logging;

import javafx.application.Platform;
import sun.util.logging.PlatformLogger.Level;
import xdean.css.editor.service.MessageService;
import xdean.jex.log.Logable;
import xdean.jex.util.file.FileUtil;
import xdean.jex.util.lang.ExceptionUtil;
import xdean.jfx.spring.starter.FxContextPostProcessor;

/**
 * @author XDean
 */
@Configuration
@SuppressWarnings("restriction")
public class Context implements Logable, FxContextPostProcessor {
  public static final Path HOME_PATH = Paths.get(System.getProperty("user.home"), ".xdean", "css");
  public static final Path TEMP_PATH = HOME_PATH.resolve("temp");
  public static final Path CONFIG_PATH = HOME_PATH.resolve("config.properties");
  public static final Path DEFAULT_CONFIG_PATH = Paths.get("/default_config.properties");
  public static final Path LAST_FILE_PATH = Context.TEMP_PATH.resolve("last");

  @Inject
  MessageService messageService;

  @Override
  public void beforeStart() {
    // create directories
    try {
      FileUtil.createDirectory(HOME_PATH);
      FileUtil.createDirectory(TEMP_PATH);
    } catch (IOException e) {
      error("Create folder fail.", e);
    }
    // close CSS logger
    Logging.getCSSLogger().setLevel(Level.OFF);
    // handle uncaught exception
    Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
      error("Uncaught: ", e);
      if (e instanceof Error) {
        System.exit(1);
      }
      Platform.runLater(() -> messageService.showMessageDialog(null, "ERROR", ExceptionUtil.getStackTraceString(e)));
    });

    Config.locate(CONFIG_PATH, DEFAULT_CONFIG_PATH);
  }
}
