package xdean.css.editor.controller;

import static xdean.jex.util.lang.ExceptionUtil.uncatch;
import static xdean.jfxex.bean.BeanUtil.mapToString;
import static xdean.jfxex.bean.BeanUtil.nestValue;

import java.util.function.Function;

import javax.inject.Inject;

import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.Validator;
import org.fxmisc.richtext.CodeArea;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.MouseEvent;
import xdean.css.editor.context.setting.PreferenceSettings;
import xdean.jex.util.calc.MathUtil;
import xdean.jfx.spring.FxInitializable;
import xdean.jfx.spring.annotation.FxController;
import xdean.jfxex.bean.property.BooleanPropertyEX;
import xdean.jfxex.bean.property.ObjectPropertyEX;

@FxController(fxml = "/fxml/StatusBar.fxml")
public class StatusBarController implements FxInitializable {

  public final ObjectPropertyEX<CodeArea> area = new ObjectPropertyEX<>(this, "area");
  public final BooleanPropertyEX override = new BooleanPropertyEX(this, "override");

  private @FXML Label length;
  private @FXML Label lines;
  private @FXML Label caretLine;
  private @FXML Label caretCol;
  private @FXML Label select;
  private @FXML Label charset;
  private @FXML Label inputType;
  private @Inject PreferenceSettings options;

  @Override
  public void initAfterFxSpringReady() {
    lines.textProperty().bind(map(nestValue(area, c -> c.textProperty()), t -> countLine(t)));
    length.textProperty().bind(map(nestValue(area, c -> c.textProperty()), t -> t.length()));
    caretCol.textProperty().bind(map(nestValue(area, c -> c.caretColumnProperty()), t -> t));
    caretLine.textProperty().bind(map(nestValue(area, c -> c.caretPositionProperty()),
        t -> countLine(area.getValue().getText().substring(0, t))));
    select.textProperty()
        .bind(map(nestValue(area, c -> c.selectedTextProperty()), t -> t.length() + " | " + countLine(t)));
    charset.textProperty().bind(map(options.charset().valueProperty(), t -> t.toString()));
    inputType.textProperty().bind(Bindings.when(override.normalize()).then("Override").otherwise("Insert"));
  }

  @FXML
  public void toggleType(MouseEvent e) {
    override.setValue(!override.getValue());
  }

  @FXML
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
