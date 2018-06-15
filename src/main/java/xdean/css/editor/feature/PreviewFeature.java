package xdean.css.editor.feature;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.fxmisc.richtext.PopupAlignment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
import javafx.scene.shape.SVGPath;
import javafx.stage.PopupWindow;
import xdean.css.editor.control.CssCodeArea;
import xdean.css.editor.feature.preview.CssElementPreviewer;
import xdean.jex.extra.collection.Pair;
import xdean.jfxex.support.DragSupport;
import xdean.jfxex.util.LayoutUtil;

@Service
@SuppressWarnings("rawtypes")
public class PreviewFeature implements CssCodeAreaFeature {

  @Autowired(required = false)
  List<CssElementPreviewer> previewers = Collections.emptyList();

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

  private class InnerController {

    private double width = 80, height = 50, line = 2;

    CssCodeArea codeArea;
    PopupControl popup;

    AnchorPane contentPane;
    Canvas canvas;
    Region region;
    SVGPath svgPath;

    @SuppressWarnings("unchecked")
    InnerController(CssCodeArea area) {
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

      JavaFxObservable.valuesOf(codeArea.selectedTextProperty())
          .debounce(300, TimeUnit.MILLISECONDS)
          .map(t -> extractValue(t))
          .map(String::trim)
          .filter(s -> !s.isEmpty())
          .observeOn(Schedulers.computation())
          .<Pair<CssElementPreviewer, Object>> switchMapMaybe(text -> Observable.fromIterable(previewers)
              .flatMapMaybe(p -> p.parse(codeArea.context, text)
                  .subscribeOn(Schedulers.computation())
                  .map(o -> Pair.of(p, o)))
              .firstElement())
          .observeOn(JavaFxScheduler.platform())
          .doOnNext(p -> show(p.getLeft(), p.getRight()))
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

    <T> void show(CssElementPreviewer<T> previewer, T value) {
      GraphicsContext graphics = canvas.getGraphicsContext2D();
      graphics.clearRect(0, 0, width, height);

      previewer.preview(graphics, value, width, height);

      contentPane.getChildren().setAll(canvas);
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
