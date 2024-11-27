package com.karandaev.retrolauncher.controller;

import com.karandaev.retrolauncher.controller.interfaces.IController;
import com.karandaev.retrolauncher.utils.ConfigManager;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.web.WebView;

/** Controller class for the help window. */
public class HelpController implements IController {

  @FXML private WebView webView;

  @FXML
  public void initialize() {
    String filename =
        "/help/help_"
            + ConfigManager.getInstance().getCurrentUserProfile().getPreferredLanguageShort()
            + ".html";
    String url = getClass().getResource(filename).toExternalForm();
    webView.getEngine().load(url);

    webView
        .getEngine()
        .documentProperty()
        .addListener(
            (obs, oldDoc, newDoc) -> {
              if (newDoc != null) {
                setTheme();
              }
            });
  }

  private void setTheme() {
    String theme = ConfigManager.getInstance().getCurrentUserProfile().getPreferredThemeEn();
    var themeCss = getClass().getResource("/styles/themes/" + theme.toLowerCase() + ".css");
    webView
        .getEngine()
        .executeScript("document.getElementById('theme-link').href = '" + themeCss + "';");
  }

  @Override
  public ReadOnlyObjectProperty<Scene> getSceneProperty() {
    return webView.sceneProperty();
  }

  @Override
  public Scene getScene() {
    return webView.getScene();
  }
}
