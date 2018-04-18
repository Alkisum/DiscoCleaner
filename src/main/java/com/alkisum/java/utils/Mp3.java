package com.alkisum.java.utils;

import com.alkisum.java.exceptions.EmptyAlbumDirectoryException;
import com.mpatric.mp3agic.ID3v2FrameSet;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.NotSupportedException;
import com.mpatric.mp3agic.UnsupportedTagException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility class for mp3 files.
 *
 * @author Alkisum
 * @version 1.0
 * @since 1.0
 */
final class Mp3 {

    /**
     * Map with frame specifications and their descriptions.
     */
    private static final Map<String, String> FRAME_SPECS;

    static {
        FRAME_SPECS = new HashMap<>();
        FRAME_SPECS.put("APIC", "Cover");
        FRAME_SPECS.put("TALB", "Album");
        FRAME_SPECS.put("TIT2", "Title");
        FRAME_SPECS.put("TPE1", "Artist");
        FRAME_SPECS.put("TRCK", "Track number");
        FRAME_SPECS.put("TYER", "Year");
    }

    /**
     * Mp3 constructor.
     */
    private Mp3() {

    }

    /**
     * Handle MP3 files.
     *
     * @param album Album directory containing the songs
     */
    static void handleMp3Files(final File album) {
        // list song files
        File[] files;
        try {
            files = FileSystem.listSongs(album);
        } catch (EmptyAlbumDirectoryException e) {
            return;
        }

        // check if MP3 pattern is specified in configuration
        if (Config.getMp3Pattern() == null) {
            return;
        }

        // browse song files
        for (File song : files) {

            // check if MP3 file should be renamed
            if (song.isFile()
                    && song.getName().endsWith(".mp3")
                    && !song.getName().matches(Config.getMp3Pattern())) {
                FileSystem.openFileManager(
                        song.getName() + " is not a valid filename",
                        album.getAbsolutePath(), 2);
                if (Terminal.askToRetry(2)) {
                    handleMp3Files(album);
                    return;
                }
            }
        }
    }

    /**
     * Read MP3 tag and check its validity.
     *
     * @param album Album containing the MP3 to check
     * @throws InvalidDataException    Invalid data
     * @throws IOException             The file cannot be read
     * @throws UnsupportedTagException Unsupported tag
     */
    static void checkMp3Tag(final File album)
            throws InvalidDataException, IOException, UnsupportedTagException {
        // list song files
        File[] files;
        try {
            files = FileSystem.listSongs(album);
        } catch (EmptyAlbumDirectoryException e) {
            return;
        }

        // browse song files
        for (File song : files) {

            // check only MP3
            if (!song.getName().endsWith(".mp3")) {
                break;
            }

            // create MP3 file
            Mp3File mp3File = new Mp3File(song);

            // check if MP3 file has ID3V1 tag
            if (mp3File.hasId3v1Tag()) {
                Terminal.printWarning(song.getName() + " has Id3v1 tag", 2);
                if (Terminal.askToRetry(2)) {
                    checkMp3Tag(album);
                    return;
                }
            }

            // check if MP3 file has ID3V2 tag
            if (mp3File.hasId3v2Tag()) {

                // get frame sets
                Map<String, ID3v2FrameSet> frameSets = mp3File.getId3v2Tag().
                        getFrameSets();

                // check for missing frame in MP3 tag
                List<String> missingFrames = new ArrayList<>();
                for (String frame : Config.getTagFrames()) {
                    if (frameSets.get(frame) == null) {
                        missingFrames.add(frame);
                    }
                }

                // check if there are missing frames
                if (!missingFrames.isEmpty()) {
                    Terminal.printWarning(
                            song.getName() + " has missing frames:\n"
                                    + printMissingFrames(missingFrames), 2);
                    if (Terminal.askToRetry(2)) {
                        checkMp3Tag(album);
                        return;
                    }
                }

                // check if MP3 tag has non-allowed frames
                if (frameSets.size() > Config.getTagFrames().length) {
                    String response = "";
                    if (!Config.isForceEnabled()) {
                        // ask confirmation to clean tag
                        Terminal.printQuestion(
                                song.getName() + " has invalid tag. Clean?", 2);

                        // get response from user
                        response = Terminal.getScanner().nextLine();
                    }

                    // check if user wants to clean tag
                    if (Config.isForceEnabled()
                            || !response.toLowerCase().equals("n")) {
                        try {
                            // clean tag
                            cleanTag(mp3File);

                            // delete mp3File instance to not use it again
                            // when removing custom tag
                            mp3File = null;
                        } catch (NotSupportedException e) {
                            // cannot clean tag: print error
                            Terminal.printError("Cannot clean tag", 2);
                            e.printStackTrace();
                        }
                    }
                }
            }

            // create new instance if necessary
            if (mp3File == null) {
                mp3File = new Mp3File(song);
            }

            // check if MP3 file has custom tag
            if (!Config.isCustomTagAllowed() && mp3File.hasCustomTag()) {

                String response = "";
                if (!Config.isForceEnabled()) {
                    // ask confirmation to delete custom tag
                    Terminal.printQuestion("Delete custom tag? (Y/n)", 2);

                    // get response from user
                    response = Terminal.getScanner().nextLine();
                }

                // check if user wants to delete custom tag
                if (Config.isForceEnabled()
                        || !response.toLowerCase().equals("n")) {
                    try {
                        // delete custom tag
                        mp3File.removeCustomTag();

                        // save MP3 file
                        saveMp3File(mp3File);

                        // print confirmation
                        Terminal.printConfirmation("Custom tag deleted", 2);
                    } catch (NotSupportedException e) {
                        // cannot delete custom tag: print error
                        Terminal.printError("Cannot delete custom tag", 2);
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * Clean tag by removing the frames that are not specified in the
     * configuration.
     *
     * @param mp3File MP3 file to clean
     * @throws IOException           The file cannot be saved, move or deleted
     * @throws NotSupportedException Not supported tag
     */
    private static void cleanTag(final Mp3File mp3File)
            throws IOException, NotSupportedException {
        // get frame sets from MP3 file
        Map<String, ID3v2FrameSet> frameSets = mp3File.getId3v2Tag()
                .getFrameSets();

        // get tag frames specified in configuration
        List<String> tagFrames = Arrays.asList(Config.getTagFrames());

        // list containing frames to delete
        List<String> framesToDelete = new ArrayList<>();

        // add frames id to delete
        for (String key : frameSets.keySet()) {
            if (!tagFrames.contains(key)) {
                framesToDelete.add(key);
            }
        }

        // delete frames
        for (String id : framesToDelete) {
            mp3File.getId3v2Tag().clearFrameSet(id);
        }

        // save
        saveMp3File(mp3File);

        // print confirmation
        Terminal.printConfirmation("Tag cleaned", 2);
    }

    /**
     * Save MP3 file. A temporary file has to be created because the library
     * mp3agic does not allow to save a MP3 file with the same name as the
     * original file.
     *
     * @param mp3File MP3 file to save
     * @throws IOException           The file cannot be saved, move or deleted
     * @throws NotSupportedException Not supported tag
     */
    private static void saveMp3File(final Mp3File mp3File)
            throws IOException, NotSupportedException {
        File originalFile = new File(mp3File.getFilename());
        String tmpFilePath = mp3File.getFilename() + "_tmp";
        mp3File.save(tmpFilePath);
        Files.delete(originalFile.toPath());
        Files.move(new File(tmpFilePath).toPath(), originalFile.toPath());
    }

    /**
     * Retrieve information from MP3 file tag and build the album directory name
     * from it, using the mask specified in configuration.
     *
     * @param files Files to use to retrieve information from MP3 tag
     * @return Album directory name built from specified mask
     * @throws InvalidDataException    Invalid data
     * @throws IOException             The file cannot be read
     * @throws UnsupportedTagException Unsupported tag
     */
    static String getAlbumDirectoryNameFromMask(final File[] files)
            throws InvalidDataException, IOException, UnsupportedTagException {
        // get information from one MP3 file in the album directory
        for (File file : files) {
            if (file.getName().endsWith(".mp3")) {
                // get artist
                String artist = getArtistFromMp3Tag(file);

                // get album
                String album = getAlbumFromMp3Tag(file);

                // get year
                String year = getYearFromMp3Tag(file);

                // get mask
                String mask = Config.getAlbumMask();

                // replace %a by artist name
                if (artist != null) {
                    mask = mask.replaceAll("%a", artist);
                } else {
                    Terminal.printWarning("Artist cannot be used in album "
                            + "directory mask (null)", 2);
                }

                // replace %b by album name
                if (album != null) {
                    mask = mask.replaceAll("%b", album);
                } else {
                    Terminal.printWarning("Album cannot be used in album "
                            + "directory mask (null)", 2);
                }

                // replace %y by song year
                if (year != null) {
                    mask = mask.replaceAll("%y", year);
                } else {
                    Terminal.printWarning("Year cannot be used in album "
                            + "directory mask (null)", 2);
                }

                // remove illegal characters
                mask = mask.replaceAll("[\\\\/:*?\"<>|]", "");

                return mask;
            }
        }

        // no MP3 file in the album directory
        return null;
    }

    /**
     * Read the given file and retrieve the artist written in the MP3 tag.
     *
     * @param file MP3 file to read
     * @return The artist written in the MP3 file
     * @throws InvalidDataException    Invalid data
     * @throws IOException             The file cannot be read
     * @throws UnsupportedTagException Unsupported tag
     */
    private static String getArtistFromMp3Tag(final File file)
            throws InvalidDataException, IOException, UnsupportedTagException {
        String artist = "";
        Mp3File mp3File = new Mp3File(file);
        if (mp3File.hasId3v1Tag()) {
            artist = mp3File.getId3v1Tag().getArtist();
        } else if (mp3File.hasId3v2Tag()) {
            artist = mp3File.getId3v2Tag().getArtist();
        }
        return artist;
    }

    /**
     * Read the given file and retrieve the album written in the MP3 tag.
     *
     * @param file MP3 file to read
     * @return The album written in the MP3 file
     * @throws InvalidDataException    Invalid data
     * @throws IOException             The file cannot be read
     * @throws UnsupportedTagException Unsupported tag
     */
    private static String getAlbumFromMp3Tag(final File file)
            throws InvalidDataException, IOException, UnsupportedTagException {
        String album = "";
        Mp3File mp3File = new Mp3File(file);
        if (mp3File.hasId3v1Tag()) {
            album = mp3File.getId3v1Tag().getAlbum();
        } else if (mp3File.hasId3v2Tag()) {
            album = mp3File.getId3v2Tag().getAlbum();
        }
        return album;
    }

    /**
     * Read the given file and retrieve the year written in the MP3 tag.
     *
     * @param file MP3 file to read
     * @return The year written in the MP3 file
     * @throws InvalidDataException    Invalid data
     * @throws IOException             The file cannot be read
     * @throws UnsupportedTagException Unsupported tag
     */
    private static String getYearFromMp3Tag(final File file)
            throws InvalidDataException, IOException, UnsupportedTagException {
        String year = "";
        Mp3File mp3File = new Mp3File(file);
        if (mp3File.hasId3v1Tag()) {
            year = mp3File.getId3v1Tag().getYear();
        } else if (mp3File.hasId3v2Tag()) {
            year = mp3File.getId3v2Tag().getYear();
        }
        return year;
    }

    /**
     * Print missing frames description.
     *
     * @param missingFrames Frames to get the descriptions from
     * @return List of missing frames descriptions
     */
    private static String printMissingFrames(final List<String> missingFrames) {
        StringBuilder message = new StringBuilder();
        for (String frame : missingFrames) {
            message.append("\t\t\t\t- ");
            message.append(FRAME_SPECS.get(frame));
            message.append("\n");
        }
        return message.toString();
    }

    /**
     * Get the cover from the MP3 files contained in the given album.
     *
     * @param album Album to get the cover for
     * @return Album cover
     * @throws InvalidDataException    Invalid data
     * @throws IOException             The file cannot be read
     * @throws UnsupportedTagException Unsupported tag
     */
    static byte[] getCoverFromMp3Tag(final File album)
            throws InvalidDataException, IOException, UnsupportedTagException {
        // list song files
        File[] files;
        try {
            files = FileSystem.listSongs(album);
        } catch (EmptyAlbumDirectoryException e) {
            return null;
        }

        // browse files
        for (File file : files) {
            if (file.getName().endsWith(".mp3")) {
                // get MP3 file
                Mp3File mp3File = new Mp3File(file);

                // get cover from MP3 file tag
                return mp3File.getId3v2Tag().getAlbumImage();
            }
        }
        return null;
    }

    /**
     * Load the given cover to the MP3 file tags contained in the given album.
     *
     * @param album          Album to set the cover for
     * @param coverFileBytes Cover to set
     * @throws InvalidDataException    Invalid data
     * @throws IOException             The file cannot be read
     * @throws UnsupportedTagException Unsupported tag
     * @throws NotSupportedException   Not supported tag
     */
    static void loadCoverToMp3(final File album,
                               final byte[] coverFileBytes)
            throws InvalidDataException, IOException, UnsupportedTagException,
            NotSupportedException {
        // list song files
        File[] files;
        try {
            files = FileSystem.listSongs(album);
        } catch (EmptyAlbumDirectoryException e) {
            return;
        }

        // browse files
        for (File file : files) {
            if (file.getName().endsWith(".mp3")) {
                // get MP3 file
                Mp3File mp3File = new Mp3File(file);

                // set cover
                mp3File.getId3v2Tag().setAlbumImage(
                        coverFileBytes, "image/jpeg");

                // save MP3 file
                saveMp3File(mp3File);
            }
        }
        Terminal.printConfirmation("Cover saved to MP3", 2);
    }
}
