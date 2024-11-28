module com.karandaev.retrolauncher {
  requires javafx.controls;
  requires javafx.fxml;
  requires java.logging;
  requires javafx.web;
  requires com.fasterxml.jackson.databind;
  requires jdk.jshell;
  requires atlantafx.base;
  requires java.desktop;
    requires com.formdev.flatlaf;

    opens com.karandaev.retrolauncher to
      javafx.fxml;

  exports com.karandaev.retrolauncher;
  exports com.karandaev.retrolauncher.controller;

  opens com.karandaev.retrolauncher.controller to
      javafx.fxml;
  opens com.karandaev.retrolauncher.utils to
      com.fasterxml.jackson.databind;
  opens com.karandaev.retrolauncher.model to
      com.fasterxml.jackson.databind;

  exports com.karandaev.retrolauncher.controller.interfaces;

  opens com.karandaev.retrolauncher.controller.interfaces to
      javafx.fxml;
}
