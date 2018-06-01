package xdean.css.editor;

import org.springframework.boot.SpringApplication;

import xdean.css.editor.config.Context;
import xdean.jfx.spring.annotation.SpringFxApplication;

@SpringFxApplication
public class FxCssEntrance {
  public static void main(String[] args) {
    Context.class.getName();
    SpringApplication.run(FxCssEntrance.class, args);
  }
}
