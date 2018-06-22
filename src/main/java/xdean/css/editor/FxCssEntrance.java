package xdean.css.editor;

import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.Import;

import xdean.css.editor.context.Context;
import xdean.jfx.spring.annotation.SpringFxApplication;

@SpringFxApplication
@Import(Context.class)
public class FxCssEntrance {
  public static void main(String[] args) {
    SpringApplication.run(FxCssEntrance.class, args);
  }
}
