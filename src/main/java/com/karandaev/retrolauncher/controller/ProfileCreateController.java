package com.karandaev.retrolauncher.controller;

import com.karandaev.retrolauncher.Main;
import com.karandaev.retrolauncher.controller.interfaces.IController;
import com.karandaev.retrolauncher.model.UserProfile;
import com.karandaev.retrolauncher.utils.ConfigManager;
import com.karandaev.retrolauncher.utils.LanguageManager;
import com.karandaev.retrolauncher.utils.LogManager;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import static com.karandaev.retrolauncher.Main.*;

/** Controller for the new profile creation window. */
public class ProfileCreateController implements IController {

  @FXML private TextField usernameField;
  @FXML private ComboBox<String> languageComboBox;
  @FXML private ComboBox<String> themeComboBox;
  @FXML private ComboBox<String> baseProfileComboBox;

  private static String username = null;
  private static String baseProfile = null;
  private static String languageShort = "en";
  private static String themeEn = "light";

  @FXML
  public void initialize() {
    // Close window event
    getSceneProperty()
        .addListener(
            (observable, oldScene, newScene) -> {
              if (newScene != null) {
                newScene
                    .windowProperty()
                    .addListener(
                        (observableValue, oldWindow, newWindow) -> {
                          if (newWindow != null) {
                            Stage stage = (Stage) newWindow;
                            stage.setOnCloseRequest(windowEvent -> onClosing());
                          }
                        });
              }
            });

    // Filling comboboxes
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

    // Loading existing profiles into ComboBox
    List<UserProfile> profiles = ConfigManager.getInstance().getProfiles();
    baseProfileComboBox
        .getItems()
        .add(LanguageManager.getResourceBundle().getString("profile.create.none"));
    for (UserProfile profile : profiles) {
      baseProfileComboBox.getItems().add(profile.getUsername());
    }

    // Restore values
    usernameField.setText(username);
    baseProfileComboBox.setValue(
        Objects.requireNonNullElseGet(
            baseProfile,
            () -> LanguageManager.getResourceBundle().getString("profile.create.none")));

    if (Objects.equals(languageShort, "en")) {
      languageComboBox.setValue(LanguageManager.getResourceBundle().getString("language.english"));
    } else {
      languageComboBox.setValue(LanguageManager.getResourceBundle().getString("language.russian"));
    }

    if (Objects.equals(themeEn, "light")) {
      themeComboBox.setValue(LanguageManager.getResourceBundle().getString("theme.light"));
    } else {
      themeComboBox.setValue(LanguageManager.getResourceBundle().getString("theme.dark"));
    }

    // Set listeners
    usernameField
        .textProperty()
        .addListener(
            (observableValue, oldV, newV) -> {
              username = newV;
            });
    baseProfileComboBox
        .valueProperty()
        .addListener(
            (observableValue, oldV, newV) -> {
              baseProfile = newV;
              if (Objects.equals(
                  newV, LanguageManager.getResourceBundle().getString("profile.create.none"))) {
                baseProfile = null;
              }
            });

    languageComboBox
        .valueProperty()
        .addListener(
            (observableValue, oldV, newV) -> {
              if (LanguageManager.getResourceBundle().getString("language.russian").equals(newV)) {
                LanguageManager.setLocale(new java.util.Locale("ru"));
                languageShort = "ru";
              } else {
                LanguageManager.setLocale(new java.util.Locale("en"));
                languageShort = "en";
              }
              reloadWindow();
            });

    themeComboBox
        .valueProperty()
        .addListener(
            (observableValue, oldV, newV) -> {
              if (LanguageManager.getResourceBundle().getString("theme.light").equals(newV)) {
                themeEn = "light";
              } else {
                themeEn = "dark";
              }
              reloadWindow();
            });
  }

  @FXML
  private void createNewProfile() {
    if (username == null || username.isEmpty()) {
      getAlert(
              getClass(),
              Alert.AlertType.ERROR,
              LanguageManager.getResourceBundle().getString("alert.error"),
              LanguageManager.getResourceBundle().getString("alert.empty.name"),
              "")
          .showAndWait();
      return;
    }

    if (!ConfigManager.getInstance().getProfiles().isEmpty()) {
      var duplicateExist =
          ConfigManager.getInstance().getProfiles().stream()
              .map(UserProfile::getUsername)
              .map(username::equals)
              .findAny()
              .get();
      if (duplicateExist) {
        getAlert(
                getClass(),
                Alert.AlertType.ERROR,
                LanguageManager.getResourceBundle().getString("alert.error"),
                LanguageManager.getResourceBundle().getString("alert.duplicate.name"),
                "")
            .showAndWait();
        return;
      }
    }

    if (LanguageManager.getAllTranslates("profile.create.none").contains(username)) {
      getAlert(
              getClass(),
              Alert.AlertType.ERROR,
              LanguageManager.getResourceBundle().getString("alert.error"),
              LanguageManager.getResourceBundle().getString("alert.none.name"),
              "")
          .showAndWait();
      return;
    }

    UserProfile newProfile;

    // Checking base profile selection
    if (baseProfile != null) {
      // Searching for base profile
      UserProfile baseUserProfile = null;
      for (UserProfile profile : ConfigManager.getInstance().getProfiles()) {
        if (profile.getUsername().equals(baseProfile)) {
          baseUserProfile = profile;
          break;
        }
      }
      if (baseUserProfile != null) {
        // Creating a copy of the base profile
        newProfile = new UserProfile(baseUserProfile);
        newProfile.setUsername(username);
      } else {
        // Base profile not found, creating new one
        newProfile = new UserProfile(username);
      }
    } else {
      // Base profile not selected, creating new one
      newProfile = new UserProfile(username);
    }

    // Applying language settings
    newProfile.setPreferredLanguageShort(languageShort);

    // Applying theme settings
    newProfile.setPreferredThemeEn(themeEn);

    // Adding new profile to configuration
    ConfigManager.getInstance().getProfiles().add(newProfile);
    ConfigManager.getInstance().getProfiles().sort(Comparator.comparing(UserProfile::getUsername));
    var profileIndex = ConfigManager.getInstance().getProfiles().indexOf(newProfile);
    ConfigManager.getInstance().setCurrentUserProfileIndex(profileIndex);
    ConfigManager.getInstance().saveConfig();

    // Resetting static fields
    setDefaultStaticFields();

    // Closing the window
    Stage stage = (Stage) getScene().getWindow();
    stage.close();
    onClosing();
  }

  private void reloadWindow() {
    try {
      Stage oldStage = (Stage) getScene().getWindow();
      var newPair =
          getWindow(
              getClass(),
              "/view/profile_create.fxml",
              LanguageManager.getResourceBundle().getString("menu.profile.settings"));
      var newStage = newPair.getKey();
      newStage.getScene().getStylesheets().add(THEME_CSS_BASE + themeEn + ".css");
      Main.reloadWindow(oldStage, newStage);
    } catch (IOException e) {
      LogManager.getLogger().severe(e.getMessage());
      e.printStackTrace();
    }
  }

  private static void setDefaultStaticFields() {
    username = null;
    baseProfile = null;
    languageShort = "en";
    themeEn = "light";
  }

  private void onClosing() {
    if (ConfigManager.getInstance().getProfiles().isEmpty()) {
      Platform.exit();
    } else {
      // Restore theme
      String preferredTheme =
          ConfigManager.getInstance().getCurrentUserProfile().getPreferredThemeEn();
      setTheme(preferredTheme);

      // Restore language
      String preferredLanguage =
          ConfigManager.getInstance().getCurrentUserProfile().getPreferredLanguageShort();
      LanguageManager.setLocale(new Locale(preferredLanguage));
    }
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
