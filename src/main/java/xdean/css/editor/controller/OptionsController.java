package xdean.css.editor.controller;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
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
import javafx.stage.Window;
import javafx.util.Pair;
import lombok.extern.slf4j.Slf4j;
import xdean.css.editor.config.Options;
import xdean.css.editor.config.option.BooleanOption;
import xdean.css.editor.config.option.ConstraintOption;
import xdean.css.editor.config.option.IntegerOption;
import xdean.css.editor.config.option.Option;
import xdean.css.editor.config.option.OptionGroup;
import xdean.css.editor.config.option.ValueOption;
import xdean.css.editor.util.Util;
import xdean.jex.util.cache.CacheUtil;
import xdean.jex.util.task.TaskUtil;

@Slf4j
public class OptionsController implements Initializable {

  static void show(Window window) {
    try {
      Pair<OptionsController, DialogPane> pair = Util.renderFxml(OptionsController.class);
      Dialog<Void> dialog = new Dialog<>();
      dialog.initOwner(window);
      dialog.setDialogPane(pair.getValue());
      dialog.show();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @FXML
  DialogPane dialogPane;

  @FXML
  VBox generalPane;

  @FXML
  TableView<Option<KeyCombination>> keyTable;

  @FXML
  TableColumn<Option<KeyCombination>, String> commandColumn;

  @FXML
  TableColumn<Option<KeyCombination>, KeyCombination> bindingColumn;

  private int nowTab = 0;
  private List<Runnable> onSubmit = new ArrayList<>();

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    initGeneral();
    initKey();

    dialogPane.lookupButton(ButtonType.FINISH).addEventHandler(ActionEvent.ACTION, e -> {
      onSubmit.forEach(Runnable::run);
      e.consume();
    });
  }

  private void initGeneral() {
    nowTab = -1;
    Options.GENERAL.getChildren().forEach(e -> e.exec(this::handle, this::handle));
  }

  private void initKey() {
    commandColumn.setCellValueFactory(cdf -> new SimpleStringProperty(cdf.getValue().getDescribe()));
    bindingColumn.setCellValueFactory(cdf -> CacheUtil.cache(OptionsController.this,
        cdf.getValue(), () -> new SimpleObjectProperty<>(cdf.getValue().get())));
//    bindingColumn.setCellValueFactory(cdf -> new SimpleObjectProperty<>(cdf.getValue().get()));

    bindingColumn.setEditable(true);
    bindingColumn.setCellFactory(column -> new KeyEditField());

    keyTable.getItems().setAll(Options.KEY.getChildren(KeyCombination.class));
    onSubmit.add(() -> keyTable.getItems().forEach(key -> key.set(bindingColumn.getCellData(key))));
  }

  private <T> void handle(OptionGroup og) {
    nowTab++;
    Label label = new Label(og.getDescribe());
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
      log.error("Can't handle this option: " + o);
    }
    nowTab--;
  }

  private <T> boolean special(Option<T> o) {
    if (o == Options.fontFamily) {
      ComboBox<String> box = add(Options.fontFamily);
      box.setCellFactory(p -> new FontListCell());
      return true;
    }

    return false;
  }

  private <T> ComboBox<T> add(ValueOption<T> vo) {
    ComboBox<T> cb = new ComboBox<>();
    cb.getItems().addAll(vo.getValues());
    cb.getSelectionModel().select(vo.get());
    onSubmit.add(() -> vo.set(cb.getSelectionModel().getSelectedItem()));
    add(wrapWithText(cb, vo.getDescribe()));
    return cb;
  }

  private Spinner<Integer> add(IntegerOption ro) {
    Spinner<Integer> s = new Spinner<>(new SpinnerValueFactory.IntegerSpinnerValueFactory(ro.getMin(), ro.getMax(), ro.get()));
    onSubmit.add(() -> ro.set(s.getValue()));
    add(wrapWithText(s, ro.getDescribe()));
    return s;
  }

  private CheckBox add(BooleanOption ob) {
    CheckBox cb = new CheckBox(ob.getDescribe());
    cb.setSelected(ob.get());
    onSubmit.add(() -> ob.set(cb.isSelected()));
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
