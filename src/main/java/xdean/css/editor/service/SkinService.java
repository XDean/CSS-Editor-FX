package xdean.css.editor.service;

import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.stereotype.Service;

import io.reactivex.rxjavafx.observables.JavaFxObservable;
import xdean.css.editor.context.Context;
import xdean.css.editor.context.setting.DefaultValue.DefaultSkin;
import xdean.css.editor.context.setting.SettingKeys;
import xdean.css.editor.context.setting.model.option.Option;
import xdean.jex.log.Logable;
import xdean.jex.util.string.StringUtil;
import xdean.jfx.spring.annotation.FxReady;
import xdean.jfx.spring.splash.PreloadReporter;
import xdean.jfx.spring.splash.PreloadReporter.SubReporter;
import xdean.jfxex.support.skin.SkinManager;
import xdean.jfxex.support.skin.SkinStyle;

@Service
@FxReady
public class SkinService extends SkinManager implements Logable {
  @Inject
  @Named(SettingKeys.SKIN)
  private Option<String> skinOption;

  @Inject
  private DialogService dialogService;

  @Inject
  private PreloadReporter preload;

  @PostConstruct
  public void init() throws Exception {
    SubReporter sub = preload.load("Loading skins...");
    // load default skins
    for (SkinStyle ss : DefaultSkin.values()) {
      sub.load("Loading " + ss.getName());
      addSkin(ss);
    }
    // load skin files in /skin
    Path skinFolder = Context.HOME_PATH.resolve("skin");
    try {
      if (Files.notExists(skinFolder)) {
        Files.createDirectory(skinFolder);
      } else {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(skinFolder)) {
          stream.forEach(path -> {
            String fileName = path.getFileName().toString();
            if (!Files.isDirectory(path) && (fileName.endsWith(".css") || fileName.endsWith(".bss"))) {
              String url = path.toUri().toString();
              String name = StringUtil.upperFirst(fileName.substring(0, fileName.length() - 4));
              sub.load("Loading " + name);
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
      }
      throw new Exception();
    } catch (Exception e) {
      dialogService.errorNotification(e)
          .text("Load skins failed.")
          .show();
    }
    getSkinList().stream().map(SkinStyle::getURL).map(s -> "loaded skin: " + s).forEach(this::debug);

    sub.load("Read skin setting");
    String configSkin = skinOption.getValue();
    if (configSkin != null) {
      getSkinList().stream()
          .filter(s -> s.getName().equals(configSkin))
          .findAny()
          .ifPresent(s -> changeSkin(s));
    } else {
      changeSkin(DefaultSkin.CLASSIC);
    }
    sub.load("Apply skin setting");
    JavaFxObservable.valuesOf(skinProperty())
        .subscribe(skin -> skinOption.setValue(skin.getName()));
  }
}
