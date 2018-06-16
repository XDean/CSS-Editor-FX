package xdean.css.editor.context;

import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.inject.Inject;

import org.springframework.boot.context.event.ApplicationStartingEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;

import com.sun.javafx.util.Logging;

import javafx.application.Platform;
import sun.util.logging.PlatformLogger.Level;
import xdean.css.editor.service.DialogService;
import xdean.jex.log.Logable;
import xdean.jex.util.file.FileUtil;
import xdean.jfx.spring.starter.FxContextPostProcessor;
import xdean.spring.auto.AutoSpringFactories;

@Configuration
@AutoSpringFactories(ApplicationListener.class)
public class Context implements FxContextPostProcessor, ApplicationListener<ApplicationStartingEvent>,
    UncaughtExceptionHandler, Logable {
  public static final Path HOME_PATH = Paths.get(System.getProperty("user.home"), ".xdean", "css");
  public static final Path TEMP_PATH = HOME_PATH.resolve("temp");
  public static final Path LAST_FILE_PATH = Context.TEMP_PATH.resolve("last");

  private @Inject DialogService messageService;

  @Override
  public void onApplicationEvent(ApplicationStartingEvent event) {
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
  }

  @Override
  public void beforeStart() {
    // handle uncaught exception
    Thread.setDefaultUncaughtExceptionHandler(this);
    Platform.runLater(() -> Thread.currentThread().setUncaughtExceptionHandler(this));
  }

  @Override
  public void uncaughtException(Thread t, Throwable e) {
    error("Uncaught exception", e);
    if (e instanceof Error) {
      System.exit(1);
    }
    if (Platform.isFxApplicationThread()) {
      messageService.showError("ERROR", e);
    } else {
      Platform.runLater(() -> messageService.showError("ERROR", e));
    }
  }
}
