package xdean.css.editor.feature;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import io.reactivex.rxjavafx.observables.JavaFxObservable;
import io.reactivex.rxjavafx.schedulers.JavaFxScheduler;
import xdean.css.editor.control.CssCodeArea;
import xdean.css.editor.feature.highlight.CssHighLight;

@Service
public class HighLightFeature implements CssCodeAreaFeature {

  @Inject
  CssHighLight cssHighLight;

  @Override
  public void bind(CssCodeArea cssCodeArea) {
    JavaFxObservable.valuesOf(cssCodeArea.textProperty())
        .debounce(300, TimeUnit.MILLISECONDS)
        .observeOn(JavaFxScheduler.platform())
        .subscribe(e -> cssCodeArea.setStyleSpans(0, cssHighLight.compute(e)));
  }
}
