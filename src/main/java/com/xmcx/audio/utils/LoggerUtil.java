package com.xmcx.audio.utils;

import java.io.*;
import java.util.Locale;
import java.util.function.Supplier;
import java.util.logging.*;

import static com.xmcx.audio.dump.Option.*;

/**
 * Logger util
 */
public class LoggerUtil {

    private static final Logger LOGGER;

    /*
     * init logger
     */
    static {
        Locale.setDefault(Locale.US);
        LogManager logManager = LogManager.getLogManager();
        try (InputStream fis = ClassLoader.getSystemResourceAsStream("logger.properties")) {
            logManager.readConfiguration(fis);
        } catch (IOException ignored) {
        }

        LOGGER = Logger.getLogger("audio.dump");
        // print to console
        LOGGER.addHandler(new InstantlyHandler(System.out));
        // write to file
        if (!LOG_FILE_TOGGLE.isToggled()) {
            String logFileName = getProperty(LOG_FILENAME.getKey(), () -> logManager.getProperty(LOG_FILENAME.getKey()));
            String logFileDirectory = getProperty(LOG_FILE_DIRECTORY.getKey(), () -> System.getProperty("user.dir"));
            File directory = new File(logFileDirectory);
            if (directory.exists() || directory.mkdirs()) {
                try {
                    // stream will be closed when shutdown
                    LOGGER.addHandler(new InstantlyHandler(new FileOutputStream(logFileDirectory + File.separator + logFileName, true)));
                } catch (IOException e) {
                    // should not happen
                    throw new RuntimeException(e);
                }
            } else {
                warn("Directory '%s' is unavailable, please use '%s' to change the directory or use '%s' to disable logging",
                        logFileDirectory, LOG_FILE_DIRECTORY.getOption(), LOG_FILE_TOGGLE.getOption());
            }
        }
    }

    private static class InstantlyHandler extends StreamHandler {

        private static final SimpleFormatter FORMATTER = new SimpleFormatter();
        private final StreamHandler delegate;

        private InstantlyHandler(OutputStream out) {
            delegate = new StreamHandler(out, FORMATTER);
        }

        @Override
        public void publish(LogRecord record) {
            delegate.publish(record);
            delegate.flush();
        }
    }

    private static String getProperty(String key, Supplier<String> defVal) {
        String value = System.getProperty(key);
        if (value == null || value.isEmpty()) {
            value = defVal.get();
        }
        return value;
    }

    /**
     * Info
     */
    public static void info(String msg, Object... args) {
        LOGGER.info(String.format(msg, args));
    }

    /**
     * Warn
     */
    public static void warn(String msg, Object... args) {
        LOGGER.warning(String.format(msg, args));
    }

    /**
     * Log
     */
    public static void log(Level level, String msg, Object... args) {
        LOGGER.log(level, String.format(msg, args));
    }

}
