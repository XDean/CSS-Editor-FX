package xdean.css.editor.control;

import static xdean.jex.util.cache.CacheUtil.cache;
import static xdean.jex.util.lang.ExceptionUtil.uncatch;
import static xdean.jfxex.bean.BeanUtil.map;
import static xdean.jfxex.bean.BeanUtil.mapToBoolean;
import static xdean.jfxex.bean.ListenerUtil.weak;

import java.nio.file.Files;
import java.util.Collections;
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
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.event.EventHandler;
import javafx.event.EventTarget;
import javafx.scene.Node;
import javafx.scene.control.IndexRange;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.text.Font;
import xdean.css.editor.context.setting.OtherSettings;
import xdean.css.editor.context.setting.PreferenceSettings;
import xdean.css.editor.context.setting.model.CssEditorKeyOption;
import xdean.css.editor.feature.CssEditorFeature;
import xdean.css.editor.model.CssContext;
import xdean.css.editor.model.FileWrapper;
import xdean.css.editor.service.RecentFileService;
import xdean.jex.extra.StringURL;
import xdean.jex.util.string.StringUtil;
import xdean.jex.util.task.If;
import xdean.jex.util.task.TaskUtil;
import xdean.jfx.spring.FxInitializable;
import xdean.jfx.spring.annotation.FxComponent;
import xdean.jfxex.extra.ModifiableObject;

@FxComponent
public class CssEditor extends CodeArea implements FxInitializable, EventTarget {

  public final CssContext context = CssContext.createByDefault();
  private CssContext lastContext;
  private @Inject RecentFileService recentFileService;
  private @Inject List<CssEditorKeyOption<?>> keys = Collections.emptyList();
  private @Inject List<CssEditorFeature> features = Collections.emptyList();
  private @Inject PreferenceSettings preference;
  private @Inject OtherSettings otherSettings;
  private final BooleanProperty override = new SimpleBooleanProperty(this, "overrride");
  private final ModifiableObject modify = new ModifiableObject();
  private final ObjectProperty<FileWrapper> file = new SimpleObjectProperty<>(this, "file", FileWrapper.newFile(0));
  private final BooleanProperty active = new SimpleBooleanProperty(this, "active");

  public ObjectProperty<FileWrapper> fileProperty() {
    return file;
  }

  public ObjectBinding<String> nameBinding() {
    return cache(this, "nameBinding", () -> map(file, f -> f.getFileName()));
  }

  public BooleanBinding isNewBinding() {
    return cache(this, "isNewBinding", () -> mapToBoolean(file, f -> f.isNewFile()));
  }

  public BooleanProperty activeProperty() {
    return active;
  }

  @Override
  public void initAfterFxSpringReady() {
    this.getStylesheets().add(
        TaskUtil.firstSuccess(
            () -> CssEditor.class.getResource("/css/css-highlighting.bss").toExternalForm(),
            () -> CssEditor.class.getResource("/css/css-highlighting.css").toExternalForm()));

    // recent
    file.addListener((ob, o, n) -> {
      if (n.isExistFile()) {
        recentFileService.setLatestFile(n.getExistFile().get());
        reload();
      }
    });

    keys.forEach(f -> f.bind(this));
    features.forEach(f -> f.bind(this));

    Observable<KeyEvent> keyPress = JavaFxObservable.eventsOf(this, KeyEvent.KEY_PRESSED).share();
    // font and line number
    bindFont(this);
    bindLineNumber(this, idx -> {
      Node node = LineNumberFactory.get(this, i -> "%" + i + "d").apply(idx);
      bindFont(node);
      return node;
    });
    // zoom
    keyPress.map(KeyEvent::getCode)
        .filter(KeyCode.CONTROL::equals)
        .subscribe(e -> this.addEventFilter(ScrollEvent.SCROLL, zoom));
    Observable.merge(
        JavaFxObservable.eventsOf(this, KeyEvent.KEY_RELEASED)
            .map(KeyEvent::getCode)
            .filter(KeyCode.CONTROL::equals),
        JavaFxObservable.valuesOf(this.focusedProperty())
            .filter(b -> b == false))
        .subscribe(e -> this.removeEventFilter(ScrollEvent.SCROLL, zoom));
    // context add to suggestion
    JavaFxObservable.valuesOf(this.textProperty())
        .debounce(700, TimeUnit.MILLISECONDS)
        .subscribe(this::refreshContextSuggestion);
    // wrap word
    this.wrapTextProperty().bind(otherSettings.wrapSearch().valueProperty());
    // auto select word's first '-'
    JavaFxObservable.eventsOf(this, MouseEvent.MOUSE_PRESSED)
        .filter(e -> e.getClickCount() == 2)
        .subscribe(e -> {
          if (e.isConsumed()) {
            IndexRange selection = this.getSelection();
            if (this.getText().charAt(selection.getStart() - 1) == '-') {
              this.selectRange(selection.getStart() - 1, selection.getEnd());
            }
          }
        });
    // toggle insert and override
    StringProperty overrideCSS = new SimpleStringProperty();
    keyPress.filter(e -> e.getCode() == KeyCode.INSERT)
        .subscribe(e -> override.set(!override.get()));
    JavaFxObservable.valuesOf(this.caretPositionProperty())
        .filter(c -> override.get())
        .map(c -> uncatch(() -> this.getText().charAt(c)))
        .map(c -> c == null ? '\n' : c)
        .map(this::getOverrideCaretCSS)
        .subscribe(s -> overrideCSS.set(s));
    ChangeListener<? super String> overrideCSSListener = (ob, o, n) -> {
      this.getStylesheets().remove(o);
      this.getStylesheets().add(n);
    };
    EventHandler<? super KeyEvent> overrideListener = e -> {
      IndexRange selection = this.getSelection();
      String character = e.getCharacter();
      int caret = this.getCaretPosition();
      char oldChar = this.getText().charAt(caret);
      char newChar;
      if (selection.getLength() == 0 && character.length() == 1 && caret != this.getText().length() - 1 &&
          !StringUtil.isControlCharacter(newChar = character.charAt(0)) && oldChar != '\n') {
        this.replaceText(caret, caret + 1, newChar + "");
        this.moveTo(caret + 1);
        e.consume();
      }
    };
    override.addListener((ob, o, n) -> {
      if (n) {
        overrideCSS.addListener(overrideCSSListener);
        this.addEventFilter(KeyEvent.KEY_TYPED, overrideListener);
        int caretPosition = this.getCaretPosition();
        this.moveTo(caretPosition - 1);
        this.moveTo(caretPosition);
      } else {
        overrideCSS.removeListener(overrideCSSListener);
        this.removeEventFilter(KeyEvent.KEY_TYPED, overrideListener);
        this.getStylesheets().remove(overrideCSS.get());
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
    modify.bindModified(this.textProperty());
    this.getUndoManager().atMarkedPositionProperty().addListener((ob, o, n) -> If.that(n).todo(() -> modify.saved()));
    modify.modifiedProperty().addListener((ob, o, n) -> If.that(n).ordo(() -> this.getUndoManager().mark()));
  }

  public void format() {
    // TODO Format
  }

  private void refreshContextSuggestion(String text) {
    if (lastContext != null) {
      this.context.remove(lastContext);
      lastContext = null;
    }
    lastContext = new CssContext(text);
    this.context.add(lastContext);
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

  public void reload() {
    file.get().getExistFile().ifPresent(p -> uncatch(() -> {
      replaceText(new String(Files.readAllBytes(p), preference.charset().getValue()));
      moveTo(0);
      getUndoManager().forgetHistory();
      modify().saved();
    }));
  }

  private void zoomIn() {
    preference.fontSize().setValue(preference.fontSize().getValue() + 1);
  }

  private void zoomOut() {
    preference.fontSize().setValue(preference.fontSize().getValue() - 1);
  }

  public BooleanProperty overrideProperty() {
    return override;
  }

  public ModifiableObject modify() {
    return modify;
  }

  public CodeArea getEditor() {
    return this;
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

  public void fire(CssEditorKeyOption keyOption) {
    fireEvent(keyOption.getEvent(this));
  }
}
