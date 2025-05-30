package com.filesystem.exception;

public class NotATextFileException extends FileSystemException {

    public NotATextFileException(String path) {
        super("Not a text file: " + path);
    }
}
