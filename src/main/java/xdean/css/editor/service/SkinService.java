package xdean.css.editor.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import io.reactivex.rxjavafx.observables.JavaFxObservable;
import xdean.css.editor.context.Context;
import xdean.css.editor.context.DefaultSkin;
import xdean.css.editor.context.config.Config;
import xdean.css.editor.context.config.ConfigKey;
import xdean.jex.log.Logable;
import xdean.jex.util.string.StringUtil;
import xdean.jfxex.support.skin.SkinStyle;

@Service
public class SkinService extends xdean.jfxex.support.skin.SkinManager implements Logable, InitializingBean {
  @Override
  public void afterPropertiesSet() throws Exception {
    // load default skins
    for (SkinStyle ss : DefaultSkin.values()) {
      addSkin(ss);
    }
    // load skin files in /skin
    Path skinFolder = Context.HOME_PATH.resolve("skin");
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
            addSkin(new SkinStyle() {
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
    getSkinList().stream().map(SkinStyle::getURL).map(s -> "loaded skin: " + s).forEach(this::debug);

    changeSkin(DefaultSkin.CLASSIC);
    String configSkin = Config.getProperty(ConfigKey.SKIN, null);
    if (configSkin != null) {
      getSkinList().stream()
          .filter(s -> s.getName().equals(configSkin))
          .findAny()
          .ifPresent(s -> changeSkin(s));
    }
    JavaFxObservable.valuesOf(skinProperty())
        .subscribe(skin -> Config.setProperty(ConfigKey.SKIN, skin.getName()));
  }
}
