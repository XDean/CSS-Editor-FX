package xdean.css.editor.context.setting;

import static xdean.css.editor.context.setting.RawSettingKeys.*;

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

  interface Find {
    String REGEX = OPTION_FIND_REGEX;
    String WRAP_SEARCH = OPTION_FIND_WRAP_SEARCH;
    String CASE_SENSITIVE = OPTION_FIND_CASE_SENSITIVE;
  }

  String LANGUAGE = OPTION_LANGUAGE;
  String SKIN = OPTION_SKIN;
  String RECENT_LOC = OPTION_RECENT_LOCATION;



  interface File {
    String NEW = ACTION_FILE_NEW;
    String OPEN = ACTION_FILE_OPEN;
    String SAVE = ACTION_FILE_SAVE;
    String SAVE_AS = ACTION_FILE_SAVEAS;
    String CLOSE = ACTION_FILE_CLOSE;
    String REVERT = ACTION_FILE_REVERT;
    String EXIT = ACTION_FILE_EXIT;
  }

  interface Edit {
    String UNDO = ACTION_EDIT_UNDO;
    String REDO = ACTION_EDIT_REDO;
    String SUGGEST = ACTION_EDIT_SUGGEST;
    String FORMAT = ACTION_EDIT_FORMAT;
    String COMMENT = ACTION_EDIT_COMMENT;
    String FIND = ACTION_EDIT_FIND;
  }
}
