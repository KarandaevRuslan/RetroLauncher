package com.karandaev.retrolauncher.controller;

import com.karandaev.retrolauncher.Main;
import com.karandaev.retrolauncher.controller.interfaces.IController;
import com.karandaev.retrolauncher.model.Emulator;
import com.karandaev.retrolauncher.utils.ConfigManager;
import com.karandaev.retrolauncher.utils.LanguageManager;
import com.karandaev.retrolauncher.utils.LogManager;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.function.Supplier;

import static com.karandaev.retrolauncher.Main.centerColumnHeaderText;
import static com.karandaev.retrolauncher.Main.getWindow;

/** Controller class for the Emulator Manager window. */
public class EmulatorManagerController implements IController {

  @FXML private TableView<Emulator> emulatorTableView;
  @FXML private TableColumn<Emulator, String> emulatorNameColumn;
  @FXML private TableColumn<Emulator, String> emulatorPathColumn;
  @FXML private TableColumn<Emulator, String> emulatorConsolesColumn;
  @FXML private Button addEmulatorButton;

  private ObservableList<Emulator> emulatorList;

  @FXML
  public void initialize() {
    // Makes the last column take up all remaining free space
    emulatorTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

    // Center column headers
    centerColumnHeaderText(emulatorNameColumn);
    centerColumnHeaderText(emulatorPathColumn);
    centerColumnHeaderText(emulatorConsolesColumn);

    // Initialize table columns
    emulatorNameColumn.setCellValueFactory(
        cellData -> new SimpleStringProperty(cellData.getValue().getName()));
    emulatorPathColumn.setCellValueFactory(
        cellData ->
            new SimpleStringProperty(cellData.getValue().getExecutablePath().getAbsolutePath()));
    emulatorConsolesColumn.setCellValueFactory(
        cellData ->
            new SimpleStringProperty(
                String.join(", ", cellData.getValue().getSupportedConsoles())));

    // Load emulator list from user profile
    emulatorList =
        FXCollections.observableArrayList(
            ConfigManager.getInstance().getCurrentUserProfile().getEmulators());
    emulatorTableView.setItems(emulatorList);

    // Add context menu to table rows
    emulatorTableView.setRowFactory(
        tv -> {
          TableRow<Emulator> row = new TableRow<>();
          row.setOnMouseClicked(
              event -> {
                if (!row.isEmpty() && event.getButton() == MouseButton.SECONDARY) {
                  ContextMenu contextMenu = new ContextMenu();

                  MenuItem editItem =
                      new MenuItem(LanguageManager.getResourceBundle().getString("button.edit"));
                  editItem.setOnAction(e -> editEmulator(row.getItem()));

                  MenuItem deleteItem =
                      new MenuItem(LanguageManager.getResourceBundle().getString("button.delete"));
                  deleteItem.setOnAction(e -> deleteEmulator(row.getItem()));

                  contextMenu.getItems().addAll(editItem, deleteItem);
                  contextMenu.show(row, event.getScreenX(), event.getScreenY());
                }
              });
          return row;
        });

    // Handle Add Emulator button click
    addEmulatorButton.setOnAction(event -> addEmulator());
  }

  /** Opens the Add Emulator window. */
  private void addEmulator() {
    try {
      var pair =
          getWindow(
              getClass(),
              "/view/add_emulator.fxml",
              LanguageManager.getResourceBundle().getString("button.add.emulator"));
      Stage stage = pair.getKey();
      stage.initModality(Modality.APPLICATION_MODAL);

      // Set fixed height
      var h = 232;
      stage.setMaxHeight(h);
      stage.setMinHeight(h);

      stage.showAndWait();

      // Reload window
      reloadWindow();
    } catch (IOException e) {
      LogManager.getLogger().severe(e.getMessage());
      e.printStackTrace();
    }
  }

  /** Opens the Edit Emulator window for the selected emulator. */
  private void editEmulator(Emulator emulator) {
    try {
      var pair =
          getWindow(
              getClass(),
              "/view/edit_emulator.fxml",
              LanguageManager.getResourceBundle().getString("button.edit.emulator"));
      Stage stage = pair.getKey();

      EditEmulatorController controller = (EditEmulatorController) pair.getValue();
      controller.setEmulator(emulator);
      stage.initModality(Modality.APPLICATION_MODAL);

      // Set fixed height
      var h = 280;
      stage.setMaxHeight(h);
      stage.setMinHeight(h);

      stage.showAndWait();

      // Reload window
      reloadWindow();

    } catch (IOException e) {
      LogManager.getLogger().severe(e.getMessage());
      e.printStackTrace();
    }
  }

  /** Deletes the selected emulator after confirmation. */
  private void deleteEmulator(Emulator emulator) {
    Alert alert =
        Main.getAlert(
            getClass(),
            Alert.AlertType.CONFIRMATION,
            LanguageManager.getResourceBundle().getString("alert.delete.emulator.title"),
            LanguageManager.getResourceBundle().getString("alert.delete.emulator.header"),
            LanguageManager.getResourceBundle().getString("alert.delete.emulator.content"));
    var result = alert.showAndWait();
    if (result.isPresent() && result.get() == ButtonType.OK) {
      ConfigManager.getInstance().getCurrentUserProfile().getEmulators().remove(emulator);
      ConfigManager.getInstance().saveConfig();

      // Reload window
      reloadWindow();
    }
  }

  private IController reloadWindow() {
    try {
      Stage oldStage = (Stage) getScene().getWindow();
      var newPair =
          getWindow(
              getClass(),
              "/view/emulator_manager.fxml",
              LanguageManager.getResourceBundle().getString("menu.manage.emulators"));
      Main.reloadWindow(oldStage, newPair.getKey());
      return newPair.getValue();
    } catch (IOException e) {
      LogManager.getLogger().severe(e.getMessage());
      e.printStackTrace();
    }
    return null;
  }

  @Override
  public ReadOnlyObjectProperty<Scene> getSceneProperty() {
    return emulatorTableView.sceneProperty();
  }

  @Override
  public Scene getScene() {
    return emulatorTableView.getScene();
  }
}
