package com.alkisum.java.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

/**
 * Utility class for printing messages on terminal.
 *
 * @author Alkisum
 * @version 1.0
 * @since 1.0
 */
public final class Terminal {

    /**
     * Code to reset format.
     */
    private static final String ANSI_RESET = "\u001B[0m";

    /**
     * Code for red text.
     */
    private static final String ANSI_RED = "\u001B[31m";

    /**
     * Code for green text.
     */
    private static final String ANSI_GREEN = "\u001B[32m";

    /**
     * Code for yellow text.
     */
    private static final String ANSI_YELLOW = "\u001B[33m";

    /**
     * Code for blue text.
     */
    private static final String ANSI_BLUE = "\u001B[34m";

    //public static final String ANSI_BLACK = "\u001B[30m";
    //public static final String ANSI_PURPLE = "\u001B[35m";
    //public static final String ANSI_CYAN = "\u001B[36m";
    //public static final String ANSI_WHITE = "\u001B[37m";

    /**
     * Date format to parse the build date from the version number.
     */
    private static final SimpleDateFormat PARSER =
            new SimpleDateFormat("yyyyMMdd");

    /**
     * Date format to format the build date parsed from the version number.
     */
    private static final SimpleDateFormat FORMATTER =
            new SimpleDateFormat("MMMM dd, yyyy");

    /**
     * Terminal instance.
     */
    private static Terminal terminal;

    /**
     * Scanner to read user input.
     */
    private final Scanner scanner;

    /**
     * Terminal constructor.
     */
    private Terminal() {
        scanner = new Scanner(System.in);
    }

    /**
     * Print error.
     *
     * @param message Message to print
     * @param indent  Number of indent to use when printing message
     */
    static void printError(final String message,
                           final int indent) {
        String output = buildIndentedMessage(message, indent);
        System.out.println(ANSI_RED + output + ANSI_RESET);
        if (Config.isLogEnabled()) {
            Logger.append(output + "\n");
        }
    }

    /**
     * Print warning.
     *
     * @param message Message to print
     * @param indent  Number of indent to use when printing message
     */
    static void printWarning(final String message,
                             final int indent) {
        String output = buildIndentedMessage(message, indent);
        System.out.println(ANSI_YELLOW + output + ANSI_RESET);
        if (Config.isLogEnabled()) {
            Logger.append(output + "\n");
        }
    }

    /**
     * Print info.
     *
     * @param message Message to print
     * @param indent  Number of indent to use when printing message
     */
    static void printInfo(final String message,
                          final int indent) {
        String output = buildIndentedMessage(message, indent);
        System.out.println(output);
        if (Config.isLogEnabled()) {
            Logger.append(output + "\n");
        }
    }

    /**
     * Print question.
     *
     * @param message Message to print
     * @param indent  Number of indent to use when printing message
     */
    static void printQuestion(final String message,
                              final int indent) {
        String output = buildIndentedMessage(message, indent);
        System.out.println(ANSI_BLUE + output + ANSI_RESET);
    }

    /**
     * Print confirmation.
     *
     * @param message Message to print
     * @param indent  Number of indent to use when printing message
     */
    static void printConfirmation(final String message,
                                  final int indent) {
        String output = buildIndentedMessage(message, indent);
        System.out.println(ANSI_GREEN + output + ANSI_RESET);
        if (Config.isLogEnabled()) {
            Logger.append(output + "\n");
        }
    }

    /**
     * Indent the message according to the given indent number.
     *
     * @param message Message to indent
     * @param indent  Number of indent to use
     * @return Indented message
     */
    private static String buildIndentedMessage(final String message,
                                               final int indent) {
        StringBuilder messageBuilder = new StringBuilder();
        for (int i = 0; i < indent; i++) {
            messageBuilder.append("\t");
        }
        messageBuilder.append(message);
        return messageBuilder.toString();
    }

    /**
     * Ask user to continue.
     *
     * @param indent Number of indent to use
     */
    public static void askToContinue(final int indent) {
        // ask confirmation to continue
        printQuestion("Continue? (Y/n)", indent);

        // get response from user
        String response = getScanner().nextLine();

        // check if user wants to continue
        if (response.toLowerCase().equals("n")) {
            askToContinue(indent);
        }
    }

    /**
     * Ask user to retry.
     *
     * @param indent Number of indent to use
     * @return true if the use wants to retry, false otherwise
     */
    static boolean askToRetry(final int indent) {
        // ask confirmation to retry
        printQuestion("Retry? (Y/n)", indent);

        // get response from user
        String response = getScanner().nextLine();

        // check if user wants to retry
        return !response.toLowerCase().equals("n");
    }

    /**
     * Show version and other information about the app.
     */
    public static void showVersion() {
        // get information from manifest
        Package p = Terminal.class.getPackage();
        String title;
        String version;
        String date;
        String website;

        // check if all information are available
        if (p.getImplementationTitle() != null
                && p.getImplementationVersion() != null
                && p.getImplementationVendor() != null) {
            // show info
            title = p.getImplementationTitle();
            String[] fullVersion = p.getImplementationVersion().split("_");
            version = fullVersion[0];
            try {
                date = FORMATTER.format(PARSER.parse(fullVersion[1]));
            } catch (ParseException e) {
                date = FORMATTER.format(new Date());
            }
            website = p.getImplementationVendor();
        } else {
            // no info from manifest available (program not executed from jar)
            title = "Title";
            version = "x.x";
            date = FORMATTER.format(new Date());
            website = "Website";
        }

        // print information
        System.out.println(title + " " + version);
        System.out.println("Built on " + date);
        System.out.println(website);
    }

    /**
     * Show help section when the user enter the --help argument.
     */
    public static void showHelp() {
        String format = "%-40s%s%n";
        System.out.println(
                "Usage: java -jar DiscoCleaner-x.x.jar [OPTION]" + "\n");
        System.out.println("Options:");
        System.out.printf(format, "--version", "Show program's version");
        System.out.printf(format, "--help", "Show help message");
        System.out.printf(format, "--artist=\"<artist directory name>\"",
                "Proceed with the given artist only");
        System.out.printf(format, "--album=\"<album directory name>\"",
                "Proceed with the given album only");
    }

    /**
     * @return Scanner instance
     */
    static Scanner getScanner() {
        if (terminal == null) {
            terminal = new Terminal();
        }
        return terminal.scanner;
    }

    /**
     * Close the scanner instance.
     */
    public static void closeScanner() {
        if (terminal != null && terminal.scanner != null) {
            terminal.scanner.close();
        }
    }
}
