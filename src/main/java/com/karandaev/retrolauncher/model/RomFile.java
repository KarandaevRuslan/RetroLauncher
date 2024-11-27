package com.karandaev.retrolauncher.model;

import java.io.File;
import java.util.List;
import java.util.Map;

/** Model class representing a ROM file. */
public class RomFile {
  private String name;
  private File filePath;
  private List<String> consoles; // Supports multiple consoles
  private List<String> genres; // Supports multiple genres
  private Boolean favorite;
  private Map<Integer, LaunchParameters>
      launchParameters; // Dictionary <Emulator ID, LaunchParameters>

  /** Default constructor for JSON deserialization. */
  public RomFile() {
    // Empty constructor
  }

  /**
   * Parameterized constructor.
   *
   * @param name Name of the ROM file
   * @param filePath Path to the ROM file
   * @param consoles List of supported consoles
   * @param genres List of game genres
   * @param favorite Favorite flag
   * @param launchParameters Dictionary mapping emulators ID to their launch parameters
   */
  public RomFile(
      String name,
      File filePath,
      List<String> consoles,
      List<String> genres,
      Boolean favorite,
      Map<Integer, LaunchParameters> launchParameters) {
    this.name = name;
    this.filePath = filePath;
    this.consoles = consoles;
    this.genres = genres;
    this.favorite = favorite;
    this.launchParameters = launchParameters;
  }

  // Getters and Setters

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public File getFilePath() {
    return filePath;
  }

  public void setFilePath(File filePath) {
    this.filePath = filePath;
  }

  public List<String> getConsoles() {
    return consoles;
  }

  public List<String> getGenres() {
    return genres;
  }

  public void setGenres(List<String> genres) {
    this.genres = genres;
  }

  public Boolean isFavorite() {
    return favorite;
  }

  public void setFavorite(Boolean favorite) {
    this.favorite = favorite;
  }

  public Map<Integer, LaunchParameters> getLaunchParameters() {
    return launchParameters;
  }

  public void setLaunchParameters(Map<Integer, LaunchParameters> launchParameters) {
    this.launchParameters = launchParameters;
  }
}
