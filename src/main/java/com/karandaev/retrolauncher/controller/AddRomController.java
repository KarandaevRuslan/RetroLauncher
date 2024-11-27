package com.karandaev.retrolauncher.controller;

import com.karandaev.retrolauncher.Main;
import com.karandaev.retrolauncher.controller.interfaces.IController;
import com.karandaev.retrolauncher.model.Emulator;
import com.karandaev.retrolauncher.model.LaunchParameters;
import com.karandaev.retrolauncher.model.RomFile;
import com.karandaev.retrolauncher.utils.ConfigManager;
import com.karandaev.retrolauncher.utils.LanguageManager;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;
import java.util.*;

import static com.karandaev.retrolauncher.Main.getWindow;

/** Controller class for the Add ROM window. */
public class AddRomController implements IController {

  @FXML private TextField nameField;
  @FXML private TextField pathField;
  @FXML private TextField consolesField;
  @FXML private TextField genresField;
  @FXML private CheckBox favoriteCheckBox;
  @FXML private Button browseButton;
  @FXML private Button saveButton;
  @FXML private Button cancelButton;

  private RomFile newRom;

  @FXML
  public void initialize() {
    // Handle Browse button click to select ROM file
    browseButton.setOnAction(
        event -> {
          FileChooser fileChooser = new FileChooser();
          fileChooser.setTitle(
              LanguageManager.getResourceBundle().getString("file.chooser.select.rom.file"));
          File selectedFile = fileChooser.showOpenDialog(getScene().getWindow());
          if (selectedFile != null) {
            pathField.setText(selectedFile.getAbsolutePath());
          }
        });

    // Handle Save button click to add a new ROM
    saveButton.setOnAction(
        event -> {
          // Update consoles and genres fields
          consolesField.setText(
              String.join(
                  ",",
                  Arrays.stream(consolesField.getText().split(","))
                      .map(String::trim)
                      .distinct()
                      .filter(x -> !x.isEmpty())
                      .sorted()
                      .toArray(String[]::new)));
          genresField.setText(
              String.join(
                  ",",
                  Arrays.stream(genresField.getText().split(","))
                      .map(String::trim)
                      .distinct()
                      .filter(x -> !x.isEmpty())
                      .sorted()
                      .toArray(String[]::new)));

          if (isInputValid()) {
            String name = nameField.getText();
            File path = new File(pathField.getText());
            List<String> consoles = Arrays.asList(consolesField.getText().split(","));
            List<String> genres = Arrays.asList(genresField.getText().split(","));
            Boolean favorite = favoriteCheckBox.isSelected();
            Map<Integer, LaunchParameters> launchParameters = new HashMap<>();

            newRom =
                new RomFile(
                    name,
                    path,
                    new ArrayList<>(consoles),
                    new ArrayList<>(genres),
                    favorite,
                    launchParameters);

            // Save ROM config
            ConfigManager.getInstance().getCurrentUserProfile().getRoms().add(newRom);
            ConfigManager.getInstance()
                .getCurrentUserProfile()
                .getRoms()
                .sort(Comparator.comparing(RomFile::getName));
            ConfigManager.getInstance().saveConfig();

            // Close the window
            Stage stage = (Stage) getScene().getWindow();
            stage.close();
          }
        });

    // Handle Cancel button click to close the window without adding a ROM
    cancelButton.setOnAction(
        event -> {
          newRom = null;
          Stage stage = (Stage) getScene().getWindow();
          stage.close();
        });
  }

  /**
   * Validates user input.
   *
   * @return true if input is valid, false otherwise.
   */
  private boolean isInputValid() {
    String errorMessage = "";

    if (nameField.getText() == null || nameField.getText().isEmpty()) {
      errorMessage +=
          LanguageManager.getResourceBundle().getString("error.invalid.rom.name") + "\n";
    }
    if (ConfigManager.getInstance().getCurrentUserProfile().getRoms().stream()
        .map(RomFile::getName)
        .anyMatch(x -> x.equals(nameField.getText()))) {
      errorMessage +=
          LanguageManager.getResourceBundle().getString("error.duplicate.rom.name") + "\n";
    }
    if (pathField.getText() == null || pathField.getText().isEmpty()) {
      errorMessage +=
          LanguageManager.getResourceBundle().getString("error.invalid.rom.path") + "\n";
    } else {
      File file = new File(pathField.getText());
      if (!file.exists() || !file.isFile()) {
        errorMessage +=
            LanguageManager.getResourceBundle().getString("error.rom.path.not.exist") + "\n";
      }
    }
    if (consolesField.getText() == null || consolesField.getText().isEmpty()) {
      errorMessage +=
          LanguageManager.getResourceBundle().getString("error.invalid.rom.consoles") + "\n";
    }
    if (genresField.getText() == null || genresField.getText().isEmpty()) {
      errorMessage +=
          LanguageManager.getResourceBundle().getString("error.invalid.rom.genres") + "\n";
    }

    if (errorMessage.isEmpty()) {
      return true;
    } else {
      Alert alert =
          Main.getAlert(
              getClass(),
              Alert.AlertType.ERROR,
              LanguageManager.getResourceBundle().getString("alert.invalid.input.title"),
              null,
              errorMessage);
      alert.showAndWait();
      return false;
    }
  }

  @Override
  public ReadOnlyObjectProperty<Scene> getSceneProperty() {
    return nameField.sceneProperty();
  }

  @Override
  public Scene getScene() {
    return nameField.getScene();
  }
}
