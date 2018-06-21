package xdean.css.editor.context.setting;

import xdean.auto.message.AutoMessage;

@AutoMessage(path = "/message/settings.properties", generatedName = "RawSettingKeys")
public interface SettingKeys {
  String GENERAL = RawSettingKeys.OPTION_GENERAL;

  interface General {

    String COMMON = RawSettingKeys.OPTION_GENERAL_COMMON;

    interface Common {
      String AUTO_SUGGEST = RawSettingKeys.OPTION_GENERAL_COMMON_AUTO_SUGGEST;
      String SHOW_LINE = RawSettingKeys.OPTION_GENERAL_COMMON_SHOW_LINE;
      String OPEN_LAST = RawSettingKeys.OPTION_GENERAL_COMMON_OPEN_LAST;
      String CHARSET = RawSettingKeys.OPTION_GENERAL_COMMON_CHARSET;
    }

    String TEXT = RawSettingKeys.OPTION_GENERAL_TEXT;

    interface Text {
      String FONT_FAMILY = RawSettingKeys.OPTION_GENERAL_TEXT_FONT_FAMILY;
      String FONT_SIZE = RawSettingKeys.OPTION_GENERAL_TEXT_FONT_SIZE;
      String WRAP_TEXT = RawSettingKeys.OPTION_GENERAL_TEXT_WRAP_TEXT;
    }
  }

  String KEY = RawSettingKeys.OPTION_KEY;

  interface Key {
    String SUGGEST = RawSettingKeys.OPTION_KEY_SUGGEST;
    String FORMAT = RawSettingKeys.OPTION_KEY_FORMAT;
    String COMMENT = RawSettingKeys.OPTION_KEY_COMMENT;
    String FIND = RawSettingKeys.OPTION_KEY_FIND;
    String CLOSE = RawSettingKeys.OPTION_KEY_CLOSE;
  }

  interface Find {
    String REGEX = RawSettingKeys.OPTION_FIND_REGEX;
    String WRAP_SEARCH = RawSettingKeys.OPTION_FIND_WRAP_SEARCH;
    String CASE_SENSITIVE = RawSettingKeys.OPTION_FIND_CASE_SENSITIVE;
  }

  String LANGUAGE = RawSettingKeys.OPTION_LANGUAGE;
  String SKIN = RawSettingKeys.OPTION_SKIN;
  String RECENT_LOC = RawSettingKeys.OPTION_RECENT_LOCATION;
}
