package com.alkisum.java.utils;

import com.alkisum.java.exceptions.EmptyAlbumDirectoryException;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.NotSupportedException;
import com.mpatric.mp3agic.UnsupportedTagException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Scanner;

/**
 * Utility class for cover files.
 *
 * @author Alkisum
 * @version 1.0
 * @since 1.0
 */
final class Cover {

    /**
     * Cover constructor.
     */
    private Cover() {

    }

    /**
     * Check if a cover file already exists or try to rename or convert
     * an existing file.
     *
     * @param album Album directory containing the songs
     * @throws IOException          ImageMagick cannot convert the image
     * @throws InterruptedException Current thread has been interrupted
     */
    static void replaceCoverFile(final File album)
            throws IOException, InterruptedException {
        // list song files
        File[] files;
        try {
            files = FileSystem.listSongs(album);
        } catch (EmptyAlbumDirectoryException e) {
            return;
        }

        // browse song files
        for (File file : files) {

            // check if cover already exists
            File coverFile = Config.getCoverFile(file.getParentFile());
            if (coverFile.exists()) {
                break;
            }

            // rename old cover to new cover
            if (file.isFile()
                    && Arrays.asList(Config.getObsoleteCoverFileName())
                    .contains(file.getName())) {

                // check files extension
                if (getExtension(coverFile.getName()).equals(
                        getExtension(file.getName()))) {

                    // image type identical: rename it
                    renameCoverFile(file);
                } else {

                    // image type different: convert it
                    convertImage(file, coverFile);
                }
            }
        }
    }

    /**
     * Rename the given cover file with a valid cover filename.
     *
     * @param cover Cover file to rename
     */
    private static void renameCoverFile(final File cover) {
        String response = "";
        if (!Config.isForceEnabled()) {
            // ask confirmation to rename the cover file
            Terminal.printQuestion("Rename " + cover.getName() + " to "
                    + Config.getCoverFileName() + "? (Y/n)", 2);

            // get response from user
            response = Terminal.getScanner().nextLine();
        }

        // check if user wants to rename the cover file
        if (Config.isForceEnabled() || !response.toLowerCase().equals("n")) {
            try {
                // rename cover file with a valid cover filename
                Files.move(cover.toPath(), Config.getCoverFile(
                        cover.getParentFile()).toPath());
                Terminal.printConfirmation(cover.getName() + " renamed", 2);
            } catch (IOException e) {
                // cannot rename cover file: print error
                Terminal.printError("Cannot rename to "
                        + Config.getCoverFileName(), 2);
                e.printStackTrace();
            }
        }
    }

    /**
     * Check if a cover file name exists for the given album.
     *
     * @param album Album to check
     */
    static void checkCoverExists(final File album) {
        // check if a cover file name exists
        if (!Config.getCoverFile(album).exists()) {

            // no cover file exists: tell user to create one
            createCoverFile(album);
        }
    }

    /**
     * Create cover file by opening the file manager only.
     *
     * @param album Album to create the cover for
     */
    private static void createCoverFile(final File album) {
        FileSystem.openFileManager("Cover does not exist",
                album.getAbsolutePath(), 2);
        if (Terminal.askToRetry(2)) {
            checkCoverExists(album);
        }
    }

    /**
     * Check if cover is a baseline or progressive jpeg, convert to baseline
     * if necessary and load new cover to MP3 tag.
     *
     * @param album Album to process the cover for
     */
    static void processCover(final File album) {
        try {
            // get cover file
            File coverFile = Config.getCoverFile(album);

            // check if cover is a baseline or a progressive jpeg
            if (Cover.isCoverBaselineJpeg(coverFile)) {

                // read cover bytes from MP3 tag
                byte[] mp3CoverBytes = Mp3.getCoverFromMp3Tag(album);

                // read cover bytes from file
                byte[] coverFileBytes = Files.readAllBytes(coverFile.toPath());

                // check if MP3 cover and cover file are different
                if (!Arrays.equals(mp3CoverBytes, coverFileBytes)) {

                    // load cover file bytes to MP3 file tag
                    Mp3.loadCoverToMp3(album, coverFileBytes);
                }

            } else {
                // convert image to baseline jpeg
                convertImage(coverFile, coverFile);

                // read cover bytes from file
                byte[] coverFileBytes = Files.readAllBytes(coverFile.toPath());

                // load cover file bytes to MP3 file tag
                Mp3.loadCoverToMp3(album, coverFileBytes);
            }
        } catch (IOException | InvalidDataException | UnsupportedTagException
                | NotSupportedException | InterruptedException e) {
            Terminal.printError("Cannot convert cover", 1);
            e.printStackTrace();
        }
    }

    /**
     * Check if the cover is a baseline JPEG using ImageMagick.
     *
     * @param cover File to check
     * @return true if the cover is a baseline JPEG, false otherwise
     * @throws IOException Cannot execute ImageMagick command
     */
    private static boolean isCoverBaselineJpeg(final File cover)
            throws IOException {
        // build command to execute
        ProcessBuilder pb = new ProcessBuilder();

        // use ImageMagick to detect if the image is a progressive or baseline
        // jpeg
        pb.command("identify", "-verbose", cover.getAbsolutePath());

        // start process
        Process process = pb.start();

        // get result
        InputStream is = process.getInputStream();

        // read command input stream
        boolean baselineJpeg = false;
        try (Scanner s = new Scanner(is).useDelimiter("\\A")) {
            while (s.hasNextLine()) {
                String line = s.nextLine();
                if (line.contains("Interlace:")) {
                    if (line.contains("None")) {
                        baselineJpeg = true;
                        break;
                    }
                }
            }
        }
        return baselineJpeg;
    }

    /**
     * Convert image using ImageMagick.
     *
     * @param src    Source image to convert
     * @param target Target image
     * @throws IOException          ImageMagick cannot convert the image
     * @throws InterruptedException Current thread has been interrupted
     */
    private static void convertImage(final File src, final File target)
            throws IOException, InterruptedException {
        // build command to execute
        ProcessBuilder pb = new ProcessBuilder();

        // use ImageMagick to convert image
        pb.command("convert", src.getAbsolutePath(), target.getAbsolutePath());

        // start process
        Process p = pb.start();

        // wait for the process to finish
        p.waitFor();

        // confirm conversion
        Terminal.printConfirmation(src.getName() + " converted to "
                + target.getName(), 2);
    }

    /**
     * Get extension from given filename.
     *
     * @param filename Filename to get the extension from
     * @return Extension from filename
     */
    private static String getExtension(final String filename) {
        String extension = "";
        int i = filename.lastIndexOf('.');
        if (i > 0) {
            extension = filename.substring(i + 1);
        }
        return extension;
    }

    /**
     * Check if the file is a jpeg from the given filename.
     *
     * @param filename Filename to check
     * @return true if the file is a jpeg, false otherwise
     */
    static boolean isJpg(final String filename) {
        String extension = getExtension(filename);
        return extension.toLowerCase().equals("jpg")
                || extension.toLowerCase().equals("jpeg");
    }
}
