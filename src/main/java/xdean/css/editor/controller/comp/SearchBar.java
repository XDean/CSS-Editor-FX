package xdean.css.editor.controller.comp;

import static xdean.jex.util.lang.ExceptionUtil.uncatch;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.controlsfx.control.textfield.TextFields;
import org.fxmisc.richtext.CodeArea;

import io.reactivex.rxjavafx.observables.JavaFxObservable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import rx.functions.Func3;
import xdean.css.editor.config.Options;
import xdean.jex.util.string.StringUtil;
import xdean.jex.util.task.If;
import xdean.jfx.ex.util.bean.BeanUtil;

public final class SearchBar extends HBox {

  private TextField findField;
  private Button findButton;
  private CheckBox caseSensitive;
  private CheckBox regex;
  private CheckBox wrapSearch;

  private ObservableValue<CodeArea> codeArea;

  private BooleanProperty showing;

  public SearchBar(ObservableValue<CodeArea> codeArea) {
    super();
    this.codeArea = codeArea;
    this.showing = new SimpleBooleanProperty(false);
    initUI();
    initBind();
  }

  private void initUI() {
    findField = TextFields.createClearableTextField();
    findButton = new Button("Find");
    caseSensitive = new CheckBox("Case Sensitive");
    regex = new CheckBox("Regex");
    wrapSearch = new CheckBox("Wrap Search");

    getChildren().addAll(findField, findButton, regex, caseSensitive, wrapSearch);
    setPadding(new Insets(5));
    setSpacing(5);
    setAlignment(Pos.CENTER_LEFT);
  }

  private void initBind() {
    regex.selectedProperty().bindBidirectional(Options.findRegex.property());
    caseSensitive.selectedProperty().bindBidirectional(Options.findCaseSensitive.property());
    wrapSearch.selectedProperty().bindBidirectional(Options.findWrapText.property());

    JavaFxObservable.valuesOf(visibleProperty())
        .subscribe(
            v -> If.that(v)
                .todo(() -> findField.requestFocus())
                .ordo(() -> uncatch(() -> codeArea.getValue().requestFocus())));
    visibleProperty().bind(showing.and(BeanUtil.isNotNull(codeArea)));
    managedProperty().bind(visibleProperty());
    findButton.setOnAction(e -> find());
    findField.setOnAction(e -> find());
  }

  private void find() {
    if (find(codeArea.getValue().getCaretPosition()) == false && wrapSearch.isSelected()) {
      find(0);
    }
  }

  private boolean find(int offset) {
    CodeArea area = codeArea.getValue();
    String findText = findField.getText();
    if (regex.isSelected()) {
      return regexFind(area, findText, offset);
    } else {
      return simpleFind(area, findText, offset);
    }
  }

  private boolean simpleFind(CodeArea area, String text, int offset) {
    Func3<String, String, Integer, Integer> func = caseSensitive.isSelected() ? String::indexOf
        : StringUtil::indexOfIgnoreCase;
    int index = func.call(area.getText(), text, offset);
    if (index != -1) {
      area.selectRange(index, index + text.length());
      return true;
    }
    return false;
  }

  private boolean regexFind(CodeArea area, String regex, int offset) {
    if (caseSensitive.isSelected() && regex.startsWith("(?i)")) {
      regex = "?i" + regex;
    }
    Matcher matcher = Pattern.compile(regex).matcher(area.getText().substring(offset));
    if (matcher.find()) {
      area.selectRange(matcher.start() + offset, matcher.end() + offset);
      return true;
    }
    return false;
  }

  public void toggle() {
    showing.set(!showing.get());
  }
}
