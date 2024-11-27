package com.karandaev.retrolauncher.utils;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/** Utility class for checking and handling updates. */
public class UpdateManager {
  // TODO: add updating sources
  /** Stores the latest version tag of the application. * */
  private static final String VERSION_URL = "https://yourserver.com/retrolauncher/version.txt";

  /** The location where stores the latest version. * */
  private static final String DOWNLOAD_URL = "https://yourserver.com/retrolauncher/download.jar";

  public static void checkForUpdates() {
    Task<Void> updateTask =
        new Task<>() {
          @Override
          protected Void call() throws Exception {
            String latestVersion = getLatestVersion();
            String currentVersion = ConfigManager.getInstance().getCurrentApplicationVersion();

            if (isNewVersionAvailable(currentVersion, latestVersion)) {
              Platform.runLater(
                  () -> {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Update Available");
                    alert.setHeaderText(null);
                    alert.setContentText(
                        "A new version ("
                            + latestVersion
                            + ") is available. Do you want to download it?");
                    alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
                    alert
                        .showAndWait()
                        .ifPresent(
                            response -> {
                              if (response == ButtonType.YES) {
                                // TODO: implement downloading and installing updates
                                // Open download link
                                // getHostServices().showDocument(DOWNLOAD_URL);
                              }
                            });
                  });
            }
            return null;
          }
        };
    new Thread(updateTask).start();
  }

  /** Returns tag of version * */
  private static String getLatestVersion() throws Exception {
    URL url = new URL(VERSION_URL);
    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    conn.setRequestMethod("GET");
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
      return reader.readLine();
    }
  }

  private static boolean isNewVersionAvailable(String currentVersion, String latestVersion) {
    return !currentVersion.equals(latestVersion);
  }
}
