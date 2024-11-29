package com.karandaev.retrolauncher.utils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.*;
import java.util.List;
import java.util.Optional;
import java.io.IOException;

import static com.karandaev.retrolauncher.Main.getAlert;

/** Utility class for checking and handling updates. */
public class UpdateManager {
  /** The location where stores the latest version. * */
  private static final String GITHUB_API_URL =
      "https://api.github.com/repos/KarandaevRuslan/RetroLauncher/releases/latest";

  private static final String VERSION_PATH = "VERSION";
  public static final String VERSION_UNKNOWN = "unknown";

  private static ObjectMapper objectMapper;

  static {
    objectMapper = new ObjectMapper();
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
  }

  public static String getVersion() {
    Path versionFile = Paths.get(VERSION_PATH);
    try {
      if (Files.exists(versionFile)) {
        String version =
            Files.lines(versionFile)
                .map(String::trim)
                .filter(x -> x.matches("^v(\\d+\\.)+\\d+$"))
                .findFirst()
                .orElse(VERSION_UNKNOWN);
        return version;
      }
    } catch (IOException e) {
      e.printStackTrace();
      LogManager.getLogger().severe(e.getMessage());
    }
    return VERSION_UNKNOWN;
  }

  public static void tryUpdate(Class<?> clazz) {
    Optional<String> downloadUrlOpt = getDownloadUrl();
    if (!isUpdateAvailable() || downloadUrlOpt.isEmpty()) {
      Alert alert =
          getAlert(
              clazz,
              Alert.AlertType.INFORMATION,
              LanguageManager.getResourceBundle().getString("menu.application.update"),
              null,
              LanguageManager.getResourceBundle().getString("alert.no.application.update.content")
                  + " "
                  + getVersion());
      alert.showAndWait();
      LogManager.getLogger().warning("Is download URL empty? " + downloadUrlOpt.isEmpty());
      return;
    }
    String downloadUrl = downloadUrlOpt.get();

    Platform.runLater(
        () -> {
          Alert alert =
              getAlert(
                  clazz,
                  Alert.AlertType.INFORMATION,
                  LanguageManager.getResourceBundle().getString("menu.application.update"),
                  null,
                  LanguageManager.getResourceBundle()
                      .getString("alert.update.application.content"));
          alert.getButtonTypes().clear();
          alert.getButtonTypes().addAll(ButtonType.YES, ButtonType.NO);
          Optional<ButtonType> result = alert.showAndWait();
          if (result.isPresent() && result.get() == ButtonType.YES) {
            try {
              downloadAndUpdate(downloadUrl);
            } catch (IOException e) {
              LogManager.getLogger().severe(e.getMessage());
              e.printStackTrace();
            }
          }
        });
  }

  private static boolean isUpdateAvailable() {
    Release latestRelease = null;
    try {
      latestRelease = getLatestRelease();
    } catch (IOException e) {
      return false;
    }
    String latestVersion = latestRelease.getTagName();
    if (getVersion().equals(VERSION_UNKNOWN)) {
      var msg = "Version is unknown. Please set version manually in VERSION file";
      LogManager.getLogger().severe(msg);
      System.err.println(msg);
      return true;
    }
    return isVersionNewer(latestVersion.trim(), getVersion().trim());
  }

  private static Optional<String> getDownloadUrl() {
    try {
      Release latestRelease = getLatestRelease();
      String os = System.getProperty("os.name").toLowerCase();
      LogManager.getLogger().info("Your os is " + os);
      if (os.contains("win")) {
        return latestRelease.getAssets().stream()
            .filter(asset -> asset.getBrowserDownloadUrl().contains("windows"))
            .findFirst()
            .map(Asset::getBrowserDownloadUrl);
      } else if (os.contains("mac")) {
        return latestRelease.getAssets().stream()
            .filter(asset -> asset.getBrowserDownloadUrl().contains("macos"))
            .findFirst()
            .map(Asset::getBrowserDownloadUrl);
      } else if (os.contains("nix") || os.contains("nux") || os.contains("aix")) {
        return latestRelease.getAssets().stream()
            .filter(asset -> asset.getBrowserDownloadUrl().contains("ubuntu"))
            .findFirst()
            .map(Asset::getBrowserDownloadUrl);
      }

    } catch (IOException e) {
      e.printStackTrace();
      LogManager.getLogger().severe(e.getClass() + " " + e.getMessage());
    }

    return Optional.empty();
  }

  private static Release getLatestRelease() throws IOException {
    URL url = new URL(GITHUB_API_URL);
    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    conn.setRequestMethod("GET");
    conn.setRequestProperty("Accept", "application/vnd.github.v3+json");
    conn.setRequestProperty("User-Agent", "Java Update Checker");

    int responseCode = conn.getResponseCode();
    if (responseCode != HttpURLConnection.HTTP_OK) {
      throw new IOException("Failed to fetch latest release: HTTP error code : " + responseCode);
    }

    try (InputStream is = new BufferedInputStream(conn.getInputStream())) {
      Release release = objectMapper.readValue(is, Release.class);
      return release;
    } finally {
      conn.disconnect();
    }
  }

  private static boolean isVersionNewer(String latestVersion, String currentVersion) {
    String latest = latestVersion.startsWith("v") ? latestVersion.substring(1) : latestVersion;
    String current = currentVersion.startsWith("v") ? currentVersion.substring(1) : currentVersion;

    String[] latestParts = latest.split("\\.");
    String[] currentParts = current.split("\\.");

    int length = Math.max(latestParts.length, currentParts.length);
    for (int i = 0; i < length; i++) {
      int latestPart = i < latestParts.length ? Integer.parseInt(latestParts[i]) : 0;
      int currentPart = i < currentParts.length ? Integer.parseInt(currentParts[i]) : 0;
      if (latestPart > currentPart) {
        return true;
      } else if (latestPart < currentPart) {
        return false;
      }
    }
    return false;
  }

  private static void downloadAndUpdate(String downloadUrl) throws IOException {
    URL url = new URL(downloadUrl);
    Path tempFile = Files.createTempFile("update", ".zip");
    try (InputStream in = url.openStream()) {
      Files.copy(in, tempFile, StandardCopyOption.REPLACE_EXISTING);
    }

    LogManager.getLogger().info("Successfully downloaded update.");

    Path updateDir = Files.createTempDirectory("update_unpacked");
    unzip(tempFile, updateDir);

    LogManager.getLogger().info("Successfully unpacked update.");

    launchUpdater(updateDir);
    Platform.exit();
    System.exit(0);
  }

  private static void unzip(Path zipFilePath, Path destDir) throws IOException {
    LogManager.getLogger()
        .info("zipFilePath=" + zipFilePath.toString() + "\ndestDir=" + destDir.toString());
    try (var fs = FileSystems.newFileSystem(zipFilePath, (ClassLoader) null)) {
      for (Path root : fs.getRootDirectories()) {
        Files.walk(root)
            .forEach(
                source -> {
                  try {
                    Path destPath = destDir.resolve(root.relativize(source).toString());
                    if (Files.isDirectory(source)) {
                      if (!Files.exists(destPath)) {
                        Files.createDirectory(destPath);
                        LogManager.getLogger().info("Created directory " + destPath);
                      }
                    } else {
                      Files.copy(source, destPath, StandardCopyOption.REPLACE_EXISTING);
                      LogManager.getLogger()
                          .info("Copied files from " + source + " to " + destPath);
                    }
                  } catch (IOException e) {
                    e.printStackTrace();
                    LogManager.getLogger().severe(e.getClass() + " " + e.getMessage());
                  }
                });
      }
    }
  }

  private static void launchUpdater(Path updateDir) {
    try {
      Path updaterJar = Path.of("Updater.jar");
      Path javaLocation = Path.of("bin", "java");

      if (Files.exists(updaterJar)) {
        new ProcessBuilder(
                javaLocation.toString(),
                "-jar",
                updaterJar.toString(),
                updateDir.toString(),
                new File("").getAbsolutePath())
            .start();
      } else {
        var msg = "Updater not found!";
        LogManager.getLogger().severe(msg);
        System.err.println(msg);
      }
    } catch (IOException e) {
      LogManager.getLogger().severe(e.getMessage());
      e.printStackTrace();
    }
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  private static class Release {
    @JsonProperty("tag_name")
    private String tagName;

    private List<Asset> assets;

    public Release() {}

    public String getTagName() {
      return tagName;
    }

    public void setTagName(String tagName) {
      this.tagName = tagName;
    }

    public List<Asset> getAssets() {
      return assets;
    }

    public void setAssets(List<Asset> assets) {
      this.assets = assets;
    }
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  private static class Asset {
    @JsonProperty("browser_download_url")
    private String browserDownloadUrl;

    public Asset() {}

    public String getBrowserDownloadUrl() {
      return browserDownloadUrl;
    }

    public void setBrowserDownloadUrl(String browserDownloadUrl) {
      this.browserDownloadUrl = browserDownloadUrl;
    }
  }
}
