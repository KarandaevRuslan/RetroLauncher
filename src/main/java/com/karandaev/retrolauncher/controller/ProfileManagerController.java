package com.karandaev.retrolauncher.controller;

import com.karandaev.retrolauncher.Main;
import com.karandaev.retrolauncher.controller.interfaces.IController;
import com.karandaev.retrolauncher.model.UserProfile;
import com.karandaev.retrolauncher.utils.ConfigManager;
import com.karandaev.retrolauncher.utils.LanguageManager;
import com.karandaev.retrolauncher.utils.LogManager;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Locale;
import java.util.function.Supplier;

import static com.karandaev.retrolauncher.Main.*;

public class ProfileManagerController implements IController {

  @FXML private ListView<UserProfile> profileListView;
  @FXML private Button createProfileButton;

  private static Supplier<IController> reloadMainWindow;

  @FXML
  public void initialize() {

    ObservableList<UserProfile> profiles =
        FXCollections.observableArrayList(ConfigManager.getInstance().getProfiles());
    profileListView.setItems(profiles);

    profileListView.setOnMouseClicked(this::handleMouseClick);
    createProfileButton.setOnAction(event -> openCreateProfileWindow());

    // Add context menu to list items
    profileListView.setCellFactory(
        lv -> {
          ListCell<UserProfile> cell =
              new ListCell<>() {
                @Override
                protected void updateItem(UserProfile profile, boolean empty) {
                  super.updateItem(profile, empty);
                  if (empty || profile == null) {
                    setText(null);
                    setStyle("");
                  } else {
                    setText(profile.getUsername());
                    if (profile.equals(ConfigManager.getInstance().getCurrentUserProfile())) {
                      setStyle("-fx-font-weight: bold;");
                    } else {
                      setStyle("");
                    }
                    //  -fx-alignment: CENTER;
                  }
                }
              };

          cell.setOnMouseClicked(
              event -> {
                if (event.getButton() == MouseButton.SECONDARY && !cell.isEmpty()) {
                  UserProfile selectedProfile = cell.getItem();

                  ContextMenu contextMenu = new ContextMenu();

                  MenuItem selectItem =
                      new MenuItem(
                          LanguageManager.getResourceBundle().getString("menu.select.profile"));
                  selectItem.setOnAction(e -> selectProfile(selectedProfile));

                  contextMenu.getItems().add(selectItem);

                  if (!selectedProfile.equals(
                      ConfigManager.getInstance().getCurrentUserProfile())) {
                    MenuItem deleteItem =
                        new MenuItem(
                            LanguageManager.getResourceBundle().getString("menu.delete.profile"));
                    deleteItem.setOnAction(e -> deleteProfile(selectedProfile));
                    contextMenu.getItems().add(deleteItem);
                  }

                  contextMenu.show(cell, event.getScreenX(), event.getScreenY());
                }
              });

          return cell;
        });
  }

  private void handleMouseClick(MouseEvent event) {
    if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
      UserProfile selectedProfile = profileListView.getSelectionModel().getSelectedItem();
      if (selectedProfile != null) {
        selectProfile(selectedProfile);
      }
    }
  }

  private void selectProfile(UserProfile profile) {
    int index = ConfigManager.getInstance().getProfiles().indexOf(profile);
    if (index != -1) {
      ConfigManager.getInstance().setCurrentUserProfileIndex(index);
      ConfigManager.getInstance().saveConfig();

      // Change theme
      String preferredTheme =
          ConfigManager.getInstance().getCurrentUserProfile().getPreferredThemeEn();
      setTheme(preferredTheme);

      // Change language
      String preferredLanguage =
          ConfigManager.getInstance().getCurrentUserProfile().getPreferredLanguageShort();
      LanguageManager.setLocale(new Locale(preferredLanguage));

      // Reload UI
      reloadWindow();
      var mainController = (MainController) reloadMainWindow.get();
      reloadMainWindow = mainController::reloadWindow;
    }
  }

  private void deleteProfile(UserProfile profile) {
    UserProfile currentProfile = ConfigManager.getInstance().getCurrentUserProfile();
    ConfigManager.getInstance().getProfiles().remove(profile);
    var currentProfileIndex = ConfigManager.getInstance().getProfiles().indexOf(currentProfile);
    ConfigManager.getInstance().setCurrentUserProfileIndex(currentProfileIndex);
    ConfigManager.getInstance().saveConfig();

    // Reload window
    reloadWindow();
  }

  private void openCreateProfileWindow() {
    try {
      LanguageManager.setLocale(new Locale("en"));

      var pair =
          getWindow(
              getClass(),
              "/view/profile_create.fxml",
              LanguageManager.getResourceBundle().getString("menu.profile.create"));
      Stage createProfileStage = pair.getKey();
      createProfileStage.getScene().getStylesheets().add(THEME_CSS_BASE + "light.css");
      createProfileStage.initModality(Modality.APPLICATION_MODAL);
      createProfileStage.setResizable(false);
      createProfileStage.showAndWait();
    } catch (IOException e) {
      LogManager.getLogger().severe(e.getMessage());
      e.printStackTrace();
    }

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
              "/view/profile_manager.fxml",
              LanguageManager.getResourceBundle().getString("menu.profile.manager"));
      Main.reloadWindow(oldStage, newPair.getKey());
    } catch (IOException e) {
      LogManager.getLogger().severe(e.getMessage());
      e.printStackTrace();
    }
  }

  public static void setReloadMainWindow(Supplier<IController> reloadMainWindow) {
    ProfileManagerController.reloadMainWindow = reloadMainWindow;
  }

  @Override
  public ReadOnlyObjectProperty<Scene> getSceneProperty() {
    return profileListView.sceneProperty();
  }

  @Override
  public Scene getScene() {
    return profileListView.getScene();
  }
}
