package com.alkisum.java.utils;

import com.alkisum.java.exceptions.EmptyAlbumDirectoryException;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.UnsupportedTagException;

import java.io.File;
import java.io.IOException;

/**
 * Utility class for browsing operations.
 *
 * @author Alkisum
 * @version 1.0
 * @since 1.0
 */
public final class Browser {

    /**
     * Browser constructor.
     */
    private Browser() {

    }

    /**
     * Browse artist files.
     *
     * @param artists List of artist files
     */
    public static void browseArtists(final File[] artists) {
        // browse artist files
        for (File artist : artists) {

            if (Config.getArtist() != null
                    && !Config.getArtist().equals(artist.getName())
                    && Config.getAlbum() == null) {
                continue;
            }

            // print current artist name only if the album is not specified
            if (Config.getAlbum() == null) {
                Terminal.printInfo(artist.getName() + ":", 0);
            }

            // check if artist file is a directory
            if (artist.isFile()) {
                FileSystem.openFileManager(
                        artist.getName() + " is not a directory",
                        artist.getParentFile().getAbsolutePath(), 0);
                Terminal.askToContinue(0);
                continue;
            }

            // list album files
            File[] albums = artist.listFiles();

            // check if artist directory contains album files
            if (albums == null || albums.length == 0) {
                FileSystem.openFileManager("No albums in directory",
                        artist.getAbsolutePath(), 0);
                Terminal.askToContinue(0);
                continue;
            }

            // browse album files
            browseAlbums(albums);
        }
    }

    /**
     * Browse album files.
     *
     * @param albums List of album files
     */
    private static void browseAlbums(final File[] albums) {
        // browse album files
        for (File album : albums) {

            if (Config.getAlbum() != null
                    && !Config.getAlbum().equals(album.getName())) {
                continue;
            }

            // print current album name
            Terminal.printInfo(album.getName() + ":", 1);

            // check if album file is a directory
            if (album.isFile()) {
                FileSystem.openFileManager(
                        album.getName() + " is not a directory",
                        album.getParentFile().getAbsolutePath(), 1);
                Terminal.askToContinue(1);
                continue;
            }

            // browse song files
            browseSongs(album);

            // list song files
            File[] files;
            try {
                files = FileSystem.listSongs(album);
            } catch (EmptyAlbumDirectoryException e) {
                continue;
            }

            if (Config.getAlbumPattern() == null
                    || Config.getAlbumMask() == null) {
                continue;
            }
            try {
                // rename album directory
                FileSystem.renameAlbumDirectory(files, album);
            } catch (InvalidDataException | IOException
                    | UnsupportedTagException e) {
                Terminal.printError("Cannot read MP3 tag", 1);
                e.printStackTrace();
            }

            // print message to notify the user that the album is valid
            Terminal.printConfirmation("[OK]", 2);
        }
    }

    /**
     * Browse song files.
     *
     * @param album Album directory containing the songs
     */
    private static void browseSongs(final File album) {
        // check song files
        FileSystem.checkFiles(album);

        // handle MP3 files
        Mp3.handleMp3Files(album);

        // make sure tag frames are specified in configuration
        if (Config.getTagFrames() != null) {
            try {
                // check MP3 tag
                Mp3.checkMp3Tag(album);
            } catch (InvalidDataException | IOException
                    | UnsupportedTagException e) {
                Terminal.printError("Cannot read MP3 tag", 1);
                e.printStackTrace();
            }
        }

        // make sure cover file name is specified in configuration
        if (Config.getCoverFileName() != null) {

            // make sure obsolete cover file names are specified in
            // configuration
            if (Config.getObsoleteCoverFileName() != null) {

                // replace cover file if necessary
                try {
                    Cover.replaceCoverFile(album);
                } catch (IOException | InterruptedException e) {
                    Terminal.printError("Cannot replace cover file", 1);
                    e.printStackTrace();
                }
            }

            // check if cover file exists
            Cover.checkCoverExists(album);

            // process only JPEG cover files
            File coverFile = Config.getCoverFile(album);
            if (coverFile.exists()
                    && Cover.isJpg(coverFile.getName())
                    && Config.isProcessCoverEnabled()) {
                Cover.processCover(album);
            }
        }

        // delete invalid files
        FileSystem.deleteInvalidFiles(album);
    }
}
