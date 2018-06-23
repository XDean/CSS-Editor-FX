package xdean.css.editor.feature;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import io.reactivex.rxjavafx.observables.JavaFxObservable;
import io.reactivex.rxjavafx.schedulers.JavaFxScheduler;
import xdean.css.editor.control.CssEditor;
import xdean.css.editor.feature.highlight.CssHighLight;

@Service
public class HighLightFeature implements CssEditorFeature {

  @Inject
  CssHighLight cssHighLight;

  @Override
  public void bind(CssEditor cssEditor) {
    JavaFxObservable.valuesOf(cssEditor.textProperty())
        .debounce(300, TimeUnit.MILLISECONDS)
        .observeOn(JavaFxScheduler.platform())
        .subscribe(e -> cssEditor.setStyleSpans(0, cssHighLight.compute(e)));
  }
}
