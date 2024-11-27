package com.karandaev.retrolauncher.controller;

import com.karandaev.retrolauncher.Main;
import com.karandaev.retrolauncher.controller.interfaces.IController;
import com.karandaev.retrolauncher.model.Emulator;
import com.karandaev.retrolauncher.model.LaunchParameters;
import com.karandaev.retrolauncher.model.RomFile;
import com.karandaev.retrolauncher.utils.ConfigManager;
import com.karandaev.retrolauncher.utils.LanguageManager;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.*;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

/** Controller class for the Edit ROM window. */
public class EditRomController implements IController {

  @FXML private TextField nameField;
  @FXML private TextField pathField;
  @FXML private TextField consolesField;
  @FXML private TextField genresField;
  @FXML private CheckBox favoriteCheckBox;
  @FXML private Button browseButton;
  @FXML private Button saveButton;
  @FXML private Button cancelButton;
  @FXML private TitledPane launchParametersPane;
  @FXML private GridPane launchParametersGrid;

  private RomFile rom;

  @FXML
  public void initialize() {
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

                            launchParametersPane
                                .layoutYProperty()
                                .addListener(
                                    observable1 -> {
                                      launchParametersGrid
                                          .layoutYProperty()
                                          .addListener(
                                              observable2 -> {
                                                stage.setMaxHeight(
                                                    launchParametersPane.getLayoutY()
                                                        + 83
                                                        + 40
                                                        + launchParametersGrid.getHeight());
                                                stage.setMinHeight(
                                                    launchParametersPane.getLayoutY()
                                                        + 83
                                                        + 40
                                                        + launchParametersGrid.getHeight());
                                              });
                                    });

                            launchParametersPane
                                .expandedProperty()
                                .addListener(
                                    (obs, wasExpanded, isNowExpanded) ->
                                        animateWindowHeight(stage, isNowExpanded));
                          }
                        });
              }
            });

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

    // Handle Save button click to update ROM details
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
            rom.setName(nameField.getText());
            rom.setFilePath(new File(pathField.getText()));
            List<String> consoles = Arrays.asList(consolesField.getText().split(","));
            List<String> genres = Arrays.asList(genresField.getText().split(","));
            rom.getConsoles().clear();
            rom.getConsoles().addAll(new ArrayList<>(consoles));
            rom.getGenres().clear();
            rom.getGenres().addAll(new ArrayList<>(genres));
            rom.setFavorite(favoriteCheckBox.isSelected());

            for (Node node : launchParametersGrid.getChildren()) {
              if (node instanceof Label) {
                Label label = (Label) node;
                Integer row = GridPane.getRowIndex(label);
                if (row == null) row = 0;

                for (Node sibling : launchParametersGrid.getChildren()) {
                  if (sibling instanceof TextField) {
                    Integer siblingRow = GridPane.getRowIndex(sibling);
                    if (siblingRow == null) siblingRow = 0;
                    if (siblingRow.equals(row)) {
                      TextField parametersField = (TextField) sibling;
                      String emulatorName = label.getText();
                      Emulator emulator = findEmulatorByName(emulatorName);
                      if (emulator != null) {
                        LaunchParameters lp = new LaunchParameters();
                        lp.setLaunchParameters(parametersField.getText());
                        rom.getLaunchParameters().put(emulator.getId(), lp);
                      }
                      break;
                    }
                  }
                }
              }
            }

            // Save ROM config
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

    // Handle Cancel button click to close the window without saving changes
    cancelButton.setOnAction(
        event -> {
          // Close the window without changes
          Stage stage = (Stage) getScene().getWindow();
          stage.close();
        });
  }

  /**
   * Sets the ROM to be edited and populates the fields with its current data.
   *
   * @param rom The ROM to edit.
   */
  public void setRom(RomFile rom) {
    this.rom = rom;

    nameField.setText(rom.getName());
    pathField.setText(rom.getFilePath().getAbsolutePath());
    consolesField.setText(String.join(",", rom.getConsoles()));
    genresField.setText(String.join(",", rom.getGenres()));
    favoriteCheckBox.setSelected(rom.isFavorite());

    // Populate launch parameters for supporting emulators
    populateLaunchParameters();
  }

  private void populateLaunchParameters() {
    // Clear existing children
    launchParametersGrid.getChildren().clear();

    // Retrieve emulators and ROM consoles
    List<Emulator> emulators = ConfigManager.getInstance().getCurrentUserProfile().getEmulators();
    Set<String> romConsoles =
        rom.getConsoles().stream().map(String::toLowerCase).collect(Collectors.toSet());

    int row = 0;
    for (Emulator emulator : emulators) {
      Set<String> emulatorConsoles =
          emulator.getSupportedConsoles().stream()
              .map(String::toLowerCase)
              .collect(Collectors.toSet());
      emulatorConsoles.retainAll(romConsoles);

      if (!emulatorConsoles.isEmpty()) {
        Label nameLabel = new Label(emulator.getName());
        TextField paramsField = new TextField();

        LaunchParameters lp = rom.getLaunchParameters().get(emulator.getId());
        if (lp != null) {
          paramsField.setText(lp.getLaunchParameters());
        }

        GridPane.setHgrow(paramsField, Priority.ALWAYS);
        launchParametersGrid.add(nameLabel, 0, row);
        launchParametersGrid.add(paramsField, 1, row);
        row++;
      }
    }
  }

  private Emulator findEmulatorByName(String name) {
    for (Emulator emulator : ConfigManager.getInstance().getCurrentUserProfile().getEmulators()) {
      if (emulator.getName().equals(name)) {
        return emulator;
      }
    }
    return null;
  }

  private void animateWindowHeight(Stage stage, Boolean isNowExpanded) {

    // Calculate the target height
    double initialHeight = stage.getHeight();
    double targetHeight = launchParametersPane.getLayoutY() + 83;
    if (isNowExpanded) {
      targetHeight += 40 + launchParametersGrid.getHeight();
    }
    stage.setMaxHeight(99999);
    stage.setMinHeight(0);

    // Create a DoubleProperty to animate
    DoubleProperty heightProperty = new SimpleDoubleProperty(initialHeight);
    double finalTargetHeight = targetHeight;
    heightProperty.addListener(
        (obs, oldVal, newVal) -> {
          stage.setHeight(newVal.doubleValue());
          if (newVal.doubleValue() - finalTargetHeight < 0.001) {
            stage.setMaxHeight(finalTargetHeight);
            stage.setMinHeight(finalTargetHeight);
          }
        });

    // Create a timeline to animate the height
    Timeline timeline = new Timeline();
    KeyValue kv = new KeyValue(heightProperty, targetHeight, Interpolator.EASE_BOTH);
    KeyFrame kf = new KeyFrame(Duration.millis(300), kv);
    timeline.getKeyFrames().add(kf);
    timeline.play();
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
    if (!nameField.getText().equals(rom.getName())
        && ConfigManager.getInstance().getCurrentUserProfile().getRoms().stream()
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
