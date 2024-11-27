package com.karandaev.retrolauncher;

import com.karandaev.retrolauncher.controller.interfaces.IController;
import com.karandaev.retrolauncher.model.UserProfile;
import com.karandaev.retrolauncher.utils.ConfigManager;
import com.karandaev.retrolauncher.utils.LanguageManager;
import com.karandaev.retrolauncher.utils.LogManager;
import com.karandaev.retrolauncher.utils.UpdateManager;
import javafx.application.Application;
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

import java.io.IOException;
import java.util.Locale;
import java.util.Objects;
import javafx.util.Pair;

/** Main application class for RetroLauncher. */
public class Main extends Application {
  public static final String PROGRAM_NAME = "Retro Launcher";
  public static final String PROGRAM_ICON_PATH = "/icons/retro_launcher_icon.png";
  public static final String THEME_CSS_BASE = "/atlantafx/base/theme/primer-";

  @Override
  public void start(Stage primaryStage) {
    try {
      // Initialize configuration
      ConfigManager.getInstance().loadConfig();

      // Check for updates
      UpdateManager.checkForUpdates();

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

  @Override
  public void stop() {
    // Save configuration on exit
    ConfigManager.getInstance().saveConfig();
  }

  public static void main(String[] args) {
    launch(args);
  }
}
