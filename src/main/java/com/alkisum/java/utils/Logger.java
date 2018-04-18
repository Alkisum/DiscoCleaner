package com.alkisum.java.utils;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Utility class to log messages in file.
 *
 * @author Alkisum
 * @version 1.0
 * @since 1.0
 */
public final class Logger {

    /**
     * Log filename.
     */
    private static final String LOG_FILE_NAME = "discocleaner.log";

    /**
     * Logger instance.
     */
    private static Logger logger;

    /**
     * StringBuilder storing logs.
     */
    private final StringBuilder logs;

    /**
     * Logger constructor.
     */
    private Logger() {
        logs = new StringBuilder();
    }

    /**
     * Append message to logs.
     *
     * @param log Message to append to logs
     */
    static void append(final String log) {
        if (logger == null) {
            logger = new Logger();
        }
        logger.logs.append(log);
    }

    /**
     * Write logs to file.
     *
     * @return Written file
     * @throws IOException The file cannot be created or modify
     */
    public static File write() throws IOException {
        if (logger == null) {
            logger = new Logger();
        }
        File logFile = new File(LOG_FILE_NAME);
        logFile.createNewFile();
        try (PrintWriter out = new PrintWriter(logFile)) {
            out.println(logger.logs.toString());
        }
        return logFile;
    }

    /**
     * Open log with editor defined by user.
     *
     * @param file Log file to open
     */
    public static void open(final File file) {
        // build command to execute
        ProcessBuilder pb = new ProcessBuilder();
        pb.command(Config.getTextEditor(), file.getAbsolutePath());

        // start process
        try {
            pb.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
