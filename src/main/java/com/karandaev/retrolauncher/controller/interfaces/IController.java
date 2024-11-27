package com.karandaev.retrolauncher.controller.interfaces;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.scene.Scene;

public interface IController {
    ReadOnlyObjectProperty<Scene> getSceneProperty();
    Scene getScene();
}
