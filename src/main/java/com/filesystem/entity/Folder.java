package com.filesystem.entity;

import com.filesystem.enums.EntityType;
import com.filesystem.utils.ValidationUtils;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Defines Folder.
 * Contains other entities and has a parent entity.
 *
 */
public class Folder implements FileSystemContainer {
    private final String name;
    private final Map<String, FileSystemEntity> childEntities;
    private FileSystemContainer parentEntity;

    /**
     * Constructs Folder entity type.
     *
     * @param name Name of the folder.
     */
    public Folder(String name) {
        this.name = ValidationUtils.validateEntityName(name, EntityType.FOLDER.getDisplayName());
        this.childEntities = new HashMap<>();
    }

    @Override
    public EntityType getType() {
        return EntityType.FOLDER;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getPath() {
        Optional.ofNullable(this.parentEntity)
                .orElseThrow(() -> new IllegalStateException("Path invalid for folder: " + getName()));

        return Path.of(this.parentEntity.getPath(), getName()).toString();
    }

    @Override
    public Optional<FileSystemContainer> getParentEntity() {
        return Optional.ofNullable(this.parentEntity);
    }

    @Override
    public void setParentEntity(FileSystemContainer parentEntity) {
        this.parentEntity = parentEntity;
    }

    @Override
    public Map<String, FileSystemEntity> getChildEntities() {
        return childEntities;
    }

    @Override
    public void addChildEntity(FileSystemEntity childEntity) {
        Optional.ofNullable(childEntity)
                .orElseThrow(() -> new NullPointerException("Child entity cannot be null."));

        ValidationUtils.validateEntityName(childEntity.getName(), "Child entity");

        if (this.childEntities.containsKey(childEntity.getName())) {
            throw new IllegalArgumentException("Entity with name: '" + childEntity.getName() +
                    "' already exists in folder: " + this.getName());
        }

        childEntity.getParentEntity().ifPresent(parentContainer -> {
            if (parentContainer != this) {
                parentContainer.removeChildEntity(childEntity.getName());
            }
        });

        this.childEntities.put(childEntity.getName(), childEntity);
        childEntity.setParentEntity(this);
    }

    @Override
    public boolean removeChildEntity(String entityName) {
        FileSystemEntity removedChildEntity = this.childEntities.remove(entityName);
        if (removedChildEntity != null) {
            removedChildEntity.setParentEntity(null);
            return true;
        }

        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Folder folder = (Folder) o;
        try {
            return getPath().equals(folder.getPath());
        } catch (IllegalStateException e) {
            return Objects.equals(name, folder.name) && this.parentEntity == null && folder.parentEntity == null;
        }
    }

    @Override
    public int hashCode() {
        try {
            return Objects.hash(getPath());
        } catch (IllegalStateException e) {
            return Objects.hash(name, parentEntity);
        }
    }
}
