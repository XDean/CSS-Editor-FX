package xdean.css.editor.service;

import javax.inject.Inject;

import org.controlsfx.dialog.ExceptionDialog;
import org.springframework.stereotype.Service;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import xdean.jfxex.extra.FluentDialog;

@Service
public class DialogService {

  @Inject
  SkinService skinService;

  public FluentDialog<ButtonType> errorDialog(Throwable e) {
    return FluentDialog.create(new ExceptionDialog(e))
        .dialog(d -> skinService.bind(d));
  }

  public FluentDialog<ButtonType> infoDialog() {
    return FluentDialog.create(new Alert(AlertType.INFORMATION))
        .button(ButtonType.OK)
        .dialog(d -> skinService.bind(d));
  }

  public FluentDialog<ButtonType> confirmDialog() {
    return FluentDialog.create(new Alert(AlertType.CONFIRMATION))
        .button(ButtonType.OK, ButtonType.CANCEL)
        .dialog(d -> skinService.bind(d));
  }

  public FluentDialog<ButtonType> confirmCancelDialog() {
    return FluentDialog.create(new Alert(AlertType.CONFIRMATION))
        .button(ButtonType.YES, ButtonType.NO, ButtonType.CANCEL)
        .dialog(d -> skinService.bind(d));
  }
}
