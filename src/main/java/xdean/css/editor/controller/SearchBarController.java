package xdean.css.editor.controller;

import static xdean.jfxex.bean.ListenerUtil.on;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import org.controlsfx.control.textfield.TextFields;
import org.fxmisc.richtext.CodeArea;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import xdean.css.editor.context.setting.EditActions;
import xdean.css.editor.context.setting.OtherSettings;
import xdean.css.editor.control.CssEditor;
import xdean.css.editor.feature.CssEditorFeature;
import xdean.css.editor.service.ContextService;
import xdean.jex.extra.function.Func3;
import xdean.jex.util.string.StringUtil;
import xdean.jfx.spring.FxInitializable;
import xdean.jfx.spring.annotation.FxController;
import xdean.jfxex.bean.property.BooleanPropertyEX;

@FxController(fxml = "/fxml/SearchBar.fxml")
public class SearchBarController implements FxInitializable, CssEditorFeature {

  private @FXML HBox root;
  private @FXML HBox textContainer;
  private @FXML Button findButton;
  private @FXML CheckBox caseSensitive;
  private @FXML CheckBox regex;
  private @FXML CheckBox wrapSearch;
  private @Inject OtherSettings otherSettings;
  private @Inject ContextService contextService;
  private @Inject EditActions editActions;

  private TextField findField;
  private final BooleanPropertyEX visible = new BooleanPropertyEX(this, "visible", false);

  @Override
  public void initAfterFxSpringReady() {
    findField = TextFields.createClearableTextField();
    textContainer.getChildren().add(findField);

    regex.selectedProperty().bindBidirectional(otherSettings.regexSearch().valueProperty());
    caseSensitive.selectedProperty().bindBidirectional(otherSettings.caseSensitive().valueProperty());
    wrapSearch.selectedProperty().bindBidirectional(otherSettings.wrapSearch().valueProperty());
    visible.and(contextService.activeEditorProperty().isNotNull());

    root.visibleProperty().addListener(on(true, findField::requestFocus)
        .on(false, () -> contextService.getActiveEditorSafe().ifPresent(Node::requestFocus)));
    root.visibleProperty().bind(visible);
    root.managedProperty().bind(root.visibleProperty());
    findField.setOnAction(e -> find());
  }

  @Override
  public void bind(CssEditor editor) {
    editor.addEventHandler(editActions.find().getEventType(), e -> toggle());
  }

  @FXML
  public void find() {
    if (findFrom(contextService.getActiveEditor().getCaretPosition()) == false && wrapSearch.isSelected()) {
      findFrom(0);
    }
  }

  private boolean findFrom(int offset) {
    CodeArea area = contextService.getActiveEditor();
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
    visible.set(!visible.get());
  }
}
