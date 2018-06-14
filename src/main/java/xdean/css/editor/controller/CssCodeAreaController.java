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
import xdean.css.context.CSSContext;
import xdean.css.editor.context.option.Options;
import xdean.css.editor.control.CssCodeArea;
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

  @Inject
  Options options;

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
    codeArea.wrapTextProperty().bind(options.wrapSearch().valueProperty());
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
        .map(this::getOverrideCaretCSS)
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
    options.fontSize().setValue(options.fontSize().getValue() + 1);
  }

  private void zoomOut() {
    options.fontSize().setValue(options.fontSize().getValue() - 1);
  }

  public BooleanProperty overrideProperty() {
    return override;
  }

  public CodeArea getCodeArea() {
    return codeArea;
  }

  private void bindFont(Node node) {
    options.fontSize().valueProperty().addListener(weak(node, (ob, obj) -> updateFont(obj)));
    options.fontFamily().valueProperty().addListener(weak(node, (ob, obj) -> updateFont(obj)));
    updateFont(node);
  }

  private void updateFont(Node node) {
    node.setStyle(
        String.format("-fx-font-family: '%s'; -fx-font-size: %d;",
            options.fontFamily().getValue(), options.fontSize().getValue()));
  }

  private void bindLineNumber(StyledTextArea<?, ?> textArea, IntFunction<Node> factory) {
    options.showLineNo().valueProperty().addListener((ob, o, n) -> {
      if (n) {
        textArea.setParagraphGraphicFactory(factory);
      } else {
        textArea.setParagraphGraphicFactory(null);
      }
    });
    if (options.showLineNo().getValue()) {
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
        Font.font(options.fontFamily().getValue(), options.fontSize().getValue()));
  }

  public ReadOnlyBooleanProperty modifiedProperty() {
    return modify.modifiedProperty();
  }
}
