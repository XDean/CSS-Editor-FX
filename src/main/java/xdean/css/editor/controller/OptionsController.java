package xdean.css.editor.controller;

import static xdean.jex.util.cache.CacheUtil.cache;
import static xdean.jfxex.bean.ListenerUtil.on;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import com.sun.javafx.binding.ContentBinding;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyCombination.ModifierValue;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import xdean.css.editor.context.setting.HelpActions;
import xdean.css.editor.context.setting.PreferenceSettings;
import xdean.css.editor.context.setting.model.option.BooleanOption;
import xdean.css.editor.context.setting.model.option.IntegerOption;
import xdean.css.editor.context.setting.model.option.Option;
import xdean.css.editor.context.setting.model.option.OptionGroup;
import xdean.css.editor.context.setting.model.option.ValueOption;
import xdean.css.editor.feature.CssAppFeature;
import xdean.css.editor.service.MessageService;
import xdean.jex.log.Logable;
import xdean.jex.util.string.StringUtil;
import xdean.jex.util.task.TaskUtil;
import xdean.jfx.spring.FxInitializable;
import xdean.jfx.spring.annotation.FxController;

@FxController(fxml = "/fxml/Options.fxml")
public class OptionsController implements FxInitializable, Logable, CssAppFeature {
  private @FXML DialogPane root;
  private @FXML VBox generalPane;
  private @FXML TableView<Option<KeyCombination>> keyTable;
  private @FXML TableColumn<Option<KeyCombination>, String> commandColumn;
  private @FXML TableColumn<Option<KeyCombination>, KeyCombination> bindingColumn;

  private @Inject PreferenceSettings preference;
  private @Inject List<Option<KeyCombination>> keyOptions;
  private @Inject MessageService messageService;
  private @Inject HelpActions help;

  private int nowTab = 0;
  private final List<Runnable> onSubmit = new ArrayList<>();

  @Override
  public void bind(Stage stage) {
    stage.addEventHandler(help.settings().getEventType(), e -> open(stage));
  }

  @Override
  public void initAfterFxSpringReady() {
    initGeneral();
    initKey();

    root.lookupButton(ButtonType.FINISH).addEventHandler(ActionEvent.ACTION, e -> onSubmit.forEach(Runnable::run));
  }

  public void open(Stage stage) {
    Dialog<Void> dialog = new Dialog<>();
    dialog.initOwner(stage);
    dialog.setTitle("Option");
    dialog.setDialogPane(root);
    dialog.show();
  }

  private void initGeneral() {
    nowTab = -1;
    preference.general().getChildren().forEach(e -> e.exec(this::handle, this::handle));
  }

  private void initKey() {
    commandColumn.setCellValueFactory(cdf -> new SimpleStringProperty(messageService.getMessage(cdf.getValue().getKey())));
    bindingColumn.setCellValueFactory(cdf -> cache(this,
        cdf.getValue(), () -> new SimpleObjectProperty<>(cdf.getValue().getValue())));

    bindingColumn.setEditable(true);
    bindingColumn.setCellFactory(column -> new KeyEditField());

    keyTable.getItems().setAll(keyOptions);
    onSubmit.add(() -> keyTable.getItems().forEach(key -> key.setValue(bindingColumn.getCellData(key))));
    keyOptions.forEach(option -> option.valueProperty()
        .addListener((ob, o, n) -> ((Property<KeyCombination>) bindingColumn.getCellObservableValue(option)).setValue(n)));
  }

  private <T> void handle(OptionGroup og) {
    nowTab++;
    Label label = new Label(messageService.getMessage(og.getKey()));
    Font font = label.getFont();
    label.setFont(Font.font(font.getSize() + 10 - nowTab * 3));
    add(label);
    og.getChildren().forEach(e -> e.exec(this::handle, this::handle));
    nowTab--;
  }

  private <T> void handle(Option<T> o) {
    nowTab++;
    if (special(o)) {
    } else if (o instanceof BooleanOption) {
      add((BooleanOption) o);
    } else if (o instanceof IntegerOption) {
      add((IntegerOption) o);
    } else if (o instanceof ValueOption) {
      add((ValueOption<T>) o);
    } else {
      error("Can't handle this option: " + o);
    }
    nowTab--;
  }

  private <T> boolean special(Option<T> o) {
    if (o == preference.fontFamily()) {
      ComboBox<String> box = add(preference.fontFamily());
      box.setCellFactory(p -> new FontListCell());
      return true;
    }
    return false;
  }

  private <T> ComboBox<T> add(ValueOption<T> vo) {
    ComboBox<T> cb = new ComboBox<>();
    ContentBinding.bind(cb.getItems(), vo.values);
    cb.getSelectionModel().select(vo.getValue());
    onSubmit.add(() -> vo.setValue(cb.getSelectionModel().getSelectedItem()));
    vo.valueProperty().addListener((ob, o, n) -> cb.getSelectionModel().select(n));
    add(wrapWithText(cb, messageService.getMessage(vo.getKey())));
    return cb;
  }

  private Spinner<Integer> add(IntegerOption ro) {
    Spinner<Integer> s = new Spinner<>(
        new SpinnerValueFactory.IntegerSpinnerValueFactory(ro.getMin(), ro.getMax(), ro.getValue()));
    onSubmit.add(() -> ro.setValue(s.getValue()));
    ro.valueProperty().addListener((ob, o, n) -> s.getValueFactory().setValue(n));
    add(wrapWithText(s, messageService.getMessage(ro.getKey())));
    return s;
  }

  private CheckBox add(BooleanOption bo) {
    CheckBox cb = new CheckBox(messageService.getMessage(bo.getKey()));
    cb.setSelected(bo.getValue());
    onSubmit.add(() -> bo.setValue(cb.isSelected()));
    bo.valueProperty().addListener((ob, o, n) -> cb.setSelected(n));
    add(cb);
    return cb;
  }

  private Node wrapWithText(Node node, String text) {
    Label label = new Label(text);
    label.setMinWidth(Region.USE_PREF_SIZE);
    HBox hb = new HBox(label, node);
    HBox.setHgrow(node, Priority.ALWAYS);
    hb.setSpacing(5);
    return hb;
  }

  private void add(Node node) {
    VBox.setMargin(node, new Insets(0, 0, 0, 15 * nowTab));
    generalPane.getChildren().add(node);
  }

  private static class FontListCell extends ListCell<String> {
    @Override
    protected void updateItem(String fontFamily, boolean empty) {
      super.updateItem(fontFamily, empty);
      if (!empty) {
        setText(fontFamily);
        setFont(Font.font(fontFamily));
      } else {
        setText(null);
      }
    }
  }

  private static class KeyEditField extends TableCell<Option<KeyCombination>, KeyCombination> implements Logable {

    private TextField field;

    @Override
    public void startEdit() {
      super.startEdit();
      if (field == null) {
        field = createField();
      }
      field.setText(getString());
      setText(null);
      setGraphic(field);
      field.requestFocus();
    }

    @Override
    public void cancelEdit() {
      super.cancelEdit();
      setText(getString());
      setGraphic(null);
    }

    @Override
    protected void updateItem(KeyCombination item, boolean empty) {
      super.updateItem(item, empty);
      if (empty) {
        setText(null);
        setGraphic(null);
      } else {
        if (isEditing()) {
          setText(null);
          setGraphic(field);
        } else {
          setText(getString());
          setGraphic(null);
        }
      }
    }

    private TextField createField() {
      TextField field = new TextField(getString());
      field.setEditable(false);
      field.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
        if (e.getCode() == KeyCode.ENTER) {
          commit(field.getText());
        } else if (e.getCode() == KeyCode.ESCAPE) {
          cancelEdit();
        } else if (e.getCode() == KeyCode.BACK_SPACE) {
          field.setText(null);
        } else {
          field.setText(convert(e).toString());
        }
        e.consume();
      });
      field.focusedProperty().addListener(on(false, () -> commit(field.getText())));
      return field;
    }

    public void commit(String text) {
      try {
        commitEdit(StringUtil.isEmpty(text) ? KeyCombination.NO_MATCH : KeyCombination.valueOf(text));
      } catch (Exception ee) {
        debug(ee);
        cancelEdit();
      }
    }

    private String convert(KeyEvent e) {
      return TaskUtil.firstSuccess(
          () -> new KeyCodeCombination(e.getCode(),
              e.isShiftDown() ? ModifierValue.DOWN : ModifierValue.UP,
              e.isControlDown() || !(e.isAltDown() || e.isShiftDown() || e.isMetaDown()) ? ModifierValue.DOWN : ModifierValue.UP,
              e.isAltDown() ? ModifierValue.DOWN : ModifierValue.UP,
              e.isMetaDown() ? ModifierValue.DOWN : ModifierValue.UP,
              ModifierValue.UP).toString(),
          () -> {
            KeyCodeCombination key = new KeyCodeCombination(KeyCode.A,
                e.isShiftDown() ? ModifierValue.DOWN : ModifierValue.UP,
                e.isControlDown() ? ModifierValue.DOWN : ModifierValue.UP,
                e.isAltDown() ? ModifierValue.DOWN : ModifierValue.UP,
                e.isMetaDown() ? ModifierValue.DOWN : ModifierValue.UP,
                ModifierValue.UP);
            String name = key.getName();
            return name.substring(0, name.length() - key.getCode().getName().length());
          });
    }

    private String getString() {
      return getItem() == null ? "" : getItem().toString();
    }
  }
}
