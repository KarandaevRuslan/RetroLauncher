package com.karandaev.retrolauncher.controller;

import com.karandaev.retrolauncher.Main;
import com.karandaev.retrolauncher.controller.interfaces.IController;
import com.karandaev.retrolauncher.model.UserProfile;
import com.karandaev.retrolauncher.utils.ConfigManager;
import com.karandaev.retrolauncher.utils.LanguageManager;
import com.karandaev.retrolauncher.utils.LogManager;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Comparator;
import java.util.function.Supplier;

import static com.karandaev.retrolauncher.Main.*;

/** Controller class for the profile settings window. */
public class ProfileSettingsController implements IController {

  @FXML private TextField usernameField;
  @FXML private ComboBox<String> languageComboBox;
  @FXML private ComboBox<String> themeComboBox;
  private static Supplier<IController> reloadMainWindow;

  @FXML
  public void initialize() {
    languageComboBox
        .getItems()
        .addAll(
            LanguageManager.getResourceBundle().getString("language.english"),
            LanguageManager.getResourceBundle().getString("language.russian"));
    themeComboBox
        .getItems()
        .addAll(
            LanguageManager.getResourceBundle().getString("theme.light"),
            LanguageManager.getResourceBundle().getString("theme.dark"));

    UserProfile profile = ConfigManager.getInstance().getCurrentUserProfile();

    // Set current values
    languageComboBox.setValue(profile.getPreferredLanguage());
    themeComboBox.setValue(profile.getPreferredTheme());
    usernameField.setText(profile.getUsername());
  }

  @FXML
  private void applyProfileSettings() {
    String selectedLanguage = languageComboBox.getValue();
    String selectedTheme = themeComboBox.getValue();
    String newUsername = usernameField.getText();

    var duplicateExist =
        ConfigManager.getInstance().getProfiles().stream()
            .map(UserProfile::getUsername)
            .map(newUsername::equals)
            .findAny()
            .get();
    var usernameChanged =
        !ConfigManager.getInstance().getCurrentUserProfile().getUsername().equals(newUsername);
    if (duplicateExist && usernameChanged) {
      getAlert(
              getClass(),
              Alert.AlertType.ERROR,
              LanguageManager.getResourceBundle().getString("alert.error"),
              LanguageManager.getResourceBundle().getString("alert.duplicate.name"),
              "")
          .showAndWait();
      return;
    }

    if (LanguageManager.getAllTranslates("profile.create.none").contains(newUsername)) {
      getAlert(
              getClass(),
              Alert.AlertType.ERROR,
              LanguageManager.getResourceBundle().getString("alert.error"),
              LanguageManager.getResourceBundle().getString("alert.none.name"),
              "")
          .showAndWait();
      return;
    }

    UserProfile profile = ConfigManager.getInstance().getCurrentUserProfile();

    if (usernameChanged) {
      // Save and apply new username
      profile.setUsername(newUsername);
      ConfigManager.getInstance()
          .getProfiles()
          .sort(Comparator.comparing(UserProfile::getUsername));
      var profileIndex = ConfigManager.getInstance().getProfiles().indexOf(profile);
      ConfigManager.getInstance().setCurrentUserProfileIndex(profileIndex);
    }

    // Apply theme settings
    if (LanguageManager.getResourceBundle().getString("theme.light").equals(selectedTheme)) {
      profile.setPreferredThemeEn("light");
    } else {
      profile.setPreferredThemeEn("dark");
    }
    String preferredTheme =
        ConfigManager.getInstance().getCurrentUserProfile().getPreferredThemeEn();
    setTheme(preferredTheme);

    // Apply language settings
    if (LanguageManager.getResourceBundle()
        .getString("language.russian")
        .equals(selectedLanguage)) {
      LanguageManager.setLocale(new java.util.Locale("ru"));
      profile.setPreferredLanguageShort("ru");
    } else {
      LanguageManager.setLocale(new java.util.Locale("en"));
      profile.setPreferredLanguageShort("en");
    }

    // Save configuration
    ConfigManager.getInstance().saveConfig();

    // Reload UI
    reloadWindow();
    var mainController = (MainController) reloadMainWindow.get();
    reloadMainWindow = mainController::reloadWindow;
  }

  private void reloadWindow() {
    try {
      Stage oldStage = (Stage) getScene().getWindow();
      var newPair =
          getWindow(
              getClass(),
              "/view/profile_settings.fxml",
              LanguageManager.getResourceBundle().getString("menu.profile.settings"));
      Main.reloadWindow(oldStage, newPair.getKey());
    } catch (IOException e) {
      LogManager.getLogger().severe(e.getMessage());
      e.printStackTrace();
    }
  }

  public static void setReloadMainWindow(Supplier<IController> reloadMainWindow) {
    ProfileSettingsController.reloadMainWindow = reloadMainWindow;
  }

  @Override
  public ReadOnlyObjectProperty<Scene> getSceneProperty() {
    return usernameField.sceneProperty();
  }

  @Override
  public Scene getScene() {
    return usernameField.getScene();
  }
}
