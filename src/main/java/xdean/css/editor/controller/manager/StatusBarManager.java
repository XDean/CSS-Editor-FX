package xdean.css.editor.controller.manager;

import static xdean.jfx.ex.util.LayoutUtil.margin;
import static xdean.jfx.ex.util.LayoutUtil.minWidth;
import static xdean.jfx.ex.util.bean.BeanUtil.nestValue;
import static xdean.jfx.ex.util.bean.BeanUtil.yep;

import java.util.function.Function;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.Property;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;

import org.controlsfx.control.StatusBar;
import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.Validator;
import org.fxmisc.richtext.CodeArea;

import xdean.css.editor.config.Options;
import xdean.jex.util.calc.MathUtil;
import xdean.jex.util.string.StringUtil;
import xdean.jex.util.task.TaskUtil;

public class StatusBarManager {

  private StatusBar bar;
  private ObservableValue<CodeArea> area;
  private Property<Boolean> overrideProperty;

  private Label length = new Label(),
      lines = new Label(),
      caretLine = new Label(),
      caretColumn = new Label(),
      select = new Label(),
      charset = new Label(),
      inputType = new Label();

  public StatusBarManager(StatusBar bar, ObservableValue<CodeArea> codeArea, Property<Boolean> overrideProperty) {
    this.bar = bar;
    this.area = codeArea;
    this.overrideProperty = overrideProperty;
    initBar();
    initEvent();
  }

  private void initBar() {
    length.textProperty().bind(map(getCodeAreaValue(c -> c.textProperty()), t -> t.length()));
    lines.textProperty().bind(map(getCodeAreaValue(c -> c.textProperty()), t -> StringUtil.countLine(t)));
    caretLine.textProperty().bind(
        map(getCodeAreaValue(c -> c.caretPositionProperty()), t -> StringUtil.countLine(area.getValue().getText().substring(0, t))));
    caretColumn.textProperty().bind(map(getCodeAreaValue(c -> c.caretColumnProperty()), t -> t));
    select.textProperty().bind(mapString(getCodeAreaValue(c -> c.selectedTextProperty()), t -> t.length() + " | " + StringUtil.countLine(t)));
    charset.textProperty().bind(mapString(Options.charset.property(), t -> t.toString()));
    inputType.textProperty().bind(Bindings.when(yep(overrideProperty)).then("Override").otherwise("Insert"));

    bar.getRightItems().addAll(
        margin(new Text("lines"), 0, 5), minWidth(lines, 60),
        margin(new Text("length"), 0, 5), minWidth(length, 70),
        new Separator(Orientation.VERTICAL),
        margin(new Text("Col"), 0, 5), minWidth(caretColumn, 60),
        margin(new Text("Line"), 0, 5), minWidth(caretLine, 60),
        new Separator(Orientation.VERTICAL),
        margin(new Text("Sel"), 0, 5), minWidth(select, 90),
        new Separator(Orientation.VERTICAL),
        minWidth(charset, 60),
        new Separator(Orientation.VERTICAL),
        minWidth(inputType, 60),
        new Separator(Orientation.VERTICAL)
        );
  }

  private void initEvent() {
    inputType.setOnMouseClicked(hanlder);
    caretLine.setOnMouseClicked(hanlder);
  }

  private EventHandler<MouseEvent> hanlder = e -> {
    if (e.getClickCount() == 2) {
      if (e.getSource() == inputType) {
        overrideProperty.setValue(!overrideProperty.getValue());
      }
      if (e.getSource() == caretLine) {
        showLineJumpDialog();
      }
    }
  };

  private void showLineJumpDialog() {
    TextInputDialog dialog = new TextInputDialog();
    dialog.setTitle("Goto Line");
    dialog.getDialogPane().setContentText("Input Line Number: ");
    dialog.initOwner(area.getValue().getScene().getWindow());
    TextField tf = dialog.getEditor();

    int lines = StringUtil.countLine(area.getValue().getText());
    ValidationSupport vs = new ValidationSupport();
    vs.registerValidator(tf, Validator.<String> createPredicateValidator(
        s -> TaskUtil.uncatch(() -> MathUtil.inRange(Integer.valueOf(s), 1, lines)) == Boolean.TRUE,
        String.format("Line number must be in [%d,%d]", 1, lines)));

    dialog.showAndWait().ifPresent(s -> {
      if (vs.isInvalid() == false) {
        area.getValue().moveTo(Integer.valueOf(s) - 1, 0);
      }
    });
  }

  private <T> ObservableValue<T> getCodeAreaValue(Function<CodeArea, ObservableValue<T>> func) {
    return nestValue(area, c -> func.apply(c));
  }

  private static <T> StringBinding mapString(ObservableValue<T> v, Function<T, String> func) {
    return new StringBinding() {
      {
        bind(v);
      }

      @Override
      protected String computeValue() {
        if (v.getValue() == null) {
          return "";
        } else {
          return func.apply(v.getValue());
        }
      }
    };
  }

  private static <T> StringBinding map(ObservableValue<T> v, Function<T, Integer> func) {
    return mapString(v, t -> Integer.toString(func.apply(t)));
  }
}
