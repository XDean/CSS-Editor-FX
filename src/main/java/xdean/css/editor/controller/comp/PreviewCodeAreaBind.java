package xdean.css.editor.controller.comp;

import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.PopupAlignment;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.PopupControl;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.SVGPath;
import javafx.stage.PopupWindow;
import xdean.jfx.ex.support.DragSupport;
import xdean.jfx.ex.util.LayoutUtil;

public final class PreviewCodeAreaBind {

  private double width = 80, height = 50, line = 2;

  CodeArea codeArea;
  PopupControl popup;

  AnchorPane contentPane;
  Canvas canvas;
  Region region;
  SVGPath svgPath;

  public PreviewCodeAreaBind(CodeArea area) {
    this.codeArea = area;
    this.popup = new PopupControl();
    this.contentPane = new AnchorPane();
    this.canvas = new Canvas(width, height);
    this.region = new Region();
    this.svgPath = new SVGPath();

    contentPane.setBorder(LayoutUtil.getSimpleBorder(Color.BLACK, line));
    contentPane.setPrefWidth(width + 2 * line);
    contentPane.setPrefHeight(height + 2 * line);

    LayoutUtil.setAnchorZero(canvas);
    LayoutUtil.setAnchorZero(svgPath);
    LayoutUtil.setAnchorZero(region);

    region.setPrefSize(width, height);
    region.setBackground(new Background(new BackgroundFill(Color.WHITE, null, null)));

    popup.setConsumeAutoHidingEvents(false);
    popup.setAutoHide(true);
    popup.setAutoFix(true);
    popup.setHideOnEscape(true);

    popup.getScene().setRoot(contentPane);

    area.setPopupWindow(popup);
    area.setPopupAlignment(PopupAlignment.CARET_BOTTOM);

    DragSupport.bind(popup);
  }

  public void showPopup() {
    PopupWindow popupWindow = codeArea.getPopupWindow();
    if (popupWindow != popup) {
      popupWindow.hide();
      codeArea.setPopupWindow(popup);
    }
    popup.show(codeArea.getScene().getWindow());
  }

  public void hidePopup() {
    popup.hide();
  }

  public void showPaint(Paint paint) {
    GraphicsContext graphics = canvas.getGraphicsContext2D();
    graphics.clearRect(0, 0, width, height);

    // graphics.setStroke(Color.BLACK);
    // graphics.setLineWidth(line);
    // graphics.strokeRect(line / 2, line / 2, width + line, height + line);

    graphics.setFill(paint);
    graphics.fillRect(0, 0, width, height);

    contentPane.getChildren().setAll(canvas);
    showPopup();
  }

  public void showSVG(String svg) {
    svgPath.setContent(svg);
    double scaleRate = Math.min(
        (width) / svgPath.getBoundsInLocal().getWidth(),
        (height) / svgPath.getBoundsInLocal().getHeight());
    svgPath.setScaleX(scaleRate);
    svgPath.setScaleY(scaleRate);
    svgPath.setTranslateX(scaleRate * (width - svgPath.getBoundsInLocal().getWidth()) / 2 / scaleRate);
    svgPath.setTranslateY(scaleRate * (height - svgPath.getBoundsInLocal().getHeight()) / 2 / scaleRate);
    contentPane.getChildren().setAll(svgPath);
    showPopup();
  }

  public void showBorder(Border border) {
    region.setBorder(border);
    contentPane.getChildren().setAll(region);
    showPopup();
  }
}
