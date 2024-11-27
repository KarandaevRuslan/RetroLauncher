package com.karandaev.retrolauncher.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.karandaev.retrolauncher.model.UserProfile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/** Singleton class for managing configuration. */
public class ConfigManager {
  private static ConfigManager instance;
  private List<UserProfile> profiles;
  private Integer currentUserProfileIndex = 0;
  private String currentApplicationVersion;
  private final String configFilePath = "config.json";

  private ConfigManager() {
    profiles = new ArrayList<>();
    // TODO: version should detects automatically
    currentApplicationVersion = "1.0.0";
  }

  public static ConfigManager getInstance() {
    if (instance == null) {
      instance = new ConfigManager();
    }
    return instance;
  }

  /** Loads configuration from a JSON file. */
  public void loadConfig() {
    ObjectMapper mapper = new ObjectMapper();
    try {
      File configFile = new File(configFilePath);
      if (configFile.exists()) {
        // Deserialize configuration
        ConfigData configData = mapper.readValue(configFile, ConfigData.class);
        this.profiles = configData.getProfiles();
        this.currentUserProfileIndex = configData.getCurrentUserProfileIndex();
        this.currentApplicationVersion = configData.getCurrentApplicationVersion();
        LogManager.getLogger().info("Configuration loaded successfully.");
      } else {
        // Initialize with default data
        LogManager.getLogger().info("Configuration file not found.");
      }
    } catch (IOException e) {
      e.printStackTrace();
      LogManager.getLogger().severe("Failed to load configuration: " + e.getMessage());
    }
  }

  /** Saves configuration to a JSON file. */
  public void saveConfig() {
    ObjectMapper mapper = new ObjectMapper();
    try {
      ConfigData configData =
          new ConfigData(profiles, currentUserProfileIndex, currentApplicationVersion);
      mapper.writeValue(new File(configFilePath), configData);
      LogManager.getLogger().info("Configuration saved successfully.");
    } catch (IOException e) {
      e.printStackTrace();
      LogManager.getLogger().severe("Failed to save configuration: " + e.getMessage());
    }
  }

  public List<UserProfile> getProfiles() {
    return profiles;
  }

  public Integer getCurrentUserProfileIndex() {
    return currentUserProfileIndex;
  }

  public void setCurrentUserProfileIndex(Integer currentUserProfileIndex) {
    this.currentUserProfileIndex = currentUserProfileIndex;
  }

  public UserProfile getCurrentUserProfile() {
    if (currentUserProfileIndex >= 0 && currentUserProfileIndex < profiles.size()) {
      return profiles.get(currentUserProfileIndex);
    } else {
      return null;
    }
  }

  public String getConfigFilePath() {
    return configFilePath;
  }

  public String getCurrentApplicationVersion() {
    return currentApplicationVersion;
  }

  // Inner class to hold configuration data for serialization
  private static class ConfigData {
    private List<UserProfile> profiles;
    private Integer currentUserProfileIndex;
    private String currentApplicationVersion;

    public ConfigData() {
      // Default constructor
    }

    public ConfigData(
        List<UserProfile> profiles,
        Integer currentUserProfileIndex,
        String currentApplicationVersion) {
      this.profiles = profiles;
      this.currentUserProfileIndex = currentUserProfileIndex;
      this.currentApplicationVersion = currentApplicationVersion;
    }

    public String getCurrentApplicationVersion() {
      return currentApplicationVersion;
    }

    public Integer getCurrentUserProfileIndex() {
      return currentUserProfileIndex;
    }

    public List<UserProfile> getProfiles() {
      return profiles;
    }
  }
}
