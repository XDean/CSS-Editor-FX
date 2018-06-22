package xdean.css.editor.context.action;

import static xdean.css.editor.context.action.RawActionKeys.*;
import xdean.auto.message.AutoMessage;

@AutoMessage(path = "/message/actions.properties", generatedName = "RawActionKeys")
public interface ActionKeys {
  String SUGGEST = ACTION_SUGGEST;
  String FORMAT = ACTION_FORMAT;
  String COMMENT = ACTION_COMMENT;
  String FIND = ACTION_FIND;
  String CLOSE = ACTION_CLOSE;
}
