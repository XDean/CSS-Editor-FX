package xdean.css.editor.controller.service;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Dialog;

@Service
public class NodeFactoryService {

  @Inject
  SkinService skinService;

  public Scene createScene(Parent root) {
    Scene scene = new Scene(root);
    skinService.bind(scene);
    return scene;
  }

  public <T> Dialog<T> createDialog() {
    Dialog<T> dialog = new Dialog<>();
    skinService.bind(dialog);
    return dialog;
  }
}
