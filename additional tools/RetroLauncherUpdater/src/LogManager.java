import java.io.IOException;
import java.util.logging.*;

/** Utility class for managing application logging. */
public class LogManager {
    private static Logger logger = Logger.getLogger("RetroLauncherUpdater");

    static {
        try {
            Handler fileHandler = new FileHandler("retrolauncherupdater.log", true);
            SimpleFormatter formatter = new SimpleFormatter();
            fileHandler.setFormatter(formatter);

            logger.addHandler(fileHandler);
            logger.setLevel(Level.ALL);

            // Optional: Remove console handler if not needed
            Logger rootLogger = Logger.getLogger("");
            Handler[] handlers = rootLogger.getHandlers();
            if (handlers[0] instanceof ConsoleHandler) {
                rootLogger.removeHandler(handlers[0]);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Logger getLogger() {
        return logger;
    }
}
