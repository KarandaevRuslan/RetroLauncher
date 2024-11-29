import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

public class Updater {
  public static void copyFolderContents(Path sourceFolder, Path destinationFolder)
      throws IOException {
    // Check if sourceFolder exists and is a directory
    if (!Files.exists(sourceFolder) || !Files.isDirectory(sourceFolder)) {
      throw new IOException("Source folder does not exist or is not a directory: " + sourceFolder);
    }

    // Ensure destination folder exists
    if (!Files.exists(destinationFolder)) {
      Files.createDirectories(destinationFolder);
    }

    // Walk the file tree and copy each file/directory
    Files.walkFileTree(
        sourceFolder,
        new SimpleFileVisitor<>() {
          @Override
          public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
              throws IOException {
            // Compute the path in the destination folder
            Path targetDir = destinationFolder.resolve(sourceFolder.relativize(dir));
            // Create the directory if it doesn't exist
            if (!Files.exists(targetDir)) {
              Files.createDirectories(targetDir);
            }
            return FileVisitResult.CONTINUE;
          }

          @Override
          public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
            // Compute the path in the destination folder
            Path targetFile = destinationFolder.resolve(sourceFolder.relativize(file));
            try {
              // Copy the file, replacing existing ones
              Files.copy(file, targetFile, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
              // If the file is in use or cannot be copied, log and skip it
              LogManager.getLogger()
                  .severe("Failed to copy file: " + file + " (" + e.getMessage() + ")");
              // Continue with the next file
            }
            return FileVisitResult.CONTINUE;
          }
        });
  }

  public static void main(String[] args) {
    if (args.length < 2) {
      System.err.println("Using: java -jar updater.jar <update_dir> <app_dir>");
      System.exit(1);
    }

    Path updateDir = Paths.get(args[0]);
    Path appDir = Paths.get(args[1]);
    LogManager.getLogger().info("updateDir=" + updateDir + "\tappDir=" + appDir);

    try {
      copyFolderContents(updateDir, appDir);

      String os = System.getProperty("os.name").toLowerCase();
      Path mainApp = null;
      LogManager.getLogger().info("Your os is " + os);
      if (os.contains("win")) {
        mainApp = appDir.resolve("RetroLauncher.bat");
        new ProcessBuilder(mainApp.toString()).start();
      } else if (os.contains("mac")
          || os.contains("nix")
          || os.contains("nux")
          || os.contains("aix")) {
        mainApp = appDir.resolve("RetroLauncher.sh");
        new ProcessBuilder("bash", mainApp.toString()).start();
      } else {
        LogManager.getLogger().severe("Unknown OS");
        System.exit(1);
      }
      System.exit(0);
    } catch (IOException e) {
      e.printStackTrace();
      LogManager.getLogger().severe(e.getClass() + " " + e.getMessage());
      System.exit(1);
    }
  }
}
