package xdean.css.editor.context.option;

public interface OptionKeys {
  String ROOT = "option";

  String GENERAL = "option.general";

  interface General {

    String COMMON = "option.general.common";

    interface Common {
      String AUTO_SUGGEST = "option.general.common.auto-suggest";
      String SHOW_LINE = "option.general.common.show-line";
      String OPEN_LAST = "option.general.common.open-last";
      String CHARSET = "option.general.common.charset";
    }

    String TEXT = "option.general.text";

    interface Text {
      String FONT_FAMILY = "option.general.text.font-family";
      String FONT_SIZE = "option.general.text.font-size";
      String WRAP_TEXT = "option.general.text.wrap-text";
    }
  }

  String KEY = "option.key";

  interface Key {
    String SUGGEST = "option.key.suggest";
    String FORMAT = "option.key.format";
    String COMMENT = "option.key.comment";
    String FIND = "option.key.find";
    String CLOSE = "option.key.close";
  }

  String FIND = "option.other.find";

  interface Find {
    String REGEX = "option.other.find.regex";
    String WRAP_SEARCH = "option.other.find.wrap-search";
    String CASE_SENSITIVE = "option.other.find.case-sensitive";
  }

  String LANGUAGE = "option.language";
  String SKIN = "option.skin";
  String RECENT_LOC = "option.recent.location";
}
