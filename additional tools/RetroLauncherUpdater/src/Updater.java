import java.io.IOException;
import java.nio.file.*;

public class Updater {
  public static void main(String[] args) {
    if (args.length < 2) {
      System.err.println("Using: java -jar updater.jar <update_dir> <app_dir>");
      System.exit(1);
    }

    Path updateDir = Paths.get(args[0]);
    Path appDir = Paths.get(args[1]);

    try {
      Files.walk(updateDir)
          .forEach(
              source -> {
                try {
                  Path destination = appDir.resolve(updateDir.relativize(source).toString());
                  if (Files.isDirectory(source)) {
                    if (!Files.exists(destination)) {
                      Files.createDirectory(destination);
                    }
                  } else {
                    Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
                  }
                } catch (IOException e) {
                  e.printStackTrace();
                }
              });

      Path mainApp = appDir.resolve("RetroLauncher");
      new ProcessBuilder(mainApp.toString()).start();

      System.exit(0);
    } catch (IOException e) {
      e.printStackTrace();
      System.exit(1);
    }
  }
}
