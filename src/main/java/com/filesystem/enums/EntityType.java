package com.filesystem.enums;

/**
 * Entity types in the file system.
 *
 */
public enum EntityType {
    DRIVE("Drive"),
    FOLDER("Folder"),
    TEXT_FILE("Text File"),
    ZIP_FILE("Zip File");

    private final String displayName;

    EntityType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
