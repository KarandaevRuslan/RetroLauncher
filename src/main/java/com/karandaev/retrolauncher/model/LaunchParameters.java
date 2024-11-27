package com.karandaev.retrolauncher.model;

/** Model class representing settings for an emulator. */
public class LaunchParameters {
  private String launchParameters;

  public LaunchParameters() {
    // Default constructor
  }

  public String getLaunchParameters() {
    return launchParameters;
  }

  public void setLaunchParameters(String launchParameters) {
    this.launchParameters = launchParameters;
  }
}
