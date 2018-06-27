package xdean.css.editor.feature;

import static xdean.css.editor.context.Context.LAST_FILE_PATH;
import static xdean.jex.util.lang.ExceptionUtil.uncheck;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;

import org.springframework.stereotype.Component;

import javafx.stage.Stage;
import xdean.css.editor.context.setting.FileActions;
import xdean.css.editor.context.setting.PreferenceSettings;
import xdean.css.editor.control.CssEditor;
import xdean.css.editor.model.FileWrapper;
import xdean.css.editor.service.ContextService;
import xdean.css.editor.service.DialogService;
import xdean.jex.extra.tryto.Try;
import xdean.jex.util.collection.ListUtil;
import xdean.jex.util.file.FileUtil;

@Component
public class LastFileFeature implements CssAppFeature {

  private @Inject Provider<CssEditor> editorFactory;
  private @Inject ContextService contextService;
  private @Inject PreferenceSettings options;
  private @Inject FileActions fileActions;
  private @Inject DialogService dialogService;

  @Override
  public void bind(Stage stage) {
    stage.addEventHandler(fileActions.exit().getEventType(), e -> save());
    if (options.openLast().getValue()) {
      open();
    }
  }

  public void open() {
    try {
      FileUtil.createDirectory(LAST_FILE_PATH);
      Files.newDirectoryStream(LAST_FILE_PATH, "*.tmp").forEach(p -> uncheck(() -> {
        List<String> lines = Files.readAllLines(p, options.charset().getValue());
        if (lines.isEmpty()) {
          return;
        }
        String head = lines.get(0);
        CssEditor editor = editorFactory.get();
        editor.fileProperty().set(Try.to(() -> Integer.valueOf(head)).map(i -> FileWrapper.newFile(i))
            .getOrElse(() -> FileWrapper.existFile(Paths.get(head))));
        lines.stream()
            .skip(1)
            .reduce((a, b) -> String.join(System.lineSeparator(), a, b))
            .ifPresent(t -> {
              editor.replaceText(t);
              editor.getUndoManager().forgetHistory();
            });
        contextService.editorList().add(editor);
      }));
    } catch (IOException e) {
      dialogService.showError("Fail to open last files.", e);
    }
  }

  private void save() {
    if (options.openLast().getValue()) {
      uncheck(() -> FileUtil.createDirectory(LAST_FILE_PATH));
      uncheck(() -> Files.newDirectoryStream(LAST_FILE_PATH, "*.tmp").forEach(p -> uncheck(() -> Files.delete(p))));
      ListUtil.forEach(contextService.editorList(), (e, i) -> {
        String nameString = e.fileProperty().get().fileOrNew.unify(p -> p.toString(), n -> n.toString());
        String text = e.modifiedProperty().get() ? e.getText() : "";
        Path path = LAST_FILE_PATH.resolve(String.format("%s.tmp", i));
        try {
          Files.write(path, String.join("\n", nameString, text).getBytes(options.charset().getValue()));
        } catch (IOException e1) {
          dialogService.showError("Fail to save last files.", e1);
        }
      });
    }
  }
}
