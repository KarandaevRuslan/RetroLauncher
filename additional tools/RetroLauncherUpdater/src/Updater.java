import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

public class Updater {
  public static void copyFolder(Path sourceFolder, Path destinationFolder) throws IOException {
    // Check if sourceFolder exists and is a directory
    if (!Files.exists(sourceFolder) || !Files.isDirectory(sourceFolder)) {
      throw new IOException("Source folder does not exist or is not a directory: " + sourceFolder);
    }

    // The target folder where we need to copy the source folder
    Path targetFolder = destinationFolder.resolve(sourceFolder.getFileName());

    // Walk the file tree and copy each file/directory
    Files.walkFileTree(
        sourceFolder,
        new SimpleFileVisitor<Path>() {
          @Override
          public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
              throws IOException {
            // Compute the path in the target folder
            Path targetDir = targetFolder.resolve(sourceFolder.relativize(dir));
            // Create the directory if it doesn't exist
            if (!Files.exists(targetDir)) {
              Files.createDirectories(targetDir);
            }
            return FileVisitResult.CONTINUE;
          }

          @Override
          public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
              throws IOException {
            // Compute the path in the target folder
            Path targetFile = targetFolder.resolve(sourceFolder.relativize(file));
            // Copy the file, replacing existing ones
            Files.copy(file, targetFile, StandardCopyOption.REPLACE_EXISTING);
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

    try {
      copyFolder(updateDir, appDir);

      Path mainApp = appDir.resolve("RetroLauncher");
      new ProcessBuilder(mainApp.toString()).start();

      System.exit(0);
    } catch (IOException e) {
      e.printStackTrace();
      System.exit(1);
    }
  }
}
