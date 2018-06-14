package xdean.css.editor.context.setting;

import xdean.auto.message.AutoMessage;

@AutoMessage(path = "/message/settings.properties")
public interface SettingKeys {
  String GENERAL = Messages.OPTION_GENERAL;

  interface General {

    String COMMON = Messages.OPTION_GENERAL_COMMON;

    interface Common {
      String AUTO_SUGGEST = Messages.OPTION_GENERAL_COMMON_AUTO_SUGGEST;
      String SHOW_LINE = Messages.OPTION_GENERAL_COMMON_SHOW_LINE;
      String OPEN_LAST = Messages.OPTION_GENERAL_COMMON_OPEN_LAST;
      String CHARSET = Messages.OPTION_GENERAL_COMMON_CHARSET;
    }

    String TEXT = Messages.OPTION_GENERAL_TEXT;

    interface Text {
      String FONT_FAMILY = Messages.OPTION_GENERAL_TEXT_FONT_FAMILY;
      String FONT_SIZE = Messages.OPTION_GENERAL_TEXT_FONT_SIZE;
      String WRAP_TEXT = Messages.OPTION_GENERAL_TEXT_WRAP_TEXT;
    }
  }

  String KEY = Messages.OPTION_KEY;

  interface Key {
    String SUGGEST = Messages.OPTION_KEY_SUGGEST;
    String FORMAT = Messages.OPTION_KEY_FORMAT;
    String COMMENT = Messages.OPTION_KEY_COMMENT;
    String FIND = Messages.OPTION_KEY_FIND;
    String CLOSE = Messages.OPTION_KEY_CLOSE;
  }

  interface Find {
    String REGEX = Messages.OPTION_FIND_REGEX;
    String WRAP_SEARCH = Messages.OPTION_FIND_WRAP_SEARCH;
    String CASE_SENSITIVE = Messages.OPTION_FIND_CASE_SENSITIVE;
  }

  String LANGUAGE = Messages.OPTION_LANGUAGE;
  String SKIN = Messages.OPTION_SKIN;
  String RECENT_LOC = Messages.OPTION_RECENT_LOCATION;
}
