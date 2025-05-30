package com.filesystem.entity;

import com.filesystem.enums.EntityType;
import com.filesystem.utils.ValidationUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Defines Drive as root in the file system with no parent.
 */
public class Drive implements FileSystemContainer {
    private final String name;
    private final String path;
    private final Map<String, FileSystemEntity> childEntities;

    /**
     * Constructs Drive entity type.
     *
     * @param name Name of the drive.
     */
    public Drive(String name) {
        this.name = ValidationUtils.validateEntityName(name, EntityType.DRIVE.getDisplayName());
        this.path = this.name;
        this.childEntities = new HashMap<>();
    }

    @Override
    public EntityType getType() {
        return EntityType.DRIVE;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getPath() {
        return path;
    }

    /**
     * Drive entity type does not have a parent.
     *
     * @return an empty {@code Optional}.
     */
    @Override
    public Optional<FileSystemContainer> getParentEntity() {
        return Optional.empty();
    }

    @Override
    public void setParentEntity(FileSystemContainer parentEntity) {
        if (parentEntity != null) {
            throw new IllegalArgumentException("Drive cannot be contained in another drive.");
        }
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

        if (childEntity.getType() == EntityType.DRIVE) {
            throw new IllegalArgumentException("Child entity cannot be a drive. Drive cannot contain drives.");
        }

        if (this.childEntities.containsKey(childEntity.getName())) {
            throw new IllegalArgumentException("Entity with name: " + childEntity.getName() + " already exists in drive: " + this.getName());
        }

        childEntity.getParentEntity().ifPresent(parentEntity -> {
            if (parentEntity != this) {
                parentEntity.removeChildEntity(childEntity.getName());
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

        Drive drive = (Drive) o;
        return getPath().equals(drive.getPath());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getPath());
    }
}
