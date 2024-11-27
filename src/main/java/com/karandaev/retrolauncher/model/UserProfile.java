package com.karandaev.retrolauncher.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.karandaev.retrolauncher.utils.LanguageManager;
import com.karandaev.retrolauncher.utils.LogManager;
import jdk.jshell.spi.ExecutionControl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/** Model class representing a user profile. */
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserProfile {
  private String username;
  private String preferredLanguageShort;
  private String preferredThemeEn;
  private List<Emulator> emulators;
  private List<RomFile> roms;

  public UserProfile() {
    // Default constructor for JSON deserialization
  }

  public UserProfile(String username) {
    this.username = username;
    this.preferredLanguageShort = "en";
    this.preferredThemeEn = "light";
    this.emulators = new ArrayList<>();
    this.roms = new ArrayList<>();
  }

  public UserProfile(UserProfile other) {
    ObjectMapper mapper = new ObjectMapper();
    try {
      String json = mapper.writeValueAsString(other);
      UserProfile copy = mapper.readValue(json, UserProfile.class);

      this.username = copy.username;
      this.preferredLanguageShort = copy.preferredLanguageShort;
      this.preferredThemeEn = copy.preferredThemeEn;
      this.emulators = copy.emulators;
      this.roms = copy.roms;
    } catch (IOException e) {
      e.printStackTrace();
      LogManager.getLogger().severe(e.getMessage());
    }
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPreferredLanguage() {
    switch (preferredLanguageShort) {
      case "en":
        return LanguageManager.getResourceBundle().getString("language.english");
      case "ru":
        return LanguageManager.getResourceBundle().getString("language.russian");
      default:
        try {
          throw new ExecutionControl.NotImplementedException(
              "Unknown language: " + preferredLanguageShort);
        } catch (ExecutionControl.NotImplementedException e) {
          LogManager.getLogger().severe(e.getMessage());
          throw new RuntimeException(e);
        }
    }
  }

  public String getPreferredTheme() {
    switch (preferredThemeEn) {
      case "light":
        return LanguageManager.getResourceBundle().getString("theme.light");
      case "dark":
        return LanguageManager.getResourceBundle().getString("theme.dark");
      default:
        try {
          throw new ExecutionControl.NotImplementedException("Unknown theme: " + preferredThemeEn);
        } catch (ExecutionControl.NotImplementedException e) {
          LogManager.getLogger().severe(e.getMessage());
          throw new RuntimeException(e);
        }
    }
  }

  public String getPreferredThemeEn() {
    return preferredThemeEn;
  }

  public void setPreferredThemeEn(String preferredThemeEn) {
    this.preferredThemeEn = preferredThemeEn;
  }

  public String getPreferredLanguageShort() {
    return preferredLanguageShort;
  }

  public void setPreferredLanguageShort(String preferredLanguageShort) {
    this.preferredLanguageShort = preferredLanguageShort;
  }

  public List<Emulator> getEmulators() {
    return emulators;
  }

  public List<RomFile> getRoms() {
    return roms;
  }
}
