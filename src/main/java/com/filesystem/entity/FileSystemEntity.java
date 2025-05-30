package com.filesystem.entity;

import com.filesystem.enums.EntityType;

import java.util.Optional;

/**
 * File system interface providing generic methods to be implemented by file system entities.
 */
public interface FileSystemEntity {

    /**
     * @return Type of the entity.
     */
    EntityType getType();

    /**
     * @return Name of the entity (should be alphanumeric).
     */
    String getName();

    /**
     * @return Complete path of the entity.
     */
    String getPath();

    /**
     * @return Parent container of the entity. Return Optional empty if the entity type is a drive.
     */
    Optional<FileSystemContainer> getParentEntity();

    /**
     * @param parentEntity Parent container of the entity.
     */
    void setParentEntity(FileSystemContainer parentEntity);
}
