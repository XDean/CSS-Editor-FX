package xdean.css.editor.controller;

import static xdean.jex.util.lang.ExceptionUtil.uncatch;
import static xdean.jfxex.bean.ListenerUtil.weak;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.IntFunction;

import javax.inject.Inject;

import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.StyledTextArea;

import com.sun.javafx.tk.Toolkit;

import io.reactivex.Observable;
import io.reactivex.rxjavafx.observables.JavaFxObservable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.IndexRange;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.text.Font;
import xdean.css.editor.context.setting.OtherSettings;
import xdean.css.editor.context.setting.PreferenceSettings;
import xdean.css.editor.control.CssEditor;
import xdean.css.editor.feature.CssEditorFeature;
import xdean.css.editor.model.CssContext;
import xdean.jex.extra.StringURL;
import xdean.jex.util.string.StringUtil;
import xdean.jex.util.task.If;
import xdean.jex.util.task.TaskUtil;
import xdean.jfx.spring.FxInitializable;
import xdean.jfx.spring.annotation.FxComponent;
import xdean.jfxex.extra.ModifiableObject;

@FxComponent
public class CssEditorController implements FxInitializable {

  public final CssEditor editor = new CssEditor();

  CssContext lastContext;

  @Inject
  List<CssEditorFeature> features;

  @Inject
  PreferenceSettings preference;

  @Inject
  OtherSettings otherSettings;

  BooleanProperty override;
  ModifiableObject modify = new ModifiableObject();

  @Override
  public void initAfterFxSpringReady() {
    editor.getStylesheets().add(
        TaskUtil.firstSuccess(
            () -> CssEditorController.class.getResource("/css/css-highlighting.bss").toExternalForm(),
            () -> CssEditorController.class.getResource("/css/css-highlighting.css").toExternalForm()));

    features.forEach(f -> f.bind(editor));

    Observable<KeyEvent> keyPress = JavaFxObservable.eventsOf(editor, KeyEvent.KEY_PRESSED).share();
    // font and line number
    bindFont(editor);
    bindLineNumber(editor, idx -> {
      Node node = LineNumberFactory.get(editor, i -> "%" + i + "d").apply(idx);
      bindFont(node);
      return node;
    });
    // zoom
    keyPress.map(KeyEvent::getCode)
        .filter(KeyCode.CONTROL::equals)
        .subscribe(e -> editor.addEventFilter(ScrollEvent.SCROLL, zoom));
    Observable.merge(
        JavaFxObservable.eventsOf(editor, KeyEvent.KEY_RELEASED)
            .map(KeyEvent::getCode)
            .filter(KeyCode.CONTROL::equals),
        JavaFxObservable.valuesOf(editor.focusedProperty())
            .filter(b -> b == false))
        .subscribe(e -> editor.removeEventFilter(ScrollEvent.SCROLL, zoom));
    // context add to suggestion
    JavaFxObservable.valuesOf(editor.textProperty())
        .debounce(700, TimeUnit.MILLISECONDS)
        .subscribe(this::refreshContextSuggestion);
    // wrap word
    editor.wrapTextProperty().bind(otherSettings.wrapSearch().valueProperty());
    // auto select word's first '-'
    JavaFxObservable.eventsOf(editor, MouseEvent.MOUSE_PRESSED)
        .filter(e -> e.getClickCount() == 2)
        .subscribe(e -> {
          if (e.isConsumed()) {
            IndexRange selection = editor.getSelection();
            if (editor.getText().charAt(selection.getStart() - 1) == '-') {
              editor.selectRange(selection.getStart() - 1, selection.getEnd());
            }
          }
        });
    // toggle insert and override
    override = new SimpleBooleanProperty(false);
    StringProperty overrideCSS = new SimpleStringProperty();
    keyPress.filter(e -> e.getCode() == KeyCode.INSERT)
        .subscribe(e -> override.set(!override.get()));
    JavaFxObservable.valuesOf(editor.caretPositionProperty())
        .filter(c -> override.get())
        .map(c -> uncatch(() -> editor.getText().charAt(c)))
        .map(c -> c == null ? '\n' : c)
        .map(this::getOverrideCaretCSS)
        .subscribe(s -> overrideCSS.set(s));
    ChangeListener<? super String> overrideCSSListener = (ob, o, n) -> {
      editor.getStylesheets().remove(o);
      editor.getStylesheets().add(n);
    };
    EventHandler<? super KeyEvent> overrideListener = e -> {
      IndexRange selection = editor.getSelection();
      String character = e.getCharacter();
      int caret = editor.getCaretPosition();
      char oldChar = editor.getText().charAt(caret);
      char newChar;
      if (selection.getLength() == 0 && character.length() == 1 && caret != editor.getText().length() - 1 &&
          !StringUtil.isControlCharacter(newChar = character.charAt(0)) && oldChar != '\n') {
        editor.replaceText(caret, caret + 1, newChar + "");
        editor.moveTo(caret + 1);
        e.consume();
      }
    };
    override.addListener((ob, o, n) -> {
      if (n) {
        overrideCSS.addListener(overrideCSSListener);
        editor.addEventFilter(KeyEvent.KEY_TYPED, overrideListener);
        int caretPosition = editor.getCaretPosition();
        editor.moveTo(caretPosition - 1);
        editor.moveTo(caretPosition);
      } else {
        overrideCSS.removeListener(overrideCSSListener);
        editor.removeEventFilter(KeyEvent.KEY_TYPED, overrideListener);
        editor.getStylesheets().remove(overrideCSS.get());
        overrideCSS.set(null);
      }
    });
    // charSet
    // charsetLocal.addListener((ob, o, n) -> {
    // String oldText = editor.getText();
    // String newText = new String(oldText.getBytes(o), n);
    // if (oldText.equals(newText) == false) {
    // editor.replaceText(newText);
    // editor.getUndoManager().forgetHistory();
    // }
    // });

    // modified
    modify.bindModified(editor.textProperty());
    editor.getUndoManager().atMarkedPositionProperty().addListener((ob, o, n) -> If.that(n).todo(() -> modify.saved()));
    modify.modifiedProperty().addListener((ob, o, n) -> If.that(n).ordo(() -> editor.getUndoManager().mark()));
  }

  public void format() {
    // TODO Format
  }

  private void refreshContextSuggestion(String text) {
    if (lastContext != null) {
      editor.context.remove(lastContext);
      lastContext = null;
    }
    lastContext = new CssContext(text);
    editor.context.add(lastContext);
  }

  private EventHandler<ScrollEvent> zoom = e -> {
    if (e.isConsumed()) {
      return;
    }
    e.consume();
    if (e.getDeltaY() > 0) {
      zoomIn();
    } else {
      zoomOut();
    }
  };

  private void zoomIn() {
    preference.fontSize().setValue(preference.fontSize().getValue() + 1);
  }

  private void zoomOut() {
    preference.fontSize().setValue(preference.fontSize().getValue() - 1);
  }

  public BooleanProperty overrideProperty() {
    return override;
  }

  public CodeArea getEditor() {
    return editor;
  }

  private void bindFont(Node node) {
    preference.fontSize().valueProperty().addListener(weak(node, (ob, obj) -> updateFont(obj)));
    preference.fontFamily().valueProperty().addListener(weak(node, (ob, obj) -> updateFont(obj)));
    updateFont(node);
  }

  private void updateFont(Node node) {
    node.setStyle(
        String.format("-fx-font-family: '%s'; -fx-font-size: %d;",
            preference.fontFamily().getValue(), preference.fontSize().getValue()));
  }

  private void bindLineNumber(StyledTextArea<?, ?> textArea, IntFunction<Node> factory) {
    preference.showLineNo().valueProperty().addListener((ob, o, n) -> {
      if (n) {
        textArea.setParagraphGraphicFactory(factory);
      } else {
        textArea.setParagraphGraphicFactory(null);
      }
    });
    if (preference.showLineNo().getValue()) {
      textArea.setParagraphGraphicFactory(factory);
    }
  }

  private String getOverrideCaretCSS(char c) {
    double width = Math.max(getTextSize(c + ""), 1);
    return StringURL.createURLString(String.format(
        ".caret {"
            + "-fx-scale-x: %f; "
            + "-fx-translate-x: %f;"
            + "}",
        width, width / 2));
  }

  public double getTextSize(String text) {
    return Toolkit.getToolkit().getFontLoader().computeStringWidth(text,
        Font.font(preference.fontFamily().getValue(), preference.fontSize().getValue()));
  }

  public ReadOnlyBooleanProperty modifiedProperty() {
    return modify.modifiedProperty();
  }
}
