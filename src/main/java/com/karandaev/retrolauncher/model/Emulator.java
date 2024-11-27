package com.karandaev.retrolauncher.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import java.io.File;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/** Model class representing an emulator. */
public class Emulator {
  private Integer id;
  private String name;
  private File executablePath;
  private List<String> supportedConsoles;
  private LaunchParameters launchParameters;

  public Emulator() {
    // Default constructor for JSON deserialization
  }

  public Emulator(String name, File executablePath, List<String> supportedConsoles) {
    this.name = name;
    this.executablePath = executablePath;
    this.supportedConsoles = supportedConsoles;
    this.launchParameters = new LaunchParameters();
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public File getExecutablePath() {
    return executablePath;
  }

  public void setExecutablePath(File executablePath) {
    this.executablePath = executablePath;
  }

  public LaunchParameters getLaunchParameters() {
    return launchParameters;
  }

  public void setLaunchParameters(LaunchParameters launchParameters) {
    this.launchParameters = launchParameters;
  }

  public List<String> getSupportedConsoles() {
    return supportedConsoles;
  }

  public Integer getId() {
    return id;
  }

  public void setUniqueId(List<Emulator> emulators) {
    var res = emulators.stream().max(Comparator.comparing(Emulator::getId));
    id = res.map(emulator -> emulator.getId() + 1).orElse(0);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Emulator emulator = (Emulator) o;
    return Objects.equals(id, emulator.id)
        && Objects.equals(name, emulator.name)
        && Objects.equals(executablePath, emulator.executablePath)
        && Objects.equals(supportedConsoles, emulator.supportedConsoles)
        && Objects.equals(launchParameters, emulator.launchParameters);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, executablePath, supportedConsoles, launchParameters);
  }
}
