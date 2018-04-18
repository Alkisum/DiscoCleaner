package com.alkisum.java.utils;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;
import java.util.Properties;

/**
 * Utility class for the application configuration.
 *
 * @author Alkisum
 * @version 1.0
 * @since 1.0
 */
public final class Config {

    /**
     * Configuration file name.
     */
    private static final String CONFIG_FILE_NAME = "discocleaner.properties";

    /**
     * Configuration instance.
     */
    private static Config config;

    /**
     * Add arguments to configuration.
     *
     * @param arguments Map of arguments passed when starting the program
     */
    public static void addArguments(final Map<String, String> arguments) {
        config.artist = arguments.get("artist");
        config.album = arguments.get("album");
    }

    /**
     * Path to discography to browse.
     */
    private String path = System.getProperty("user.home") + "/Music/";

    /**
     * File manager program.
     */
    private String fileManager = null;

    /**
     * Text editor program.
     */
    private String textEditor = null;

    /**
     * Pattern for MP3 filenames.
     */
    private String mp3Pattern = null;

    /**
     * Pattern for album directory names.
     */
    private String albumPattern = null;

    /**
     * Mask to use for album directory name.
     * %a = artist
     * %b = album
     * %y = year
     */
    private String albumMask = null;

    /**
     * Frame allowed and mandatory in MP3 tag.
     */
    private String[] tagFrames = null;

    /**
     * Flag set to true if the MP3 file custom tag is allowed, false otherwise.
     */
    private boolean customTagAllowed = true;

    /**
     * How the cover file should be named.
     */
    private String coverFileName = null;

    /**
     * How the old cover file might be named. These names will be used when
     * renaming the cover file to {@link Config#coverFileName}.
     */
    private String[] obsoleteCoverFileName = null;

    /**
     * Only for jpeg: true if the cover should be processed (convert progressive
     * to baseline, load image to MP3 if different).
     */
    private boolean processCoverEnabled = false;

    /**
     * Flag set to true if no confirmation is asked to the user,
     * false otherwise.
     */
    private boolean forceEnabled = false;

    /**
     * Flag set to true if logs has to be written at the end of the process,
     * false otherwise.
     */
    private boolean logEnabled = false;

    /**
     * Flag set to true if the logs has to be shown in an editor at the end of
     * the process, false otherwise.
     */
    private boolean showLogEnabled = false;

    /**
     * Artist directory name given in --artist argument. If this attribute is
     * specified, only this artist will be processed.
     */
    private String artist = null;

    /**
     * Album directory name given in --album argument. If this attribute is
     * specified, only this artist will be processed.
     */
    private String album = null;

    /**
     * Config constructor.
     */
    private Config() {

    }

    /**
     * Build configuration from properties file.
     *
     * @throws IOException The properties file cannot be read
     */
    public static void build() throws IOException {
        // create config instance
        if (config == null) {
            config = new Config();
        }

        // properties file
        File configFile = new File(CONFIG_FILE_NAME);

        // check if properties file exists
        if (!configFile.exists()) {
            // copy properties file from resource
            URL src = config.getClass().getResource("/" + CONFIG_FILE_NAME);
            FileUtils.copyURLToFile(src, configFile);
        }

        // load properties file
        loadProperties();
    }

    /**
     * Load configuration from properties file.
     *
     * @throws IOException The properties file cannot be read
     */
    private static void loadProperties() throws IOException {
        Properties prop = new Properties();
        try (InputStream input = new FileInputStream(CONFIG_FILE_NAME)) {
            // load properties from file
            prop.load(input);

            // load music directory path
            String path = prop.getProperty("music.directory.path");
            if (path != null && !path.isEmpty()) {
                config.path = path;
            }

            // load file manager
            String fileManager = prop.getProperty("file.manager");
            if (fileManager != null && !fileManager.isEmpty()) {
                config.fileManager = fileManager;
            }

            // load text editor
            String textEditor = prop.getProperty("text.editor");
            if (textEditor != null && !textEditor.isEmpty()) {
                config.textEditor = textEditor;
            }

            // load MP3 pattern
            String mp3Pattern = prop.getProperty("mp3.pattern");
            if (mp3Pattern != null && !mp3Pattern.isEmpty()) {
                config.mp3Pattern = mp3Pattern;
            }

            // load album pattern
            String albumPattern = prop.getProperty("album.pattern");
            if (albumPattern != null && !albumPattern.isEmpty()) {
                config.albumPattern = albumPattern;
            }

            // load album mask
            String albumMask = prop.getProperty("album.mask");
            if (albumMask != null && !albumMask.isEmpty()) {
                config.albumMask = albumMask;
            }

            // load tag frames
            String tagFrames = prop.getProperty("tag.frames");
            if (tagFrames != null && !tagFrames.isEmpty()) {
                config.tagFrames = tagFrames.split(",");
            }

            // load custom tag allowed flag
            String customTagAllowed = prop.getProperty("custom.tag.allowed");
            if (customTagAllowed != null && !customTagAllowed.isEmpty()) {
                config.customTagAllowed = Boolean.parseBoolean(
                        customTagAllowed);
            }

            // load cover file name
            String coverFileName = prop.getProperty("cover.file.name");
            if (coverFileName != null && !coverFileName.isEmpty()) {
                config.coverFileName = coverFileName;
            }

            // load obsolete cover file name
            String obsoleteCoverFileName = prop.getProperty(
                    "obsolete.cover.file.name");
            if (obsoleteCoverFileName != null
                    && !obsoleteCoverFileName.isEmpty()) {
                config.obsoleteCoverFileName = obsoleteCoverFileName.split(",");
            }

            // load process cover enabled flag
            String processCoverEnabled = prop.getProperty(
                    "process.cover.enabled");
            if (processCoverEnabled != null && !processCoverEnabled.isEmpty()) {
                config.processCoverEnabled = Boolean.parseBoolean(
                        processCoverEnabled);
            }

            // load force enabled flag
            String forceEnabled = prop.getProperty("force.enabled");
            if (forceEnabled != null && !forceEnabled.isEmpty()) {
                config.forceEnabled = Boolean.parseBoolean(forceEnabled);
            }

            // load log enabled flag
            String logEnabled = prop.getProperty("log.enabled");
            if (logEnabled != null && !logEnabled.isEmpty()) {
                config.logEnabled = Boolean.parseBoolean(logEnabled);
            }

            // load show log enabled flag
            String showLogEnabled = prop.getProperty("show.log.enabled");
            if (showLogEnabled != null && !showLogEnabled.isEmpty()) {
                config.showLogEnabled = Boolean.parseBoolean(showLogEnabled);
            }
        }
    }

    /**
     * @return Path to discography to browse
     */
    public static String getPath() {
        return config.path;
    }

    /**
     * @return File manager program
     */
    static String getFileManager() {
        return config.fileManager;
    }

    /**
     * @return Text editor program
     */
    public static String getTextEditor() {
        return config.textEditor;
    }

    /**
     * @return Pattern for MP3 filenames
     */
    static String getMp3Pattern() {
        return config.mp3Pattern;
    }

    /**
     * @return Pattern for album directory names
     */
    static String getAlbumPattern() {
        return config.albumPattern;
    }

    /**
     * @return Mask to use for album directory name
     */
    static String getAlbumMask() {
        return config.albumMask;
    }

    /**
     * @return Frame allowed and mandatory in MP3 tag
     */
    static String[] getTagFrames() {
        return config.tagFrames;
    }

    /**
     * @return true if the MP3 file custom tag is allowed, false otherwise
     */
    static boolean isCustomTagAllowed() {
        return config.customTagAllowed;
    }

    /**
     * @return How the cover file should be named
     */
    static String getCoverFileName() {
        return config.coverFileName;
    }

    /**
     * @return How the old cover file might be named. These names will be used
     * when renaming the cover file to {@link Config#coverFileName}
     */
    static String[] getObsoleteCoverFileName() {
        return config.obsoleteCoverFileName;
    }

    /**
     * @return true if the cover should be processed (convert progressive
     * to baseline, load image to MP3 if different)
     */
    static boolean isProcessCoverEnabled() {
        return config.processCoverEnabled;
    }

    /**
     * @return true if no confirmation is asked to the user, false otherwise
     */
    static boolean isForceEnabled() {
        return config.forceEnabled;
    }

    /**
     * @return true if logs has to be written at the end of the process,
     * false otherwise
     */
    public static boolean isLogEnabled() {
        return config.logEnabled;
    }

    /**
     * @return true if the logs has to be shown in an editor at the end of the
     * process, false otherwise
     */
    public static boolean isShowLogEnabled() {
        return config.showLogEnabled;
    }

    /**
     * @return Artist directory name given in --artist argument
     */
    static String getArtist() {
        return config.artist;
    }

    /**
     * @return Album directory name given in --album argument
     */
    static String getAlbum() {
        return config.album;
    }

    /**
     * Create file from the given parent and the cover filename specified in the
     * configuration.
     *
     * @param parent Parent directory
     * @return Cover file
     */
    static File getCoverFile(final File parent) {
        return new File(parent, getCoverFileName());
    }
}
