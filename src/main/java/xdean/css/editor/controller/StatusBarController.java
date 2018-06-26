package xdean.css.editor.controller;

import static xdean.jex.util.lang.ExceptionUtil.uncatch;
import static xdean.jfxex.bean.BeanUtil.mapToString;
import static xdean.jfxex.bean.BeanUtil.nestBooleanValue;
import static xdean.jfxex.bean.BeanUtil.nestValue;

import java.util.function.Function;

import javax.inject.Inject;

import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.Validator;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.MouseEvent;
import xdean.css.editor.context.setting.PreferenceSettings;
import xdean.css.editor.control.CssEditor;
import xdean.css.editor.service.ContextService;
import xdean.jex.util.calc.MathUtil;
import xdean.jfx.spring.FxInitializable;
import xdean.jfx.spring.annotation.FxController;

@FxController(fxml = "/fxml/StatusBar.fxml")
public class StatusBarController implements FxInitializable {

  private @FXML Label length;
  private @FXML Label lines;
  private @FXML Label caretLine;
  private @FXML Label caretCol;
  private @FXML Label select;
  private @FXML Label charset;
  private @FXML Label inputType;
  private @Inject PreferenceSettings options;
  private @Inject ContextService contextService;

  @Override
  public void initAfterFxSpringReady() {
    ObjectProperty<CssEditor> editor = contextService.activeEditorProperty();
    lines.textProperty().bind(map(nestValue(editor, c -> c.textProperty()), t -> countLine(t)));
    length.textProperty().bind(map(nestValue(editor, c -> c.textProperty()), t -> t.length()));
    caretCol.textProperty().bind(map(nestValue(editor, c -> c.caretColumnProperty()), t -> t));
    caretLine.textProperty().bind(map(nestValue(editor, c -> c.caretPositionProperty()),
        t -> countLine(editor.getValue().getText().substring(0, t))));
    select.textProperty()
        .bind(map(nestValue(editor, c -> c.selectedTextProperty()), t -> t.length() + " | " + countLine(t)));
    charset.textProperty().bind(map(options.charset().valueProperty(), t -> t.toString()));
    inputType.textProperty()
        .bind(Bindings.when(nestBooleanValue(editor, e -> e.overrideProperty())).then("Override").otherwise("Insert"));
  }

  @FXML
  public void toggleType(MouseEvent e) {
    BooleanProperty override = contextService.getActiveEditor().overrideProperty();
    override.set(!override.get());
  }

  @FXML
  public void jumpLine(MouseEvent e) {
    if (e.getClickCount() == 2) {
      showLineJumpDialog();
    }
  }

  private void showLineJumpDialog() {
    CssEditor editor = contextService.activeEditorProperty().getValue();
    TextInputDialog dialog = new TextInputDialog();
    dialog.setTitle("Goto Line");
    dialog.getDialogPane().setContentText("Input Line Number: ");
    dialog.initOwner(editor.getScene().getWindow());
    TextField tf = dialog.getEditor();

    int lines = countLine(editor.getText());
    ValidationSupport vs = new ValidationSupport();
    vs.registerValidator(tf, Validator.<String> createPredicateValidator(
        s -> uncatch(() -> MathUtil.inRange(Integer.valueOf(s), 1, lines)) == Boolean.TRUE,
        String.format("Line number must be in [%d,%d]", 1, lines)));

    dialog.showAndWait().ifPresent(s -> {
      if (vs.isInvalid() == false) {
        editor.moveTo(Integer.valueOf(s) - 1, 0);
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
