package com.alkisum.java.exceptions;

/**
 * Exception class used when no song is contained in album directory.
 *
 * @author Alkisum
 * @version 1.0
 * @since 1.0
 */
public class EmptyAlbumDirectoryException extends Exception {

    /**
     * NoSongInAlbumException constructor.
     */
    public EmptyAlbumDirectoryException() {
        super();
    }
}
