package xdean.css.editor.feature;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.PopupAlignment;
import org.springframework.stereotype.Service;

import impl.org.controlsfx.skin.AutoCompletePopup;
import impl.org.controlsfx.skin.AutoCompletePopupSkin;
import io.reactivex.rxjavafx.observables.JavaFxObservable;
import io.reactivex.rxjavafx.schedulers.JavaFxScheduler;
import javafx.scene.control.IndexRange;
import javafx.scene.control.ListView;
import javafx.scene.control.Skin;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.stage.PopupWindow;
import xdean.css.context.CSSContext;
import xdean.css.editor.config.Key;
import xdean.css.editor.control.CssCodeArea;
import xdean.css.editor.feature.suggestion.CssSuggestionService;

@Service
public class SuggestionFeature implements CssCodeAreaFeature {

  @Inject
  CssSuggestionService cssSuggestion;

  private final Collection<KeyCombination> legalPrefix = Arrays.asList(
      new KeyCodeCombination(KeyCode.PERIOD),
      new KeyCodeCombination(KeyCode.DIGIT3, KeyCombination.SHIFT_DOWN),
      new KeyCodeCombination(KeyCode.MINUS));

  @Override
  public void bind(CssCodeArea cssCodeArea) {
    new Inner(cssCodeArea);
  }

  private boolean shouldSuggest(KeyEvent e) {
    if (legalPrefix.stream().filter(c -> c.match(e)).count() > 0) {
      return true;
    } else if (Key.SUGGEST.get().match(e)) {
      return true;
    } else {
      return false;
    }
  }

  private class Inner {

    CodeArea codeArea;
    CSSContext context;

    AutoCompletePopup<String> popup = new AutoCompletePopup<>();

    public Inner(CssCodeArea cssCodeArea) {
      this.codeArea = cssCodeArea;
      this.context = cssCodeArea.context;

      codeArea.textProperty().addListener((ob, o, n) -> {
        if (codeArea.isFocused() && popup.isShowing()) {
          showPopup();
        }
      });
      codeArea.focusedProperty().addListener((ob, o, n) -> {
        if (n == false) {
          hidePopup();
        }
      });

      codeArea.setPopupWindow(popup);
      codeArea.setPopupAlignment(PopupAlignment.CARET_BOTTOM);

      popup.setOnSuggestion(sce -> {
        completeUserInput(sce.getSuggestion());
        hidePopup();
      });

      JavaFxObservable.eventsOf(codeArea, KeyEvent.KEY_PRESSED)
          .debounce(100, TimeUnit.MILLISECONDS)
          .filter(e -> shouldSuggest(e))
          .observeOn(JavaFxScheduler.platform())
          .subscribe(e -> showPopup());
    }

    public void showPopup() {
      Collection<String> suggestions = cssSuggestion.getSuggestion(codeArea.getText(), codeArea.getCaretPosition(), context);
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
      IndexRange range = cssSuggestion.getReplaceRange(codeArea.getText(), codeArea.getCaretPosition(), context);
      codeArea.deleteText(range);
      codeArea.insertText(range.getStart(), suggestion);
      codeArea.moveTo(range.getStart() + suggestion.length());
    }

    private void selectFirstSuggestion(AutoCompletePopup<?> autoCompletionPopup) {
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
}
