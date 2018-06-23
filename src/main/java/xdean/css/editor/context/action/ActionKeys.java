package xdean.css.editor.context.action;

import static xdean.css.editor.context.action.RawActionKeys.*;
import xdean.auto.message.AutoMessage;

@AutoMessage(path = "/message/actions.properties", generatedName = "RawActionKeys")
public interface ActionKeys {

  public interface File {
    String NEW = ACTION_FILE_NEW;
    String OPEN = ACTION_FILE_OPEN;
    String SAVE = ACTION_FILE_SAVE;
    String SAVE_AS = ACTION_FILE_SAVEAS;
    String CLOSE = ACTION_FILE_CLOSE;
  }

  public interface Edit {
    String UNDO = ACTION_EDIT_UNDO;
    String REDO = ACTION_EDIT_REDO;
    String SUGGEST = ACTION_EDIT_SUGGEST;
    String FORMAT = ACTION_EDIT_FORMAT;
    String COMMENT = ACTION_EDIT_COMMENT;
    String FIND = ACTION_EDIT_FIND;
  }
}
