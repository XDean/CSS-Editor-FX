package xdean.css.editor;

import org.springframework.boot.SpringApplication;

import xdean.css.editor.config.Context;
import xdean.jfx.spring.annotation.SpringFxApplication;

@SpringFxApplication
public class FxCssApp {
  public static void main(String[] args) {
    SpringApplication.run(FxCssApp.class, args);
  }

  public FxCssApp() {
    Context.class.getName();
  }
}
