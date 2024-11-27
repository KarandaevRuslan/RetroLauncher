package com.karandaev.retrolauncher.controller;

import com.karandaev.retrolauncher.Main;
import com.karandaev.retrolauncher.controller.interfaces.IController;
import com.karandaev.retrolauncher.model.Emulator;
import com.karandaev.retrolauncher.model.LaunchParameters;
import com.karandaev.retrolauncher.utils.ConfigManager;
import com.karandaev.retrolauncher.utils.LanguageManager;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.swing.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import static com.karandaev.retrolauncher.Main.selectOnDriveAndUpdateStringProperty;

/** Controller class for the Edit Emulator window. */
public class EditEmulatorController implements IController {

  @FXML private TextField nameField;
  @FXML private TextField pathField;
  @FXML private TextField consolesField;
  @FXML private TextField launchParametersField;
  @FXML private Button browseButton;
  @FXML private Button saveButton;
  @FXML private Button cancelButton;

  private Emulator emulator;

  @FXML
  public void initialize() {
    // Handle Browse button click to select emulator executable
    browseButton.setOnAction(
        event -> {
          selectOnDriveAndUpdateStringProperty(
              pathField.textProperty(),
              LanguageManager.getResourceBundle()
                  .getString("file.chooser.select.emulator.executable"),
              JFileChooser.FILES_ONLY);
        });

    // Handle Save button click to update emulator details
    saveButton.setOnAction(
        event -> {
          // Update supported consoles field
          consolesField.setText(
              String.join(
                  ",",
                  Arrays.stream(consolesField.getText().split(","))
                      .map(String::trim)
                      .distinct()
                      .filter(x -> !x.isEmpty())
                      .sorted()
                      .toArray(String[]::new)));

          if (isInputValid()) {
            emulator.setName(nameField.getText());
            emulator.setExecutablePath(new File(pathField.getText()));
            String[] consoles = consolesField.getText().split(",");
            var supportedConsoles = emulator.getSupportedConsoles();
            supportedConsoles.clear();
            supportedConsoles.addAll(new ArrayList<>(Arrays.asList(consoles)));
            emulator.getLaunchParameters().setLaunchParameters(launchParametersField.getText());

            // Save emulator config
            ConfigManager.getInstance()
                .getCurrentUserProfile()
                .getEmulators()
                .sort(Comparator.comparing(Emulator::getName));
            ConfigManager.getInstance().saveConfig();

            // Close the window
            Stage stage = (Stage) getScene().getWindow();
            stage.close();
          }
        });

    // Handle Cancel button click to close the window without saving changes
    cancelButton.setOnAction(
        event -> {
          // Close the window without changes
          Stage stage = (Stage) getScene().getWindow();
          stage.close();
        });
  }

  /**
   * Sets the emulator to be edited and populates the fields with its current data.
   *
   * @param emulator The emulator to edit.
   */
  public void setEmulator(Emulator emulator) {
    this.emulator = emulator;

    nameField.setText(emulator.getName());
    pathField.setText(emulator.getExecutablePath().getAbsolutePath());
    consolesField.setText(String.join(",", emulator.getSupportedConsoles()));
    if (emulator.getLaunchParameters() != null) {
      launchParametersField.setText(emulator.getLaunchParameters().getLaunchParameters());
    } else {
      emulator.setLaunchParameters(new LaunchParameters());
    }
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
          LanguageManager.getResourceBundle().getString("error.invalid.emulator.name") + "\n";
    }
    if (!nameField.getText().equals(emulator.getName())
        && ConfigManager.getInstance().getCurrentUserProfile().getEmulators().stream()
            .map(Emulator::getName)
            .anyMatch(x -> x.equals(nameField.getText()))) {
      errorMessage +=
          LanguageManager.getResourceBundle().getString("error.duplicate.emulator.name") + "\n";
    }
    if (pathField.getText() == null || pathField.getText().isEmpty()) {
      errorMessage +=
          LanguageManager.getResourceBundle().getString("error.invalid.emulator.path") + "\n";
    } else {
      File file = new File(pathField.getText());
      if (!file.exists() || !file.isFile()) {
        errorMessage +=
            LanguageManager.getResourceBundle().getString("error.emulator.path.not.exist") + "\n";
      }
    }
    if (consolesField.getText() == null || consolesField.getText().isEmpty()) {
      errorMessage +=
          LanguageManager.getResourceBundle().getString("error.invalid.supported.consoles") + "\n";
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
