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
import xdean.css.editor.context.setting.EditActions;
import xdean.css.editor.control.CssEditor;
import xdean.css.editor.feature.suggestion.CssSuggestionService;
import xdean.css.editor.model.CssContext;

@Service
public class SuggestionFeature implements CssEditorFeature {

  @Inject
  CssSuggestionService cssSuggestion;

  @Inject
  EditActions keys;

  private final Collection<KeyCombination> legalPrefix = Arrays.asList(
      new KeyCodeCombination(KeyCode.PERIOD),
      new KeyCodeCombination(KeyCode.DIGIT3, KeyCombination.SHIFT_DOWN),
      new KeyCodeCombination(KeyCode.MINUS));

  @Override
  public void bind(CssEditor cssEditor) {
    new InnerController(cssEditor);
  }

  private boolean shouldSuggest(KeyEvent e) {
    if (legalPrefix.stream().filter(c -> c.match(e)).count() > 0) {
      return true;
    } else if (keys.suggest().getValue().match(e)) {
      return true;
    } else {
      return false;
    }
  }

  private class InnerController {

    CodeArea editor;
    CssContext context;

    AutoCompletePopup<String> popup = new AutoCompletePopup<>();

    public InnerController(CssEditor cssEditor) {
      this.editor = cssEditor;
      this.context = cssEditor.context;

      editor.textProperty().addListener((ob, o, n) -> {
        if (editor.isFocused() && popup.isShowing()) {
          showPopup();
        }
      });
      editor.focusedProperty().addListener((ob, o, n) -> {
        if (n == false) {
          hidePopup();
        }
      });

      editor.setPopupWindow(popup);
      editor.setPopupAlignment(PopupAlignment.CARET_BOTTOM);

      popup.setOnSuggestion(sce -> {
        completeUserInput(sce.getSuggestion());
        hidePopup();
      });

      JavaFxObservable.eventsOf(editor, KeyEvent.KEY_PRESSED)
          .debounce(100, TimeUnit.MILLISECONDS)
          .filter(e -> shouldSuggest(e))
          .observeOn(JavaFxScheduler.platform())
          .subscribe(e -> showPopup());
    }

    public void showPopup() {
      Collection<String> suggestions = cssSuggestion.getSuggestion(editor.getText(), editor.getCaretPosition(), context);
      if (suggestions.isEmpty()) {
        hidePopup();
      } else {
        PopupWindow popupWindow = editor.getPopupWindow();
        if (popupWindow != popup) {
          popupWindow.hide();
          editor.setPopupWindow(popup);
        }
        popup.getSuggestions().setAll(suggestions);
        selectFirstSuggestion(popup);
        if (popup.isShowing() == false) {
          popup.show(editor.getScene().getWindow());
        }
      }
    }

    public void hidePopup() {
      popup.hide();
    }

    private void completeUserInput(String suggestion) {
      IndexRange range = cssSuggestion.getReplaceRange(editor.getText(), editor.getCaretPosition(), context);
      editor.deleteText(range);
      editor.insertText(range.getStart(), suggestion);
      editor.moveTo(range.getStart() + suggestion.length());
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
