package xdean.css.editor.context.setting;

import static xdean.css.editor.context.setting.RawSettingKeys.*;

import xdean.auto.message.AutoMessage;

@AutoMessage(path = "/message/settings.properties", generatedName = "RawSettingKeysStub")
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
    String NEW = FILE_NEW;
    String OPEN = FILE_OPEN;
    String SAVE = FILE_SAVE;
    String SAVE_AS = FILE_SAVEAS;
    String CLOSE = FILE_CLOSE;
    String REVERT = FILE_REVERT;
    String EXIT = FILE_EXIT;
  }

  interface Edit {
    String UNDO = EDIT_UNDO;
    String REDO = EDIT_REDO;
    String SUGGEST = EDIT_SUGGEST;
    String FORMAT = EDIT_FORMAT;
    String COMMENT = EDIT_COMMENT;
    String FIND = EDIT_FIND;
  }

  interface Help {
    String SETTINGS = HELP_SETTINGS;
    String ABOUT = HELP_ABOUT;
    String HELP = HELP_HELP;
  }
}
