package com.filesystem.entity;

import com.filesystem.enums.EntityType;
import com.filesystem.utils.ValidationUtils;

import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;

/**
 * Defines Text File which includes text content.
 * Does not contain other entities but has a parent.
 *
 */
public class TextFile implements FileSystemEntity {
    private final String name;
    private String content;
    private FileSystemContainer parentEntity;

    /**
     * Constructs Text File entity type.
     *
     * @param name Name of the text file.
     * @param content Content of the text file.
     */
    public TextFile(String name, String content) {
        this.name = ValidationUtils.validateEntityName(name, EntityType.TEXT_FILE.getDisplayName());
        this.content = content;
    }

    @Override
    public EntityType getType() {
        return EntityType.TEXT_FILE;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getPath() {
        Optional.ofNullable(this.parentEntity)
                .orElseThrow(() -> new IllegalStateException("Path invalid for text-file: " + getName()));

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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TextFile textFile = (TextFile) o;
        try {
            return getPath().equals(textFile.getPath()) && Objects.equals(content, textFile.content);
        } catch (IllegalStateException e) {
            return Objects.equals(name, textFile.name) && Objects.equals(content, textFile.content)
                    && this.parentEntity == null && textFile.parentEntity == null;
        }
    }

    @Override
    public int hashCode() {
        try {
            return Objects.hash(getPath(), content);
        } catch (IllegalStateException e) {
            return Objects.hash(name, parentEntity, content);
        }
    }
}
