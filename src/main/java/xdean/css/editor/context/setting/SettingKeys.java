package xdean.css.editor.context.setting;

import static xdean.css.editor.context.setting.RawSettingKeys.OPTION_FIND_CASE_SENSITIVE;
import static xdean.css.editor.context.setting.RawSettingKeys.OPTION_FIND_REGEX;
import static xdean.css.editor.context.setting.RawSettingKeys.OPTION_FIND_WRAP_SEARCH;
import static xdean.css.editor.context.setting.RawSettingKeys.OPTION_GENERAL;
import static xdean.css.editor.context.setting.RawSettingKeys.OPTION_GENERAL_COMMON;
import static xdean.css.editor.context.setting.RawSettingKeys.OPTION_GENERAL_COMMON_AUTO_SUGGEST;
import static xdean.css.editor.context.setting.RawSettingKeys.OPTION_GENERAL_COMMON_CHARSET;
import static xdean.css.editor.context.setting.RawSettingKeys.OPTION_GENERAL_COMMON_OPEN_LAST;
import static xdean.css.editor.context.setting.RawSettingKeys.OPTION_GENERAL_COMMON_SHOW_LINE;
import static xdean.css.editor.context.setting.RawSettingKeys.OPTION_GENERAL_TEXT;
import static xdean.css.editor.context.setting.RawSettingKeys.OPTION_GENERAL_TEXT_FONT_FAMILY;
import static xdean.css.editor.context.setting.RawSettingKeys.OPTION_GENERAL_TEXT_FONT_SIZE;
import static xdean.css.editor.context.setting.RawSettingKeys.OPTION_GENERAL_TEXT_WRAP_TEXT;
import static xdean.css.editor.context.setting.RawSettingKeys.OPTION_KEY;
import static xdean.css.editor.context.setting.RawSettingKeys.OPTION_LANGUAGE;
import static xdean.css.editor.context.setting.RawSettingKeys.OPTION_RECENT_LOCATION;
import static xdean.css.editor.context.setting.RawSettingKeys.OPTION_SKIN;

import xdean.auto.message.AutoMessage;

@AutoMessage(path = "/message/settings.properties", generatedName = "RawSettingKeys")
public interface SettingKeys {
  String GENERAL = OPTION_GENERAL;

  interface General {

    String COMMON = OPTION_GENERAL_COMMON;

    interface Common {
      String AUTO_SUGGEST = OPTION_GENERAL_COMMON_AUTO_SUGGEST;
      String SHOW_LINE = OPTION_GENERAL_COMMON_SHOW_LINE;
      String OPEN_LAST = OPTION_GENERAL_COMMON_OPEN_LAST;
      String CHARSET = OPTION_GENERAL_COMMON_CHARSET;
    }

    String TEXT = OPTION_GENERAL_TEXT;

    interface Text {
      String FONT_FAMILY = OPTION_GENERAL_TEXT_FONT_FAMILY;
      String FONT_SIZE = OPTION_GENERAL_TEXT_FONT_SIZE;
      String WRAP_TEXT = OPTION_GENERAL_TEXT_WRAP_TEXT;
    }
  }

  String KEY = OPTION_KEY;
  String KEY_PREFIX = "option.key.";

  interface Key {
    String UNDO = KEY_PREFIX + ActionKeys.Edit.UNDO;
    String REDO = KEY_PREFIX + ActionKeys.Edit.REDO;
    String SUGGEST = KEY_PREFIX + ActionKeys.Edit.SUGGEST;
    String FORMAT = KEY_PREFIX + ActionKeys.Edit.FORMAT;
    String COMMENT = KEY_PREFIX + ActionKeys.Edit.COMMENT;
    String FIND = KEY_PREFIX + ActionKeys.Edit.FIND;
    String CLOSE = KEY_PREFIX + ActionKeys.File.CLOSE;
  }

  interface Find {
    String REGEX = OPTION_FIND_REGEX;
    String WRAP_SEARCH = OPTION_FIND_WRAP_SEARCH;
    String CASE_SENSITIVE = OPTION_FIND_CASE_SENSITIVE;
  }

  String LANGUAGE = OPTION_LANGUAGE;
  String SKIN = OPTION_SKIN;
  String RECENT_LOC = OPTION_RECENT_LOCATION;
}
