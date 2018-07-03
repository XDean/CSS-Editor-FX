package xdean.css.editor;

import org.springframework.boot.SpringApplication;

import xdean.jfx.spring.annotation.Splash;
import xdean.jfx.spring.annotation.SpringFxApplication;

@Splash
@SpringFxApplication
public class FxCssEntrance {
  public static void main(String[] args) {
    SpringApplication.run(FxCssEntrance.class, args);
  }
}
