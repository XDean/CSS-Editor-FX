package xdean.css.editor.controller.comp;

import impl.org.controlsfx.skin.AutoCompletePopup;
import impl.org.controlsfx.skin.AutoCompletePopupSkin;

import java.util.Collection;
import java.util.function.BiFunction;

import javafx.beans.value.ChangeListener;
import javafx.scene.control.IndexRange;
import javafx.scene.control.ListView;
import javafx.scene.control.Skin;
import javafx.stage.PopupWindow;

import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.PopupAlignment;

public final class AutoCompletionCodeAreaBind {

  CodeArea codeArea;
  BiFunction<String, Integer, Collection<String>> suggestionProvider;
  BiFunction<String, Integer, IndexRange> replaceTextProvider;
  AutoCompletePopup<String> popup;

  /**
   * 
   * @param codeArea
   * @param suggestionProvider
   * @param replaceTextProvider
   *          When a suggestion entered, which range of text should be replaced.
   */
  public AutoCompletionCodeAreaBind(CodeArea codeArea,
      BiFunction<String, Integer, Collection<String>> suggestionProvider,
      BiFunction<String, Integer, IndexRange> replaceTextProvider) {
    this.codeArea = codeArea;
    this.suggestionProvider = suggestionProvider;
    this.replaceTextProvider = replaceTextProvider;
    this.popup = new AutoCompletePopup<>();

    codeArea.textProperty().addListener(textChangeListener);
    codeArea.focusedProperty().addListener(focusChangedListener);

    codeArea.setPopupWindow(popup);
    codeArea.setPopupAlignment(PopupAlignment.CARET_BOTTOM);

    popup.setOnSuggestion(sce -> {
      completeUserInput(sce.getSuggestion());
      hidePopup();
    });
  }

  public void showPopup() {
    Collection<String> suggestions = suggestionProvider.apply(codeArea.getText(), codeArea.getCaretPosition());
    if (suggestions.isEmpty()) {
      hidePopup();
    } else {
      PopupWindow popupWindow = codeArea.getPopupWindow();
      if (popupWindow != popup) {
        popupWindow.hide();
        codeArea.setPopupWindow(popup);
      }
      popup.getSuggestions().setAll(suggestions);
      selectFirstSuggestion(popup);
      if (popup.isShowing() == false) {
        popup.show(codeArea.getScene().getWindow());
      }
    }
  }

  public void hidePopup() {
    popup.hide();
  }

  private void completeUserInput(String suggestion) {
    IndexRange range = replaceTextProvider.apply(codeArea.getText(), codeArea.getCaretPosition());
    codeArea.deleteText(range);
    codeArea.insertText(range.getStart(), suggestion);
    codeArea.moveTo(range.getStart() + suggestion.length());
  }

  private final ChangeListener<String> textChangeListener = (obs, oldText, newText) -> {
    if (codeArea.isFocused() && popup.isShowing()) {
      showPopup();
    }
  };

  private final ChangeListener<Boolean> focusChangedListener = (obs, oldFocused, newFocused) -> {
    if (newFocused == false) {
      hidePopup();
    }
  };

  private static void selectFirstSuggestion(AutoCompletePopup<?> autoCompletionPopup) {
    Skin<?> skin = autoCompletionPopup.getSkin();
    if (skin instanceof AutoCompletePopupSkin) {
      AutoCompletePopupSkin<?> au = (AutoCompletePopupSkin<?>) skin;
      ListView<?> li = (ListView<?>) au.getNode();
      if (li.getItems() != null && !li.getItems().isEmpty()) {
        li.getSelectionModel().select(0);
      }
    }
  }
}
