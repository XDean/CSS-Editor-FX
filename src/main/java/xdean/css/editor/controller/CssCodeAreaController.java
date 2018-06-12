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
import org.fxmisc.richtext.model.NavigationActions.SelectionPolicy;

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
import xdean.css.context.CSSContext;
import xdean.css.editor.config.Key;
import xdean.css.editor.config.Options;
import xdean.css.editor.control.CssCodeArea;
import xdean.css.editor.feature.CSSFormat;
import xdean.css.editor.feature.CssCodeAreaFeature;
import xdean.jex.extra.StringURL;
import xdean.jex.util.string.StringUtil;
import xdean.jex.util.task.If;
import xdean.jex.util.task.TaskUtil;
import xdean.jfx.spring.FxInitializable;
import xdean.jfx.spring.annotation.FxComponent;
import xdean.jfxex.extra.ModifiableObject;

@FxComponent
public class CssCodeAreaController implements FxInitializable {

  public final CssCodeArea codeArea = new CssCodeArea();

  CSSContext lastContext;

  @Inject
  List<CssCodeAreaFeature> features;

  BooleanProperty override;
  ModifiableObject modify = new ModifiableObject();

  @Override
  public void initAfterFxSpringReady() {
    codeArea.getStylesheets().add(
        TaskUtil.firstSuccess(
            () -> CssCodeAreaController.class.getResource("/css/css-highlighting.bss").toExternalForm(),
            () -> CssCodeAreaController.class.getResource("/css/css-highlighting.css").toExternalForm()));

    features.forEach(f -> f.bind(codeArea));

    Observable<KeyEvent> keyPress = JavaFxObservable.eventsOf(codeArea, KeyEvent.KEY_PRESSED).share();
    // font and line number
    bindFont(codeArea);
    bindLineNumber(codeArea, idx -> {
      Node node = LineNumberFactory.get(codeArea, i -> "%" + i + "d").apply(idx);
      bindFont(node);
      return node;
    });
    // zoom
    keyPress.map(KeyEvent::getCode)
        .filter(KeyCode.CONTROL::equals)
        .subscribe(e -> codeArea.addEventFilter(ScrollEvent.SCROLL, zoom));
    Observable.merge(
        JavaFxObservable.eventsOf(codeArea, KeyEvent.KEY_RELEASED)
            .map(KeyEvent::getCode)
            .filter(KeyCode.CONTROL::equals),
        JavaFxObservable.valuesOf(codeArea.focusedProperty())
            .filter(b -> b == false))
        .subscribe(e -> codeArea.removeEventFilter(ScrollEvent.SCROLL, zoom));
    // context add to suggestion
    JavaFxObservable.valuesOf(codeArea.textProperty())
        .debounce(700, TimeUnit.MILLISECONDS)
        .subscribe(this::refreshContextSuggestion);
    // wrap word
    codeArea.wrapTextProperty().bind(Options.wrapText.property());
    keyPress.filter(Key.COMMENT.get()::match)
        .filter(e -> e.isConsumed() == false)
        .doOnNext(KeyEvent::consume)
        .subscribe(e -> comment());
    // auto select word's first '-'
    JavaFxObservable.eventsOf(codeArea, MouseEvent.MOUSE_PRESSED)
        .filter(e -> e.getClickCount() == 2)
        .subscribe(e -> {
          if (e.isConsumed()) {
            IndexRange selection = codeArea.getSelection();
            if (codeArea.getText().charAt(selection.getStart() - 1) == '-') {
              codeArea.selectRange(selection.getStart() - 1, selection.getEnd());
            }
          }
        });
    // toggle insert and override
    override = new SimpleBooleanProperty(false);
    StringProperty overrideCSS = new SimpleStringProperty();
    keyPress.filter(e -> e.getCode() == KeyCode.INSERT)
        .subscribe(e -> override.set(!override.get()));
    JavaFxObservable.valuesOf(codeArea.caretPositionProperty())
        .filter(c -> override.get())
        .map(c -> uncatch(() -> codeArea.getText().charAt(c)))
        .map(c -> c == null ? '\n' : c)
        .map(CssCodeAreaController::getOverrideCaretCSS)
        .subscribe(s -> overrideCSS.set(s));
    ChangeListener<? super String> overrideCSSListener = (ob, o, n) -> {
      codeArea.getStylesheets().remove(o);
      codeArea.getStylesheets().add(n);
    };
    EventHandler<? super KeyEvent> overrideListener = e -> {
      IndexRange selection = codeArea.getSelection();
      String character = e.getCharacter();
      int caret = codeArea.getCaretPosition();
      char oldChar = codeArea.getText().charAt(caret);
      char newChar;
      if (selection.getLength() == 0 && character.length() == 1 && caret != codeArea.getText().length() - 1 &&
          !StringUtil.isControlCharacter(newChar = character.charAt(0)) && oldChar != '\n') {
        codeArea.replaceText(caret, caret + 1, newChar + "");
        codeArea.moveTo(caret + 1);
        e.consume();
      }
    };
    override.addListener((ob, o, n) -> {
      if (n) {
        overrideCSS.addListener(overrideCSSListener);
        codeArea.addEventFilter(KeyEvent.KEY_TYPED, overrideListener);
        int caretPosition = codeArea.getCaretPosition();
        codeArea.moveTo(caretPosition - 1);
        codeArea.moveTo(caretPosition);
      } else {
        overrideCSS.removeListener(overrideCSSListener);
        codeArea.removeEventFilter(KeyEvent.KEY_TYPED, overrideListener);
        codeArea.getStylesheets().remove(overrideCSS.get());
        overrideCSS.set(null);
      }
    });
    // charSet
    // charsetLocal.addListener((ob, o, n) -> {
    // String oldText = codeArea.getText();
    // String newText = new String(oldText.getBytes(o), n);
    // if (oldText.equals(newText) == false) {
    // codeArea.replaceText(newText);
    // codeArea.getUndoManager().forgetHistory();
    // }
    // });

    // modified
    modify.bindModified(codeArea.textProperty());
    codeArea.getUndoManager().atMarkedPositionProperty().addListener((ob, o, n) -> If.that(n).todo(() -> modify.saved()));
    modify.modifiedProperty().addListener((ob, o, n) -> If.that(n).ordo(() -> codeArea.getUndoManager().mark()));
  }

  public void comment() {
    selectLines(codeArea);
    String selectedText = codeArea.getSelectedText();
    IndexRange selection = codeArea.getSelection();
    codeArea.getUndoManager().preventMerge();
    codeArea.replaceSelection(CSSFormat.toggleComment(selectedText));
    codeArea.getUndoManager().preventMerge();
    codeArea.moveTo(selection.getStart(), SelectionPolicy.EXTEND);
  }

  public void format() {
    // TODO Format
  }

  private void refreshContextSuggestion(String text) {
    if (lastContext != null) {
      codeArea.context.remove(lastContext);
      lastContext = null;
    }
    lastContext = new CSSContext(text);
    codeArea.context.add(lastContext);
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
    Options.fontSize.set(Options.fontSize.get() + 1);
  }

  private void zoomOut() {
    Options.fontSize.set(Options.fontSize.get() - 1);
  }

  public BooleanProperty overrideProperty() {
    return override;
  }

  public CodeArea getCodeArea() {
    return codeArea;
  }

  private static void bindFont(Node node) {
    Options.fontSize.property().addListener(weak(node, (ob, obj) -> updateFont(obj)));
    Options.fontFamily.property().addListener(weak(node, (ob, obj) -> updateFont(obj)));
    updateFont(node);
  }

  private static void updateFont(Node node) {
    node.setStyle(
        String.format("-fx-font-family: '%s'; -fx-font-size: %d;",
            Options.fontFamily.get(), Options.fontSize.get()));
  }

  private static void bindLineNumber(StyledTextArea<?, ?> textArea, IntFunction<Node> factory) {
    Options.showLineNo.property().addListener((ob, o, n) -> {
      if (n) {
        textArea.setParagraphGraphicFactory(factory);
      } else {
        textArea.setParagraphGraphicFactory(null);
      }
    });
    if (Options.showLineNo.get()) {
      textArea.setParagraphGraphicFactory(factory);
    }
  }

  private static void selectLines(CodeArea area) {
    IndexRange origin = area.getSelection();

    area.moveTo(origin.getStart());
    area.lineStart(SelectionPolicy.CLEAR);
    int start = area.getCaretPosition();

    area.moveTo(origin.getEnd());
    area.lineEnd(SelectionPolicy.CLEAR);
    int end = area.getCaretPosition();

    area.selectRange(start, end);
  }

  private static String getOverrideCaretCSS(char c) {
    double width = Math.max(getTextSize(c + ""), 1);
    return StringURL.createURLString(String.format(
        ".caret {"
            + "-fx-scale-x: %f; "
            + "-fx-translate-x: %f;"
            + "}",
        width, width / 2));
  }

  public static double getTextSize(String text) {
    return Toolkit.getToolkit().getFontLoader().computeStringWidth(text,
        Font.font(Options.fontFamily.get(), Options.fontSize.get()));
  }

  public ReadOnlyBooleanProperty modifiedProperty() {
    return modify.modifiedProperty();
  }
}
