package xdean.css.editor.service;

import java.util.Optional;

import xdean.css.editor.control.CssEditor;

public interface ContextService {
  Optional<CssEditor> activeEditor();
}
