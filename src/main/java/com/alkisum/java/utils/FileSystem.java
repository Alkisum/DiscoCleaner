package com.alkisum.java.utils;

import com.alkisum.java.exceptions.EmptyAlbumDirectoryException;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.UnsupportedTagException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * Utility class for file system operations.
 *
 * @author Alkisum
 * @version 1.0
 * @since 1.0
 */
public final class FileSystem {

    /**
     * FileSystem constructor.
     */
    private FileSystem() {

    }

    /**
     * Rename album directory by reading year from MP3 tag.
     *
     * @param songs Songs to get the album year from
     * @param album Album directory to rename
     * @throws InvalidDataException    Invalid data
     * @throws IOException             The file cannot be read
     * @throws UnsupportedTagException Unsupported tag
     */
    static void renameAlbumDirectory(final File[] songs,
                                     final File album)
            throws InvalidDataException, IOException, UnsupportedTagException {

        // check if album has already a valid directory name
        if (album.getName().matches(Config.getAlbumPattern())) {
            return;
        }

        // get album directory name from mask
        String albumNameWithMask = Mp3.getAlbumDirectoryNameFromMask(songs);
        if (albumNameWithMask == null) {
            Terminal.printWarning("Cannot build album directory name from mask",
                    1);
            if (Terminal.askToRetry(1)) {
                renameAlbumDirectory(songs, album);
            }
            return;
        }

        String response = "";
        if (!Config.isForceEnabled()) {
            // ask confirmation to rename the album directory
            Terminal.printQuestion("Rename " + album.getName()
                    + " to " + albumNameWithMask + "? (Y/n)", 2);

            // get response from user
            response = Terminal.getScanner().nextLine();
        }

        // check if user wants to rename the album directory
        if (Config.isForceEnabled() || !response.toLowerCase().equals("n")) {
            try {
                // rename album directory with a valid name
                Files.move(album.toPath(), new File(album.getParent(),
                        albumNameWithMask).toPath());
                Terminal.printConfirmation(album.getName() + " renamed", 2);
            } catch (IOException e) {
                // cannot rename album directory: print error
                Terminal.printError("Cannot rename to " + albumNameWithMask, 2);
                e.printStackTrace();
            }
        }
    }

    /**
     * Check if the song files contained in the album directory are files or
     * directories.
     *
     * @param album Album directory containing the songs
     */
    static void checkFiles(final File album) {
        // list song files
        File[] files;
        try {
            files = FileSystem.listSongs(album);
        } catch (EmptyAlbumDirectoryException e) {
            return;
        }

        // browse song files
        for (File song : files) {

            // check if song file is a file
            if (song.isDirectory()) {
                FileSystem.openFileManager(
                        song.getName() + " is not a file",
                        song.getParentFile().getAbsolutePath(), 2);
                Terminal.askToContinue(2);
            }
        }
    }

    /**
     * Delete invalid files in the given album.
     *
     * @param album Album to delete the invalid files from
     */
    static void deleteInvalidFiles(final File album) {
        // list song files
        File[] files;
        try {
            files = FileSystem.listSongs(album);
        } catch (EmptyAlbumDirectoryException e) {
            return;
        }

        // browse song files
        for (File file : files) {

            // check if current file is a MP3 or is a cover
            if (!file.getName().endsWith(".mp3")
                    && !file.getName().equals(Config.getCoverFileName())) {

                String response = "";
                if (!Config.isForceEnabled()) {
                    // invalid filename: ask confirmation to delete the file
                    Terminal.printQuestion(
                            "Delete " + file.getName() + "? (Y/n)", 2);

                    // get response from user
                    response = Terminal.getScanner().nextLine();
                }


                // check if user wants to delete the file
                if (Config.isForceEnabled()
                        || !response.toLowerCase().equals("n")) {
                    try {
                        // delete file
                        Files.delete(file.toPath());
                        Terminal.printConfirmation(
                                file.getName() + " deleted", 2);
                    } catch (IOException e) {
                        // cannot delete file: print error
                        Terminal.printError(
                                "Cannot delete " + file.getName(), 2);
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * List songs contained in the given album directory.
     *
     * @param album Album directory to get the files from
     * @return List of files contained in the album directory
     * @throws EmptyAlbumDirectoryException Empty album directory
     */
    static File[] listSongs(final File album)
            throws EmptyAlbumDirectoryException {
        // list song files
        File[] songs = album.listFiles();

        // check if album directory contains song files
        if (songs == null || songs.length == 0) {
            FileSystem.openFileManager("No songs in directory",
                    album.getAbsolutePath(), 2);
            Terminal.askToContinue(2);
            throw new EmptyAlbumDirectoryException();
        }

        return songs;
    }

    /**
     * Open file manager.
     *
     * @param message Message to print before opening file manager
     * @param path    Path to use when opening the file manager
     * @param indent  Number of indent to use when printing message
     */
    public static void openFileManager(final String message,
                                       final String path,
                                       final int indent) {
        // check if file manager is specified in configuration
        if (Config.getFileManager() == null) {
            // print simple warning without opening file manager
            Terminal.printWarning(message + ".", indent);
            return;
        }

        // ask confirmation to open the file manager
        Terminal.printWarning(message + ". Open? (Y/n)", indent);

        // get response from user
        String response = Terminal.getScanner().nextLine();

        // check if user wants to open file manager
        if (!response.toLowerCase().equals("n")) {

            // open file manager
            startFileManagerProcess(path, indent);
        }
    }

    /**
     * Start process to open external file manager.
     *
     * @param path   Path to open the file manager to
     * @param indent Number of indent to use when printing message
     */
    private static void startFileManagerProcess(final String path,
                                                final int indent) {
        // build command to execute
        ProcessBuilder pb = new ProcessBuilder();
        pb.command(Config.getFileManager(), path);

        // start process
        try {
            pb.start();
        } catch (IOException e) {
            // cannot open file manager: print error
            Terminal.printError("Cannot open file manager", indent);
            e.printStackTrace();
        }
    }
}
