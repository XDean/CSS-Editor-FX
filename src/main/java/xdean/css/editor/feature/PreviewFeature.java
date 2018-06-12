package xdean.css.editor.feature;

import java.util.concurrent.TimeUnit;

import org.fxmisc.richtext.PopupAlignment;
import org.springframework.stereotype.Service;

import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.rxjavafx.observables.JavaFxObservable;
import io.reactivex.rxjavafx.schedulers.JavaFxScheduler;
import io.reactivex.schedulers.Schedulers;
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
import xdean.css.editor.control.CssCodeArea;
import xdean.css.parser.CSSPaintPaser;
import xdean.css.parser.CSSSVGPaser;
import xdean.jex.util.string.StringUtil;
import xdean.jfxex.support.DragSupport;
import xdean.jfxex.util.LayoutUtil;

@Service
public class PreviewFeature implements CssCodeAreaFeature {

  @Override
  public void bind(CssCodeArea cssCodeArea) {
    new InnerController(cssCodeArea);
  }

  private static String extractValue(String text) {
    String str = text.trim();
    int colon = str.indexOf(':');
    int semicolon = str.indexOf(';');
    if (semicolon != -1 && semicolon == str.length() - 1) {
      str = str.substring(0, str.length() - 1);
    }
    if (colon != -1) {
      str = str.substring(colon + 1);
    }
    return str;
  }

  private static class InnerController {

    private double width = 80, height = 50, line = 2;

    CssCodeArea codeArea;
    CSSPaintPaser paintPaser;
    PopupControl popup;

    AnchorPane contentPane;
    Canvas canvas;
    Region region;
    SVGPath svgPath;

    InnerController(CssCodeArea area) {
      this.codeArea = area;
      this.paintPaser = new CSSPaintPaser(codeArea.context);
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

      JavaFxObservable.valuesOf(codeArea.selectedTextProperty())
          .debounce(300, TimeUnit.MILLISECONDS)
          .map(t -> extractValue(t))
          .map(String::trim)
          .observeOn(Schedulers.computation())
          .switchMapMaybe(text -> Observable.merge(
              Observable.fromIterable(paintPaser.getTasks())
                  .observeOn(Schedulers.computation())
                  .concatMapMaybe(f -> Maybe.fromCallable(() -> f.apply(text)).onErrorComplete())
                  .observeOn(JavaFxScheduler.platform())
                  .doOnNext(this::showPaint),
              Observable.just(text)
                  .map(s -> StringUtil.unWrap(s, "\"", "\""))
                  .filter(CSSSVGPaser::verify)
                  .observeOn(JavaFxScheduler.platform())
                  .doOnNext(this::showSVG))
              .observeOn(JavaFxScheduler.platform())
              .firstElement()
              .doOnComplete(this::hidePopup))
          .subscribe();
    }

    void showPopup() {
      PopupWindow popupWindow = codeArea.getPopupWindow();
      if (popupWindow != popup) {
        popupWindow.hide();
        codeArea.setPopupWindow(popup);
      }
      popup.show(codeArea.getScene().getWindow());
    }

    void hidePopup() {
      popup.hide();
    }

    void showPaint(Paint paint) {
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

    void showSVG(String svg) {
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

    @SuppressWarnings("unused")
    void showBorder(Border border) {
      region.setBorder(border);
      contentPane.getChildren().setAll(region);
      showPopup();
    }
  }
}
