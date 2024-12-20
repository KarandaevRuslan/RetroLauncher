package com.karandaev.retrolauncher;

import com.formdev.flatlaf.FlatLightLaf;
import com.karandaev.retrolauncher.controller.interfaces.IController;
import com.karandaev.retrolauncher.model.UserProfile;
import com.karandaev.retrolauncher.utils.ConfigManager;
import com.karandaev.retrolauncher.utils.LanguageManager;
import com.karandaev.retrolauncher.utils.LogManager;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.Objects;
import javafx.util.Pair;
import com.formdev.flatlaf.FlatDarkLaf;

import javax.swing.*;

import static com.karandaev.retrolauncher.utils.FileManager.cleanUp;

/** Main application class for RetroLauncher. */
public class Main extends Application {
  public static final String PROGRAM_NAME = "Retro Launcher";
  public static final String PROGRAM_ICON_PATH = "/icons/retro_launcher_icon.png";
  public static final String THEME_CSS_BASE = "/atlantafx/base/theme/primer-";

  @Override
  public void start(Stage primaryStage) {
    try {
      // Clean up update files and folders
      cleanUp();

      // Initialize configuration
      ConfigManager.getInstance().loadConfig();

      // Load user profiles and select active profile
      UserProfile activeProfile = ConfigManager.getInstance().getCurrentUserProfile();
      if (activeProfile == null) {
        // No active profile, open profile selection window
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
      }
      activeProfile = ConfigManager.getInstance().getCurrentUserProfile();
      if (activeProfile == null) {
        return;
      } else {
        String preferredTheme =
            ConfigManager.getInstance().getCurrentUserProfile().getPreferredThemeEn();
        setTheme(preferredTheme);

        String preferredLanguage =
            ConfigManager.getInstance().getCurrentUserProfile().getPreferredLanguageShort();
        LanguageManager.setLocale(new Locale(preferredLanguage));
      }

      getWindow(getClass(), "/view/main.fxml", PROGRAM_NAME).getKey().show();

    } catch (Exception e) {
      e.printStackTrace();
      // Log the exception
      LogManager.getLogger().severe("Failed to start application: " + e.getMessage());
    }
  }

  public static void setTheme(String theme) {
    Application.setUserAgentStylesheet(THEME_CSS_BASE + theme.toLowerCase() + ".css");
  }

  public static void reloadWindow(Stage oldStage, Stage newStage) {
    var hStage = oldStage.getHeight();
    var wStage = oldStage.getWidth();

    oldStage.setScene(newStage.getScene());
    oldStage.setTitle(newStage.getTitle());

    oldStage.sizeToScene();
    oldStage.setHeight(hStage);
    oldStage.setWidth(wStage);
  }

  public static Pair<Stage, IController> getWindow(Class<?> clazz, String fxmlPath, String name)
      throws IOException {
    return getWindow(clazz, fxmlPath, name, PROGRAM_ICON_PATH);
  }

  public static Pair<Stage, IController> getWindow(
      Class<?> clazz, String fxmlPath, String name, String iconPath) throws IOException {
    var fxml = clazz.getResource(fxmlPath);

    // Load main FXML layout
    FXMLLoader loader = new FXMLLoader(fxml);
    loader.setResources(LanguageManager.getResourceBundle());
    Scene scene = new Scene(loader.load());

    // Get controller
    var controller = loader.getController();

    Stage stage = new Stage();
    stage.setScene(scene);
    stage.setTitle(name);
    stage.getIcons().add(new Image(Objects.requireNonNull(clazz.getResourceAsStream(iconPath))));
    return new Pair<>(stage, (IController) controller);
  }

  public static Alert getAlert(
      Class<?> clazz, Alert.AlertType type, String title, String header, String content) {
    return getAlert(clazz, type, title, PROGRAM_ICON_PATH, header, content);
  }

  public static Alert getAlert(
      Class<?> clazz,
      Alert.AlertType type,
      String title,
      String iconPath,
      String header,
      String content) {
    Alert alert = new Alert(Alert.AlertType.INFORMATION);
    alert.setTitle(title);
    ((Stage) alert.getDialogPane().getScene().getWindow())
        .getIcons()
        .add(new Image(Objects.requireNonNull(clazz.getResourceAsStream(iconPath))));
    alert.setHeaderText(header);
    alert.setContentText(content);

    return alert;
  }

  public static void centerColumnHeaderText(TableColumn<?, ?> column) {
    Label label = new Label(column.getText());
    label.setAlignment(Pos.CENTER);
    StackPane stack = new StackPane(label);
    stack.setPrefWidth(column.getPrefWidth());
    column.setGraphic(stack);
    column.setText(null); // Removes standard header text
  }

  public static void selectOnDriveAndUpdateStringProperty(
      Scene scene, StringProperty stringProperty, String dialogTitle, int type) {

    Stage hiddenStage = new Stage();
    hiddenStage.initOwner(scene.getWindow());
    hiddenStage.initModality(Modality.WINDOW_MODAL);
    hiddenStage.setOpacity(0);
    hiddenStage.setWidth(1);
    hiddenStage.setHeight(1);

    try {
      if (ConfigManager.getInstance()
          .getCurrentUserProfile()
          .getPreferredThemeEn()
          .equals("light")) {
        UIManager.setLookAndFeel(new FlatLightLaf());

      } else {
        UIManager.setLookAndFeel(new FlatDarkLaf());
      }
    } catch (Exception e) {
      e.printStackTrace();
      LogManager.getLogger().severe(e.getMessage());
    }

    SwingUtilities.invokeLater(
        () -> {
          Platform.runLater(hiddenStage::show);

          // Create a JFileChooser instance
          JFileChooser fileChooser = new JFileChooser();

          // Allow selection of both files and directories
          fileChooser.setFileSelectionMode(type);
          fileChooser.setDialogTitle(dialogTitle);

          // Show the open dialog
          int result = fileChooser.showOpenDialog(null);

          // If a file or directory was selected
          if (result == JFileChooser.APPROVE_OPTION) {
            // Get the selected file or directory
            File selectedFile = fileChooser.getSelectedFile();
            stringProperty.set(selectedFile.getAbsolutePath());
          }

          Platform.runLater(hiddenStage::close);
        });
  }

  @Override
  public void stop() {
    // Save configuration on exit
    ConfigManager.getInstance().saveConfig();
  }

  public static void main(String[] args) {
    launch(args);
  }
}
