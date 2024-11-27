package com.karandaev.retrolauncher.controller;

import com.karandaev.retrolauncher.Main;
import com.karandaev.retrolauncher.controller.interfaces.IController;
import com.karandaev.retrolauncher.model.RomFile;
import com.karandaev.retrolauncher.utils.ConfigManager;
import com.karandaev.retrolauncher.utils.LanguageManager;
import com.karandaev.retrolauncher.utils.LogManager;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.input.MouseButton;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.function.Supplier;

import static com.karandaev.retrolauncher.Main.centerColumnHeaderText;
import static com.karandaev.retrolauncher.Main.getWindow;

/** Controller class for the ROM Manager window. */
public class RomManagerController implements IController {

  @FXML private TableView<RomFile> romTableView;
  @FXML private TableColumn<RomFile, String> romNameColumn;
  @FXML private TableColumn<RomFile, String> romPathColumn;
  @FXML private TableColumn<RomFile, String> romConsolesColumn;
  @FXML private TableColumn<RomFile, String> romGenresColumn;
  @FXML private TableColumn<RomFile, Boolean> romFavoriteColumn;
  @FXML private Button addRomButton;

  private ObservableList<RomFile> romList;
  private static Supplier<IController> reloadMainWindow;

  @FXML
  public void initialize() {
    // Makes the last column take up all remaining free space
    romTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

    // Center column headers
    centerColumnHeaderText(romNameColumn);
    centerColumnHeaderText(romPathColumn);
    centerColumnHeaderText(romConsolesColumn);
    centerColumnHeaderText(romGenresColumn);
    centerColumnHeaderText(romFavoriteColumn);

    // Initialize table columns
    romNameColumn.setCellValueFactory(
        cellData -> new SimpleStringProperty(cellData.getValue().getName()));
    romPathColumn.setCellValueFactory(
        cellData -> new SimpleStringProperty(cellData.getValue().getFilePath().getAbsolutePath()));
    romConsolesColumn.setCellValueFactory(
        cellData -> new SimpleStringProperty(String.join(", ", cellData.getValue().getConsoles())));
    romGenresColumn.setCellValueFactory(
        cellData -> new SimpleStringProperty(String.join(", ", cellData.getValue().getGenres())));
    romFavoriteColumn.setCellValueFactory(
        cellData -> new SimpleBooleanProperty(cellData.getValue().isFavorite()));

    // Set cell factory for favorite column to display checkbox
    romFavoriteColumn.setCellFactory(tc -> new CheckBoxTableCell<>());

    // Load ROM list from user profile
    romList =
        FXCollections.observableArrayList(
            ConfigManager.getInstance().getCurrentUserProfile().getRoms());
    romTableView.setItems(romList);

    // Add context menu to table rows
    romTableView.setRowFactory(
        tv -> {
          TableRow<RomFile> row = new TableRow<>();
          row.setOnMouseClicked(
              event -> {
                if (!row.isEmpty() && event.getButton() == MouseButton.SECONDARY) {
                  ContextMenu contextMenu = new ContextMenu();

                  MenuItem editItem =
                      new MenuItem(LanguageManager.getResourceBundle().getString("button.edit"));
                  editItem.setOnAction(e -> editRom(row.getItem()));

                  MenuItem deleteItem =
                      new MenuItem(LanguageManager.getResourceBundle().getString("button.delete"));
                  deleteItem.setOnAction(e -> deleteRom(row.getItem()));

                  contextMenu.getItems().addAll(editItem, deleteItem);
                  contextMenu.show(row, event.getScreenX(), event.getScreenY());
                }
              });
          return row;
        });

    // Handle Add ROM button click
    addRomButton.setOnAction(event -> addRom());
  }

  /** Opens the Add ROM window. */
  private void addRom() {
    try {
      var pair =
          Main.getWindow(
              getClass(),
              "/view/add_rom.fxml",
              LanguageManager.getResourceBundle().getString("button.add.rom"));
      Stage stage = pair.getKey();
      stage.initModality(Modality.APPLICATION_MODAL);

      // Set fixed height
      var h = 310;
      stage.setMaxHeight(h);
      stage.setMinHeight(h);

      stage.showAndWait();

      // Reload UI
      reloadWindow();
      var mainController = (MainController) reloadMainWindow.get();
      reloadMainWindow = mainController::reloadWindow;
    } catch (IOException e) {
      LogManager.getLogger().severe(e.getMessage());
      e.printStackTrace();
    }
  }

  /** Opens the Edit ROM window for the selected ROM. */
  private void editRom(RomFile rom) {
    try {
      var pair =
          Main.getWindow(
              getClass(),
              "/view/edit_rom.fxml",
              LanguageManager.getResourceBundle().getString("button.edit.rom"));
      Stage stage = pair.getKey();

      EditRomController controller = (EditRomController) pair.getValue();
      controller.setRom(rom);
      stage.initModality(Modality.APPLICATION_MODAL);

      stage.showAndWait();

      // Reload UI
      reloadWindow();
      var mainController = (MainController) reloadMainWindow.get();
      reloadMainWindow = mainController::reloadWindow;

    } catch (IOException e) {
      LogManager.getLogger().severe(e.getMessage());
      e.printStackTrace();
    }
  }

  /** Deletes the selected ROM after confirmation. */
  private void deleteRom(RomFile rom) {
    Alert alert =
        Main.getAlert(
            getClass(),
            Alert.AlertType.CONFIRMATION,
            LanguageManager.getResourceBundle().getString("alert.delete.rom.title"),
            LanguageManager.getResourceBundle().getString("alert.delete.rom.header"),
            LanguageManager.getResourceBundle().getString("alert.delete.rom.content"));
    var result = alert.showAndWait();
    if (result.isPresent() && result.get() == ButtonType.OK) {
      ConfigManager.getInstance().getCurrentUserProfile().getRoms().remove(rom);
      ConfigManager.getInstance().saveConfig();

      // Reload UI
      reloadWindow();
      var mainController = (MainController) reloadMainWindow.get();
      reloadMainWindow = mainController::reloadWindow;
    }
  }

  private IController reloadWindow() {
    try {
      Stage oldStage = (Stage) getScene().getWindow();
      var newPair =
          getWindow(
              getClass(),
              "/view/rom_manager.fxml",
              LanguageManager.getResourceBundle().getString("menu.manage.roms"));
      Main.reloadWindow(oldStage, newPair.getKey());
      return newPair.getValue();
    } catch (IOException e) {
      LogManager.getLogger().severe(e.getMessage());
      e.printStackTrace();
    }
    return null;
  }

  public static void setReloadMainWindow(Supplier<IController> reloadMainWindow) {
    RomManagerController.reloadMainWindow = reloadMainWindow;
  }

  @Override
  public ReadOnlyObjectProperty<Scene> getSceneProperty() {
    return romTableView.sceneProperty();
  }

  @Override
  public Scene getScene() {
    return romTableView.getScene();
  }
}
