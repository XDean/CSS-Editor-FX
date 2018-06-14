package xdean.css.editor.context;

import xdean.jex.util.string.StringUtil;
import xdean.jfxex.support.skin.SkinStyle;

public enum DefaultSkin implements SkinStyle {
  CLASSIC("classic"),
  DEFAULT("default"),
  METAL("metal"),
  PINK("pink");

  private static final String CSS_PATH = "/css/skin/";

  private String path;

  private DefaultSkin(String name) {
    try {
      this.path = DefaultSkin.class.getResource(CSS_PATH + name + ".bss").toExternalForm();
    } catch (NullPointerException e) {// If the resource not exist
      this.path = DefaultSkin.class.getResource(CSS_PATH + name + ".css").toExternalForm();
    }
  }

  @Override
  public String getURL() {
    return path;
  }

  @Override
  public String getName() {
    return StringUtil.upperFirst(toString());
  }
}