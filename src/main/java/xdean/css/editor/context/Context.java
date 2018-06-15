package xdean.css.editor.context;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.inject.Inject;

import org.springframework.context.annotation.Configuration;

import com.sun.javafx.util.Logging;

import javafx.application.Platform;
import sun.util.logging.PlatformLogger.Level;
import xdean.css.editor.service.DialogService;
import xdean.jex.log.Logable;
import xdean.jex.util.file.FileUtil;

// TODO should be a application listener
@Configuration
public class Context implements Logable {
  public static final Path HOME_PATH = Paths.get(System.getProperty("user.home"), ".xdean", "css");
  public static final Path TEMP_PATH = HOME_PATH.resolve("temp");
  public static final Path LAST_FILE_PATH = Context.TEMP_PATH.resolve("last");

  private @Inject DialogService messageService;

  public Context() {
    debug("Setup Css Editor Environment");
    // create directories
    try {
      FileUtil.createDirectory(HOME_PATH);
      FileUtil.createDirectory(TEMP_PATH);
    } catch (IOException e) {
      error("Create home path fail.", e);
    }
    // close CSS logger
    Logging.getCSSLogger().setLevel(Level.OFF);
    // handle uncaught exception
    Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
      error("Uncaught exception", e);
      if (e instanceof Error) {
        System.exit(1);
      }
      Platform.runLater(() -> messageService.showError("ERROR", e));
    });
  }
}
