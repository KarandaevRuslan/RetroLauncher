package com.karandaev.retrolauncher.controller;

import com.karandaev.retrolauncher.Main;
import com.karandaev.retrolauncher.controller.interfaces.IController;
import com.karandaev.retrolauncher.model.Emulator;
import com.karandaev.retrolauncher.model.LaunchParameters;
import com.karandaev.retrolauncher.model.RomFile;
import com.karandaev.retrolauncher.utils.LanguageManager;
import com.karandaev.retrolauncher.utils.LogManager;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.*;

public class EmulatorSelectionController implements IController {

  @FXML private VBox emulatorButtonContainer;
  @FXML private Button cancelButton;

  @FXML
  public void initialize() {}

  /** Populates the VBox with buttons for each emulator that supports the selected ROM. */
  public void populateEmulatorButtons(List<Emulator> compatibleEmulators, RomFile selectedRom) {
    for (Emulator emulator : compatibleEmulators) {
      Button emulatorButton = new Button(emulator.getName());
      emulatorButton.setMaxWidth(Double.MAX_VALUE);
      emulatorButton.setOnAction(
          event -> {
            launchRomWithEmulator(getClass(), emulator, selectedRom);
            closeWindow();
          });
      emulatorButtonContainer.getChildren().add(emulatorButton);
    }
  }

  /**
   * Launches the ROM with the selected emulator using stored launch parameters.
   *
   * @param emulator The selected emulator.
   * @param rom The selected game.
   */
  public static void launchRomWithEmulator(Class<?> clazz, Emulator emulator, RomFile rom) {
    try {
      // Build command with parameters
      List<String> command = new ArrayList<>();
      command.add(emulator.getExecutablePath().getAbsolutePath());

      // Get emulator's launch parameters
      String emulatorArgs = emulator.getLaunchParameters().getLaunchParameters();

      // Get ROM's launch parameters for this emulator
      LaunchParameters romLaunchParams = rom.getLaunchParameters().get(emulator.getId());
      String romArgs = (romLaunchParams != null) ? romLaunchParams.getLaunchParameters() : "";

      // Combine and replace placeholders
      String combinedArgs =
          combineAndReplacePlaceholders(emulatorArgs, romArgs, rom.getFilePath().getAbsolutePath());

      // Split arguments and add to command
      command.addAll(parseArguments(combinedArgs));

      // If no arguments, add ROM path by default
      if (command.size() == 1) {
        command.add(rom.getFilePath().getAbsolutePath());
      }

      // Set up the process builder
      ProcessBuilder pb = new ProcessBuilder(command);
      pb.directory(emulator.getExecutablePath().getParentFile());

      // Start the process
      Process process = pb.start();
      LogManager.getLogger()
          .info("Launched emulator: " + emulator.getName() + " with ROM: " + rom.getName());

    } catch (IOException e) {
      e.printStackTrace();
      LogManager.getLogger().severe("Failed to launch emulator: " + e.getMessage());
      Alert alert =
          Main.getAlert(
              clazz,
              Alert.AlertType.WARNING,
              LanguageManager.getResourceBundle().getString("alert.launch.failed.title"),
              null,
              LanguageManager.getResourceBundle().getString("alert.launch.failed.content"));
      alert.showAndWait();
    }
  }

  /**
   * Combines emulator and ROM launch parameters and replaces placeholders.
   *
   * @param emulatorArgs Emulator launch parameters.
   * @param romArgs ROM-specific launch parameters.
   * @param romAbsolutePath The absolute path to ROM.
   * @return The combined and processed argument string.
   */
  private static String combineAndReplacePlaceholders(
      String emulatorArgs, String romArgs, String romAbsolutePath) {
    String combinedArgs;
    if (romArgs.contains("{emu}")) {
      combinedArgs = romArgs.replace("{emu}", emulatorArgs);
    } else {
      combinedArgs = emulatorArgs + " " + romArgs;
    }
    if (combinedArgs.contains("{rom}")) {
      combinedArgs = combinedArgs.replace("{rom}", "\"" + romAbsolutePath + "\"");
    } else {
      combinedArgs += " \"" + romAbsolutePath + "\"";
    }

    return combinedArgs.trim();
  }

  /**
   * Parses a command-line arguments string into a list of arguments.
   *
   * @param args The argument string.
   * @return A list of arguments.
   */
  private static List<String> parseArguments(String args) {
    if (args == null || args.isEmpty()) {
      return Collections.emptyList();
    }
    // Use regex to split by spaces, respecting quoted substrings
    List<String> arguments = new ArrayList<>();
    Scanner scanner = new Scanner(args);
    while (scanner.hasNext()) {
      arguments.add(scanner.next());
    }
    scanner.close();
    return arguments;
  }

  @FXML
  private void closeWindow() {
    Stage stage = (Stage) getScene().getWindow();
    stage.close();
  }

  @Override
  public ReadOnlyObjectProperty<Scene> getSceneProperty() {
    return emulatorButtonContainer.sceneProperty();
  }

  @Override
  public Scene getScene() {
    return emulatorButtonContainer.getScene();
  }
}
