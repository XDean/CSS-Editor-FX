package xdean.css.editor.service;

import javax.inject.Inject;
import javax.inject.Named;

import org.controlsfx.control.Notifications;
import org.controlsfx.dialog.ExceptionDialog;
import org.springframework.stereotype.Service;

import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.util.Duration;
import xdean.jfx.spring.annotation.FxReady;
import xdean.jfx.spring.context.FxContext;
import xdean.jfxex.extra.FluentDialog;

@Service
@FxReady
public class DialogService {
  private @Inject @Named(FxContext.FX_PRIMARY_STAGE) Stage owner;

  public FluentDialog<ButtonType> infoDialog() {
    return FluentDialog.create(new Alert(AlertType.INFORMATION))
        .owner(owner)
        .button(ButtonType.OK);
  }

  public FluentDialog<ButtonType> warnDialog(Throwable e) {
    return FluentDialog.create(new Alert(AlertType.WARNING))
        .owner(owner)
        .button(ButtonType.OK);
  }

  public FluentDialog<ButtonType> errorDialog(Throwable e) {
    return FluentDialog.create(new ExceptionDialog(e))
        .owner(owner);
  }

  public FluentDialog<ButtonType> confirmDialog() {
    return FluentDialog.create(new Alert(AlertType.CONFIRMATION))
        .owner(owner)
        .button(ButtonType.OK, ButtonType.CANCEL);
  }

  public FluentDialog<ButtonType> confirmCancelDialog() {
    return FluentDialog.create(new Alert(AlertType.CONFIRMATION))
        .owner(owner)
        .button(ButtonType.YES, ButtonType.NO, ButtonType.CANCEL);
  }

  public Notifications infoNotification() {
    return createNotification()
        .graphic(new ImageView(Notifications.class.getResource("/org/controlsfx/dialog/dialog-information.png")
            .toExternalForm())); // $NON-NLS-1$
  }

  public Notifications warningNotification() {
    return createNotification()
        .graphic(new ImageView(Notifications.class.getResource("/org/controlsfx/dialog/dialog-warning.png")
            .toExternalForm())); // $NON-NLS-1$
  }

  public Notifications errorNotification(Throwable e) {
    return createNotification()
        .graphic(new ImageView(Notifications.class.getResource("/org/controlsfx/dialog/dialog-error.png")
            .toExternalForm())) // $NON-NLS-1$
        .onAction(event -> errorDialog(e).dialog(d -> d.getDialogPane().setExpanded(false)).showAndWait());
  }

  private Notifications createNotification() {
    return Notifications.create()
        .owner(owner)
        .hideAfter(Duration.seconds(5))
        .position(Pos.BOTTOM_RIGHT);
  }
}
