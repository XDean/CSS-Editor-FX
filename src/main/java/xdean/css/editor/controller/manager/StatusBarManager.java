package xdean.css.editor.controller.manager;

import static xdean.jex.util.lang.ExceptionUtil.uncatch;
import static xdean.jfxex.bean.BeanUtil.mapToString;
import static xdean.jfxex.bean.BeanUtil.nestValue;
import static xdean.jfxex.util.LayoutUtil.margin;
import static xdean.jfxex.util.LayoutUtil.minWidth;

import java.util.function.Function;

import org.controlsfx.control.StatusBar;
import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.Validator;
import org.fxmisc.richtext.CodeArea;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Orientation;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import xdean.css.editor.config.Options;
import xdean.jex.util.calc.MathUtil;
import xdean.jfx.spring.annotation.FxComponent;
import xdean.jfxex.bean.property.BooleanPropertyEX;
import xdean.jfxex.bean.property.ObjectPropertyEX;

@FxComponent
public class StatusBarManager {

  public final ObjectPropertyEX<CodeArea> area = new ObjectPropertyEX<>(this, "area");
  public final BooleanPropertyEX override = new BooleanPropertyEX(this, "override");

  public void bind(StatusBar bar) {
    Label lines = new Label(),
        length = new Label(),
        caretLine = new Label(),
        caretColumn = new Label(),
        select = new Label(),
        charset = new Label(),
        inputType = new Label();

    lines.textProperty().bind(map(nestValue(area, c -> c.textProperty()), t -> countLine(t)));
    length.textProperty().bind(map(nestValue(area, c -> c.textProperty()), t -> t.length()));
    caretColumn.textProperty().bind(map(nestValue(area, c -> c.caretColumnProperty()), t -> t));
    caretLine.textProperty().bind(map(nestValue(area, c -> c.caretPositionProperty()),
        t -> countLine(area.getValue().getText().substring(0, t))));
    select.textProperty()
        .bind(map(nestValue(area, c -> c.selectedTextProperty()), t -> t.length() + " | " + countLine(t)));
    charset.textProperty().bind(map(Options.charset.property(), t -> t.toString()));
    inputType.textProperty().bind(Bindings.when(override.normalize()).then("Override").otherwise("Insert"));

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
        new Separator(Orientation.VERTICAL));

    inputType.setOnMouseClicked(this::toggleType);
    lines.setOnMouseClicked(this::jumpLine);
    caretLine.setOnMouseClicked(this::jumpLine);
  }

  public void toggleType(MouseEvent e) {
    override.setValue(!override.getValue());
  }

  public void jumpLine(MouseEvent e) {
    if (e.getClickCount() == 2) {
      showLineJumpDialog();
    }
  }

  private void showLineJumpDialog() {
    TextInputDialog dialog = new TextInputDialog();
    dialog.setTitle("Goto Line");
    dialog.getDialogPane().setContentText("Input Line Number: ");
    dialog.initOwner(area.getValue().getScene().getWindow());
    TextField tf = dialog.getEditor();

    int lines = countLine(area.getValue().getText());
    ValidationSupport vs = new ValidationSupport();
    vs.registerValidator(tf, Validator.<String> createPredicateValidator(
        s -> uncatch(() -> MathUtil.inRange(Integer.valueOf(s), 1, lines)) == Boolean.TRUE,
        String.format("Line number must be in [%d,%d]", 1, lines)));

    dialog.showAndWait().ifPresent(s -> {
      if (vs.isInvalid() == false) {
        area.getValue().moveTo(Integer.valueOf(s) - 1, 0);
      }
    });
  }

  private static int countLine(String str) {
    return (str + " ").split("\\R").length;
  }

  private static <T> StringBinding map(ObservableValue<T> v, Function<T, Object> func) {
    return mapToString(v, t -> t == null ? "" : func.apply(t).toString());
  }
}
