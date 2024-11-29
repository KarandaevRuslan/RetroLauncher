package com.karandaev.retrolauncher.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

public class FileManager {
  public static void cleanUp() {
    Path workingDir = new File("").toPath();
    Path updateFile = workingDir.resolve("update.zip");
    Path updateDir = workingDir.resolve("update_unpacked");
    Path updaterFile = workingDir.resolve("Updater.jar");

    try {
      if (Files.exists(updaterFile)) {
        Files.delete(updaterFile);
        LogManager.getLogger().info("Deleted updater file: " + updaterFile);
      }
      if (Files.exists(updateFile)) {
        Files.delete(updateFile);
        LogManager.getLogger().info("Deleted update file: " + updateFile);
      }
      if (Files.exists(updateDir)) {
        deleteDirectoryRecursively(updateDir);
        LogManager.getLogger().info("Deleted update directory: " + updateDir);
      }
    } catch (IOException e) {
      LogManager.getLogger().severe("Error during cleanup: " + e.getMessage());
    }
  }

  public static void deleteDirectoryRecursively(Path path) throws IOException {
    if (Files.notExists(path)) {
      return;
    }
    Files.walkFileTree(
        path,
        new SimpleFileVisitor<>() {
          @Override
          public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
              throws IOException {
            Files.delete(file);
            return FileVisitResult.CONTINUE;
          }

          @Override
          public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
            Files.delete(dir);
            return FileVisitResult.CONTINUE;
          }
        });
  }
}
