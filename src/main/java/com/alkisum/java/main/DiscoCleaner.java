package com.alkisum.java.main;

import com.alkisum.java.utils.Browser;
import com.alkisum.java.utils.Config;
import com.alkisum.java.utils.FileSystem;
import com.alkisum.java.utils.Logger;
import com.alkisum.java.utils.Terminal;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Main class for DiscoCleaner.
 *
 * @author Alkisum
 * @version 1.0
 * @since 1.0
 */
public final class DiscoCleaner {

    /**
     * Main method.
     *
     * @param args Arguments (none)
     */
    public static void main(final String[] args) {
        // get arguments
        Map<String, String> arguments = new HashMap<>();
        for (String arg : args) {
            if (arg.startsWith("--artist=")) {
                // get artist argument
                String[] artistParams = arg.split("=");
                if (artistParams.length > 1) {
                    arguments.put("artist", artistParams[1]);
                }
            } else if (arg.startsWith("--album=")) {
                // get album argument
                String[] albumParams = arg.split("=");
                if (albumParams.length > 1) {
                    arguments.put("album", albumParams[1]);
                }
            } else if (arg.equals("--version")) {
                // show version
                Terminal.showVersion();
                return;
            } else if (arg.equals("--help")) {
                // show help
                Terminal.showHelp();
                return;
            } else {
                // no matching argument
                System.out.println(arg + " is not a valid option. "
                        + "--help for help");
                return;
            }
        }

        // start DiscoCleaner
        new DiscoCleaner().start(arguments);
    }

    /**
     * DiscoCleaner constructor.
     */
    private DiscoCleaner() {

    }

    /**
     * Start application.
     *
     * @param arguments Arguments to add to configuration
     */
    private void start(final Map<String, String> arguments) {
        try {
            // build configuration
            Config.build();

            // add arguments to configuration
            Config.addArguments(arguments);

            // create root file (discography to browse)
            File root = new File(Config.getPath());

            // list artist files
            File[] artists = root.listFiles();

            // check if root directory contains artist files
            if (artists == null || artists.length == 0) {
                FileSystem.openFileManager("No artists in directory",
                        Config.getPath(), 0);
                Terminal.askToContinue(0);
                return;
            }

            // browse artist files
            Browser.browseArtists(artists);

            // write logs
            if (Config.isLogEnabled()) {
                try {
                    // write to file
                    File logFile = Logger.write();

                    if (Config.isShowLogEnabled()
                            && Config.getTextEditor() != null) {
                        Logger.open(logFile);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            Terminal.closeScanner();
        }
    }
}
