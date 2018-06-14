package xdean.css.editor.controller;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

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
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import xdean.css.editor.context.option.Options;
import xdean.css.editor.context.option.model.BooleanOption;
import xdean.css.editor.context.option.model.ConstraintOption;
import xdean.css.editor.context.option.model.IntegerOption;
import xdean.css.editor.context.option.model.Option;
import xdean.css.editor.context.option.model.OptionGroup;
import xdean.css.editor.context.option.model.ValueOption;
import xdean.jex.log.Logable;
import xdean.jex.util.cache.CacheUtil;
import xdean.jex.util.task.TaskUtil;
import xdean.jfx.spring.FxInitializable;
import xdean.jfx.spring.annotation.FxController;

@FxController(fxml = "/fxml/Options.fxml")
public class OptionsController implements FxInitializable, Logable {
  @FXML
  DialogPane root;

  @FXML
  VBox generalPane;

  @FXML
  TableView<Option<KeyCombination>> keyTable;

  @FXML
  TableColumn<Option<KeyCombination>, String> commandColumn;

  @FXML
  TableColumn<Option<KeyCombination>, KeyCombination> bindingColumn;

  @Inject
  Options options;

  private int nowTab = 0;
  private List<Runnable> onSubmit = new ArrayList<>();

  @Override
  public void initAfterFxSpringReady() {
    initGeneral();
    initKey();

    root.lookupButton(ButtonType.FINISH).addEventHandler(ActionEvent.ACTION, e -> {
      onSubmit.forEach(Runnable::run);
      e.consume();
    });
  }

  public void open(Stage stage) {
    Dialog<Void> dialog = new Dialog<>();
    dialog.initOwner(stage);
    dialog.setDialogPane(root);
    dialog.show();
  }

  private void initGeneral() {
    nowTab = -1;
    options.general().getChildren().forEach(e -> e.exec(this::handle, this::handle));
  }

  private void initKey() {
    commandColumn.setCellValueFactory(cdf -> new SimpleStringProperty(cdf.getValue().getKey()));
    bindingColumn.setCellValueFactory(cdf -> CacheUtil.cache(OptionsController.this,
        cdf.getValue(), () -> new SimpleObjectProperty<>(cdf.getValue().getValue())));
    // bindingColumn.setCellValueFactory(cdf -> new
    // SimpleObjectProperty<>(cdf.getValue().get()));

    bindingColumn.setEditable(true);
    bindingColumn.setCellFactory(column -> new KeyEditField());

    keyTable.getItems().setAll(options.key().getChildren(KeyCombination.class));
    onSubmit.add(() -> keyTable.getItems().forEach(key -> key.setValue(bindingColumn.getCellData(key))));
  }

  private <T> void handle(OptionGroup og) {
    nowTab++;
    Label label = new Label(og.getKey());
    Font font = label.getFont();
    label.setFont(Font.font(font.getSize() + 10 - nowTab * 3));
    add(label);
    og.getChildren().forEach(e -> e.exec(this::handle, this::handle));
    nowTab--;
  }

  private <T> void handle(Option<T> o) {
    nowTab++;
    if (special(o)) {
      ;
    } else if (o instanceof BooleanOption) {
      add((BooleanOption) o);
    } else if (o instanceof ConstraintOption) {
      if (o instanceof IntegerOption) {
        add((IntegerOption) o);
      } else if (o instanceof ValueOption) {
        add((ValueOption<T>) o);
      }
    } else {
      error("Can't handle this option: " + o);
    }
    nowTab--;
  }

  private <T> boolean special(Option<T> o) {
    if (o == options.fontFamily()) {
      ComboBox<String> box = add(options.fontFamily());
      box.setCellFactory(p -> new FontListCell());
      return true;
    }

    return false;
  }

  private <T> ComboBox<T> add(ValueOption<T> vo) {
    ComboBox<T> cb = new ComboBox<>();
    cb.getItems().addAll(vo.getValues());
    cb.getSelectionModel().select(vo.getValue());
    onSubmit.add(() -> vo.setValue(cb.getSelectionModel().getSelectedItem()));
    add(wrapWithText(cb, vo.getKey()));
    return cb;
  }

  private Spinner<Integer> add(IntegerOption ro) {
    Spinner<Integer> s = new Spinner<>(
        new SpinnerValueFactory.IntegerSpinnerValueFactory(ro.getMin(), ro.getMax(), ro.getValue()));
    onSubmit.add(() -> ro.setValue(s.getValue()));
    add(wrapWithText(s, ro.getKey()));
    return s;
  }

  private CheckBox add(BooleanOption ob) {
    CheckBox cb = new CheckBox(ob.getKey());
    cb.setSelected(ob.getValue());
    onSubmit.add(() -> ob.setValue(cb.isSelected()));
    add(cb);
    return cb;
  }

  private Node wrapWithText(Node node, String text) {
    HBox hb = new HBox(new Label(text), node);
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

  private static class KeyEditField extends TableCell<Option<KeyCombination>, KeyCombination> {

    TextField field;

    @Override
    public void startEdit() {
      super.startEdit();
      if (field == null) {
        createField();
      }
      field.setText(getString());
      setText(null);
      setGraphic(field);
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

    private void createField() {
      field = new TextField(getString());
      field.setEditable(false);
      field.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
        if (e.getCode() == KeyCode.ENTER) {
          try {
            commitEdit(KeyCombination.valueOf(field.getText()));
          } catch (Exception ee) {
            cancelEdit();
          }
        } else if (e.getCode() == KeyCode.ESCAPE) {
          cancelEdit();
        } else {
          field.setText(convert(e).toString());
        }
        e.consume();
      });
    }

    private String convert(KeyEvent e) {
      return TaskUtil.firstSuccess(
          () -> new KeyCodeCombination(e.getCode(),
              e.isShiftDown() ? ModifierValue.DOWN : ModifierValue.UP,
              e.isControlDown() ? ModifierValue.DOWN : ModifierValue.UP,
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
