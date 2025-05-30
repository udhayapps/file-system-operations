package com.filesystem.entity;

import java.util.Map;
import java.util.Optional;

/**
 * File system container interface holds other entities.
 */
public interface FileSystemContainer extends FileSystemEntity {

    /**
     * @return Map of child entities keyed by child name.
     * Empty map when there are no children.
     */
    Map<String, FileSystemEntity> getChildEntities();

    /**
     * Fetches a child entity by name.
     *
     * @param entityName Name of the child entity.
     * @return Optional containing the child entity else empty Optional.
     */
    default Optional<FileSystemEntity> getChildEntity(String entityName) {
        return Optional.ofNullable(getChildEntities().get(entityName));
    }

    /**
     * Adds a child entity to a parent entity.
     *
     * @param childEntity File system entity to add.
     */
    void addChildEntity(FileSystemEntity childEntity);

    /**
     * Removes a child entity.
     *
     * @param entityName Name of the child entity.
     * @return True if a child entity is removed and False when failed to remove.
     */
    boolean removeChildEntity(String entityName);
}
