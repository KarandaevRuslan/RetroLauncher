package com.karandaev.retrolauncher.controller;

import com.karandaev.retrolauncher.Main;
import com.karandaev.retrolauncher.controller.interfaces.IController;
import com.karandaev.retrolauncher.model.Emulator;
import com.karandaev.retrolauncher.model.RomFile;
import com.karandaev.retrolauncher.utils.ConfigManager;
import com.karandaev.retrolauncher.utils.LanguageManager;
import com.karandaev.retrolauncher.utils.LogManager;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.input.MouseButton;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static com.karandaev.retrolauncher.Main.*;

/** Controller class for the main application window. */
public class MainController implements IController {
  @FXML private TableView<RomFile> romTableView;
  @FXML private TableColumn<RomFile, String> romNameColumn;
  @FXML private TableColumn<RomFile, String> romConsolesColumn;
  @FXML public TableColumn<RomFile, String> romGenresColumn;
  @FXML private TableColumn<RomFile, Boolean> romFavoriteColumn;

  @FXML private TextField searchField;
  @FXML private ComboBox<String> filterComboBox;

  private ObservableList<RomFile> romList = FXCollections.observableArrayList();
  private FilteredList<RomFile> filteredData;

  @FXML
  public void initialize() {
    // Makes the last column take up all remaining free space
    romTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

    // Center column headers
    centerColumnHeaderText(romNameColumn);
    centerColumnHeaderText(romConsolesColumn);
    centerColumnHeaderText(romGenresColumn);
    centerColumnHeaderText(romFavoriteColumn);

    // Sets value factories
    romNameColumn.setCellValueFactory(
        cellData -> new SimpleStringProperty(cellData.getValue().getName()));
    romConsolesColumn.setCellValueFactory(
        cellData -> new SimpleStringProperty(String.join(", ", cellData.getValue().getConsoles())));
    romGenresColumn.setCellValueFactory(
        cellData -> new SimpleStringProperty(String.join(", ", cellData.getValue().getGenres())));
    romFavoriteColumn.setCellValueFactory(
        cellData -> new SimpleBooleanProperty(cellData.getValue().isFavorite()));
    romFavoriteColumn.setCellFactory(tc -> new CheckBoxTableCell<>());

    // Add double-click event handler to table rows
    romTableView.setRowFactory(
        tv -> {
          TableRow<RomFile> row = new TableRow<>();
          row.setOnMouseClicked(
              event -> {
                if (!row.isEmpty()) {
                  RomFile clickedRom = row.getItem();
                  if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                    openEmulatorSelectionWindow(clickedRom);
                  } else if (event.getButton() == MouseButton.SECONDARY) {
                    // Show context menu on right-click
                    ContextMenu contextMenu = new ContextMenu();

                    MenuItem launchItem =
                        new MenuItem(
                            LanguageManager.getResourceBundle().getString("button.launch"));
                    launchItem.setOnAction(e -> openEmulatorSelectionWindow(clickedRom));
                    contextMenu.getItems().add(launchItem);

                    String favoriteKey =
                        clickedRom.isFavorite() ? "button.remove.favorite" : "button.add.favorite";
                    MenuItem favoriteItem =
                        new MenuItem(LanguageManager.getResourceBundle().getString(favoriteKey));
                    favoriteItem.setOnAction(e -> toggleFavorite(clickedRom));
                    contextMenu.getItems().add(favoriteItem);

                    contextMenu.show(row, event.getScreenX(), event.getScreenY());
                  }
                }
              });
          return row;
        });

    var langBundle = LanguageManager.getResourceBundle();

    // Initialize filter options
    filterComboBox
        .getItems()
        .addAll(
            langBundle.getString("filter.all.rom"),
            langBundle.getString("filter.favorites"),
            langBundle.getString("filter.console"),
            langBundle.getString("filter.genre"));
    filterComboBox.setValue(langBundle.getString("filter.all.rom"));

    // Load ROM files asynchronously
    loadRomFiles();

    // Add listeners
    searchField.textProperty().addListener((observable, oldValue, newValue) -> applyFilters());
    filterComboBox.valueProperty().addListener((observable, oldValue, newValue) -> applyFilters());

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
                            stage.setOnCloseRequest(event -> Platform.exit());
                          }
                        });
              }
            });
  }

  /** Loads ROM files into the table view asynchronously. */
  private void loadRomFiles() {
    Task<Void> loadTask =
        new Task<>() {
          @Override
          protected Void call() {
            romList.setAll(ConfigManager.getInstance().getCurrentUserProfile().getRoms());
            return null;
          }

          @Override
          protected void succeeded() {
            filteredData = new FilteredList<>(romList, p -> true);
            applyFilters();
          }
        };

    new Thread(loadTask).start();
  }

  /** Applies search and filter criteria to the ROM list. */
  private void applyFilters() {
    String searchText = searchField.getText().toLowerCase();
    String filterOption = filterComboBox.getValue();

    filteredData.setPredicate(
        rom -> {
          boolean matchesSearch =
              rom.getName().toLowerCase().contains(searchText)
                  || rom.getConsoles().stream().anyMatch(x -> x.toLowerCase().contains(searchText))
                  || rom.getGenres().stream().anyMatch(x -> x.toLowerCase().contains(searchText));
          if (!matchesSearch) {
            return false;
          }
          var langBundle = LanguageManager.getResourceBundle();
          if (langBundle.getString("filter.favorites").equals(filterOption)) {
            return rom.isFavorite();
          }
          if (langBundle.getString("filter.console").equals(filterOption)) {
            return rom.getConsoles().stream().anyMatch(x -> x.toLowerCase().contains(searchText));
          }
          if (langBundle.getString("filter.genre").equals(filterOption)) {
            return rom.getGenres().stream().anyMatch(x -> x.toLowerCase().contains(searchText));
          }
          return true;
        });

    romTableView.setItems(filteredData);
  }

  @FXML
  private void openProfileSettings() {
    try {
      var pair =
          Main.getWindow(
              getClass(),
              "/view/profile_settings.fxml",
              LanguageManager.getResourceBundle().getString("menu.profile.settings"));
      Stage settingsStage = pair.getKey();
      settingsStage.initModality(Modality.APPLICATION_MODAL);
      settingsStage.setResizable(false);
      ProfileSettingsController.setReloadMainWindow(this::reloadWindow);
      settingsStage.showAndWait();

    } catch (IOException e) {
      e.printStackTrace();
      LogManager.getLogger().severe("Failed to open settings: " + e.getMessage());
    }
  }

  @FXML
  public void openProfileManager() {
    try {
      var pair =
          Main.getWindow(
              getClass(),
              "/view/profile_manager.fxml",
              LanguageManager.getResourceBundle().getString("menu.profile.manager"));
      Stage profileManagerStage = pair.getKey();
      profileManagerStage.initModality(Modality.APPLICATION_MODAL);
      ProfileManagerController.setReloadMainWindow(this::reloadWindow);
      profileManagerStage.showAndWait();
    } catch (IOException e) {
      e.printStackTrace();
      LogManager.getLogger().severe("Failed to open settings: " + e.getMessage());
    }
  }

  @FXML
  private void manageEmulators() {
    try {
      Stage emulatorStage =
          Main.getWindow(
                  getClass(),
                  "/view/emulator_manager.fxml",
                  LanguageManager.getResourceBundle().getString("menu.manage.emulators"))
              .getKey();
      emulatorStage.initModality(Modality.APPLICATION_MODAL);
      emulatorStage.showAndWait();

      // Reload window
      reloadWindow();

    } catch (IOException e) {
      e.printStackTrace();
      LogManager.getLogger().severe("Failed to open emulator management: " + e.getMessage());
    }
  }

  @FXML
  private void manageRoms() {
    try {
      Stage romStage =
          Main.getWindow(
                  getClass(),
                  "/view/rom_manager.fxml",
                  LanguageManager.getResourceBundle().getString("menu.manage.roms"))
              .getKey();
      romStage.initModality(Modality.APPLICATION_MODAL);
      RomManagerController.setReloadMainWindow(this::reloadWindow);
      romStage.showAndWait();

    } catch (IOException e) {
      e.printStackTrace();
      LogManager.getLogger().severe("Failed to open ROM management: " + e.getMessage());
    }
  }

  /** Opens the emulator selection window for the selected ROM. */
  private void openEmulatorSelectionWindow(RomFile selectedRom) {
    try {
      List<Emulator> compatibleEmulators =
          ConfigManager.getInstance().getCurrentUserProfile().getEmulators().stream()
              .filter(
                  emulator ->
                      !Collections.disjoint(
                          emulator.getSupportedConsoles().stream()
                              .map(String::toLowerCase)
                              .toList(),
                          selectedRom.getConsoles().stream().map(String::toLowerCase).toList()))
              .toList();

      if (compatibleEmulators.isEmpty()) {
        Alert alert =
            Main.getAlert(
                getClass(),
                Alert.AlertType.WARNING,
                LanguageManager.getResourceBundle().getString("alert.no.emulator.title"),
                null,
                LanguageManager.getResourceBundle().getString("alert.no.emulator.content"));
        alert.showAndWait();
        return;
      }

      var pair =
          Main.getWindow(
              getClass(),
              "/view/emulator_selection.fxml",
              LanguageManager.getResourceBundle().getString("window.emulator.selection"));
      Stage stage = pair.getKey();
      EmulatorSelectionController controller = (EmulatorSelectionController) pair.getValue();
      controller.setSelectedRomAndCompatibleEmulators(selectedRom, compatibleEmulators);
      stage.initModality(Modality.APPLICATION_MODAL);
      stage.setResizable(false);
      stage.showAndWait();

    } catch (IOException e) {
      e.printStackTrace();
      LogManager.getLogger().severe("Failed to open emulator selection window: " + e.getMessage());
    }
  }

  @FXML
  private void exitApplication() {
    Platform.exit();
  }

  public IController reloadWindow() {
    try {
      Stage oldStage = (Stage) getScene().getWindow();
      var newPair = getWindow(getClass(), "/view/main.fxml", PROGRAM_NAME);
      Main.reloadWindow(oldStage, newPair.getKey());
      return newPair.getValue();
    } catch (IOException e) {
      LogManager.getLogger().severe(e.getMessage());
      e.printStackTrace();
      return null;
    }
  }

  /** Toggles the favorite status of a ROM and updates the config and UI. */
  private void toggleFavorite(RomFile rom) {
    rom.setFavorite(!rom.isFavorite());
    ConfigManager.getInstance().saveConfig();
    applyFilters();
  }

  @FXML
  private void showHelpContents() throws Exception {
    try {
      Stage helpStage =
          getWindow(
                  getClass(),
                  "/view/help.fxml",
                  LanguageManager.getResourceBundle().getString("menu.help.contents"))
              .getKey();
      helpStage.initModality(Modality.APPLICATION_MODAL);
      helpStage.showAndWait();

    } catch (IOException e) {
      e.printStackTrace();
      LogManager.getLogger().severe("Failed to open help contents: " + e.getMessage());
    }
  }

  @FXML
  private void showAbout() {
    Alert alert =
        getAlert(
            getClass(),
            Alert.AlertType.INFORMATION,
            LanguageManager.getResourceBundle().getString("menu.help.about"),
            PROGRAM_NAME + " v" + ConfigManager.getInstance().getCurrentApplicationVersion(),
            LanguageManager.getResourceBundle().getString("alert.about.content"));
    alert.showAndWait();
  }

  @Override
  public ReadOnlyObjectProperty<Scene> getSceneProperty() {
    return filterComboBox.sceneProperty();
  }

  @Override
  public Scene getScene() {
    return filterComboBox.getScene();
  }
}
