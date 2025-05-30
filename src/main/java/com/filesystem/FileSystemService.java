package com.filesystem;

import com.filesystem.entity.*;
import com.filesystem.enums.EntityType;
import com.filesystem.exception.IllegalFileSystemOperationException;
import com.filesystem.exception.NotATextFileException;
import com.filesystem.exception.PathAlreadyExistsException;
import com.filesystem.exception.PathNotFoundException;
import com.filesystem.utils.ValidationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.FileSystems;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * File System Service to create, delete, move and write to files.
 */
public class FileSystemService {
    private static final Logger log = LoggerFactory.getLogger(FileSystemService.class);
    private static final String PATH_SEPARATOR = FileSystems.getDefault().getSeparator();
    private final Map<String, Drive> drives;

    /**
     * Constructs FileSystemService.
     */
    public FileSystemService() {
        this.drives = new HashMap<>();
    }

    /**
     * Find an entity based on a path.
     *
     * @param path Path to find entity.
     * @return Optional entity. Empty if a path is not found or is not an entity.
     */
    private Optional<FileSystemEntity> findEntity(String path) {
        log.info("Finding Entity for Path: {}", path);
        Objects.requireNonNull(path, "Path cannot be null.");

        if (path.isEmpty()) {
            log.debug("Entity not found for Path: {}", path);
            return Optional.empty();
        }

        // Split path into drive name and path segments using a system-specific separator
        String[] pathParts = path.split(PATH_SEPARATOR);
        String driveName = pathParts[0];
        Drive drive = drives.get(driveName);

        if (drive == null || pathParts.length == 1) {
            log.debug("Entity not found for Path: {}", path);
            return Optional.ofNullable(drive);
        }

        FileSystemEntity currentEntity = drive;
        for (int i = 1; i < pathParts.length; i++) {
            String segmentName = pathParts[i];
            if (currentEntity instanceof FileSystemContainer container) {
                Optional<FileSystemEntity> childEntity = container.getChildEntity(segmentName);

                // If no path segment found, return empty optional
                if (childEntity.isEmpty()) {
                    return Optional.empty();
                }
                currentEntity = childEntity.get();
            } else {
                // If the path segment is not a container, return empty optional
                return Optional.empty();
            }
        }

        log.info("Found Entity for Path: {}", path);
        return Optional.of(currentEntity);
    }

    /**
     * Find a container based on a file system entity path.
     *
     * @param path Path to find container.
     * @return Optional container. Empty if a path is not found or is not a container.
     */
    private Optional<FileSystemContainer> findContainer(String path) {
        log.info("Finding Container for Path: {}", path);
        return findEntity(path)
                .filter(FileSystemContainer.class::isInstance)
                .map(FileSystemContainer.class::cast);
    }

    /**
     * Creates a new entity in the file system.
     *
     * @param type       Entity type.
     * @param name       Entity name.
     * @param parentPath Path of the parent container. If null or empty, creates an entity in drive.
     * @return Created entity. If a parent path is not specified, returns the drive entity.
     * @throws PathNotFoundException               Throws if a parent path is not found or is not a container.
     * @throws PathAlreadyExistsException          Throws if entity with same name already exists in the parent container.
     * @throws IllegalFileSystemOperationException Throws if an entity type is not supported or if the drive cannot have a parent path.
     */
    public FileSystemEntity create(EntityType type, String name, String parentPath)
            throws PathNotFoundException, PathAlreadyExistsException, IllegalFileSystemOperationException {
        log.info("Creating Entity: {} of Type: {} with Parent-Path: {}", name, type, parentPath);
        Objects.requireNonNull(type, "Entity type cannot be null");
        ValidationUtils.validateEntityName(name, type.getDisplayName());

        if (type == EntityType.DRIVE) {
            return createDrive(name, parentPath);
        }

        if (parentPath == null || parentPath.isEmpty()) {
            log.error("Parent path must be specified for non-drive entities.");
            throw new PathNotFoundException("Parent path must be specified for non-drive entities.");
        }

        return createNonDriveEntity(type, name, parentPath);
    }

    /**
     * Creates a new drive in the file system.
     *
     * @param name       Entity name.
     * @param parentPath Path of the parent container.
     * @return Returns the created drive.
     * @throws IllegalFileSystemOperationException Throws if a drive cannot have a parent path.
     * @throws PathAlreadyExistsException          Throws if drive with the same name already exists in the file system.
     */
    private Drive createDrive(String name, String parentPath) throws IllegalFileSystemOperationException, PathAlreadyExistsException {
        log.info("Creating Drive: {} with Parent-Path: {}", name, parentPath);

        if (parentPath != null && !parentPath.isEmpty()) {
            log.error("Drive cannot have a parent path.");
            throw new IllegalFileSystemOperationException("Drive cannot have parent path.");
        }

        if (drives.containsKey(name)) {
            log.error("Drive with name: {} already exists.", name);
            throw new PathAlreadyExistsException(name + PATH_SEPARATOR + "Drive");
        }

        // Create a new Drive entity and add it to drives map.
        Drive drive = new Drive(name);
        drives.put(name, drive);
        return drive;
    }

    /**
     * Creates a new non-drive entity in the file system.
     *
     * @param type       Entity type.
     * @param name       Entity name.
     * @param parentPath Path of the parent container.
     * @return Returns the created entity.
     * @throws PathNotFoundException      Throws if a parent path is not found or is not a container.
     * @throws PathAlreadyExistsException Throws if an entity with the same name already exists in the parent container.
     */
    private FileSystemEntity createNonDriveEntity(EntityType type, String name, String parentPath) throws PathNotFoundException, PathAlreadyExistsException {
        log.info("Creating Entity: {} of Type: {} with Parent-Path: {}", name, type, parentPath);

        FileSystemContainer parentContainer = findContainer(parentPath)
            .orElseThrow(() -> {
                log.error("Parent path not found or is not a container: {}", parentPath);
                return new PathNotFoundException("Parent path not found or is not a container: " + parentPath);
            });

        if (parentContainer.getChildEntity(name).isPresent()) {
            log.error("Entity with name: {} already exists in parent: {}", name, parentPath);
            throw new PathAlreadyExistsException(parentContainer.getPath() + PATH_SEPARATOR + name);
        }

        // Create a new entity based on the entity type.
        FileSystemEntity newEntity = switch (type) {
            case FOLDER -> new Folder(name);
            case TEXT_FILE -> new TextFile(name, "");
            case ZIP_FILE -> new ZipFile(name);
            default -> throw new IllegalFileSystemOperationException("Unsupported entity type: " + type);
        };

        parentContainer.addChildEntity(newEntity);
        return newEntity;
    }

    /**
     * Deletes an entity from the file system.
     *
     * @param path Path of the entity to delete. If a path is a drive, deletes the drive.
     * @throws PathNotFoundException Throws if a path is not found or is not an entity.
     * @throws IllegalFileSystemOperationException Throws if the entity is a Drive or if the entity is an orphaned non-drive entity.
     */
    public void delete(String path) throws PathNotFoundException, IllegalFileSystemOperationException {
        log.info("Deleting Entity on Path: {}", path);
        Objects.requireNonNull(path, "Path cannot be null");

        FileSystemEntity entityToDelete = findEntity(path)
            .orElseThrow(() -> {
                log.error("Path not found: {}", path);
                return new PathNotFoundException(path);
            });

        // Delete Drive entity
        if (entityToDelete.getType() == EntityType.DRIVE) {
            log.info("Deleting Drive: {}", entityToDelete.getName());

            Drive driveToDelete = (Drive) entityToDelete;
            drives.remove(driveToDelete.getName());

        // Delete non-drive entity
        } else {
            log.info("Deleting Entity: {} of Type: {}", entityToDelete.getName(), entityToDelete.getType());

            FileSystemContainer parentContainer = entityToDelete.getParentEntity()
                .orElseThrow(() -> {
                    log.error("Entity: {} cannot be deleted from parent", path);
                    return new IllegalFileSystemOperationException("Entity: " + path + " cannot be deleted from parent");
                });

            log.info("Removing Entity: {} from Parent: {}", path, parentContainer.getPath());
            boolean removedChildEntity = parentContainer.removeChildEntity(entityToDelete.getName());

            // If unable to remove a child entity, throw an exception.
            if (!removedChildEntity) {
                log.error("Failed to remove entity: {} from parent: {}", path, parentContainer.getPath());
                throw new IllegalFileSystemOperationException("Failed to remove entity: " + path + " from parent: " + parentContainer.getPath());
            }
        }
    }

    /**
     * Moves an entity from one location to another.
     *
     * @param sourcePath            Path of the entity to move.
     * @param destinationParentPath Path of the parent container to move the entity to.
     * @throws PathNotFoundException               Throws if a path is not found or is not an entity.
     * @throws PathAlreadyExistsException          Throws if an entity with the same name already exists in the destination parent container.
     * @throws IllegalFileSystemOperationException Throws if the source entity is a Drive or if the source entity is an orphaned non-drive entity.
     */
    public void move(String sourcePath, String destinationParentPath) throws PathNotFoundException, PathAlreadyExistsException, IllegalFileSystemOperationException {
        log.info("Moving Entity from Source-Path: {} to Destination-Path: {}", sourcePath, destinationParentPath);

        Objects.requireNonNull(sourcePath, "Source Path cannot be null");
        Objects.requireNonNull(destinationParentPath, "Destination Path cannot be null");

        FileSystemEntity sourceEntity = findEntity(sourcePath)
            .orElseThrow(() -> {
                log.error("Source path not found: {}", sourcePath);
                return new PathNotFoundException("Source path not found: " + sourcePath);
            });

        FileSystemContainer destinationParent = findContainer(destinationParentPath)
            .orElseThrow(() -> {
                log.error("Destination path not found: {}", destinationParentPath);
                return new PathNotFoundException("Destination path not found: " + destinationParentPath);
            });

        if (sourceEntity.getType() == EntityType.DRIVE) {
            log.error("Cannot move a Drive.");
            throw new IllegalFileSystemOperationException("Cannot move a Drive.");
        }

        FileSystemContainer sourceParent = sourceEntity.getParentEntity()
            .orElseThrow(() -> {
                log.error("Source entity: {} has no parent and cannot be moved.", sourcePath);
                return new IllegalFileSystemOperationException("Source entity: " + sourcePath + " has no parent and cannot be moved.");
            });

        // Return if attempt moving to the same parent
        if (sourceParent.getPath().equals(destinationParent.getPath())) {
            log.info("Source entity: {} is already in destination parent: {}", sourcePath, destinationParentPath);
            return;
        }

        // Perform moving entity from source to destination
        performMove(sourceEntity, sourceParent, destinationParent, sourcePath, destinationParentPath);
    }

    /**
     * Moves an entity from one location to another after validations
     *
     * @param sourceEntity          Entity to move.
     * @param sourceParent          Parent of the entity to move.
     * @param destinationParent     Destination parent of the entity.
     * @param sourcePath            Path of the entity to move.
     * @param destinationParentPath Path of the parent container to move the entity to.
     * @throws IllegalFileSystemOperationException Throws if failed to detach or move the entity.
     */
    private void performMove(FileSystemEntity sourceEntity, FileSystemContainer sourceParent, FileSystemContainer destinationParent,
                             String sourcePath, String destinationParentPath) throws IllegalFileSystemOperationException {
        log.info("Moving Entity: {} from Source-Path: {} to Destination-Path: {}", sourceEntity.getName(), sourcePath, destinationParentPath);
        boolean removed = sourceParent.removeChildEntity(sourceEntity.getName());

        if (!removed) {
            log.error("Failed to detach source entity: {} from parent: {}", sourcePath, sourceParent.getPath());
            throw new IllegalFileSystemOperationException("Failed to detach source entity: " + sourcePath + " from parent: " + sourceParent.getPath());
        }

        try {
            destinationParent.addChildEntity(sourceEntity);
        } catch (Exception e) {
            log.error("Failed to move entity: {} to: {}", sourcePath, destinationParentPath);
            throw new IllegalFileSystemOperationException("Failed to move entity: " + sourcePath + " to: " + destinationParentPath);
        }
    }

    /**
     * Write content to a file
     *
     * @param path    File path
     * @param content Content is applicable for a text-file only
     * @throws PathNotFoundException Throws if a path is not found
     * @throws NotATextFileException Throws if a file is not a text-file
     */
    public void writeToFile(String path, String content) throws PathNotFoundException, NotATextFileException {
        log.info("Writing content to file on path: {} with content: {}", path, content);
        Objects.requireNonNull(path, "Path cannot be null");
        Objects.requireNonNull(content, "Content cannot be null");

        FileSystemEntity entity = findEntity(path)
            .orElseThrow(() -> {
                log.error("Path not found: {}", path);
                return new PathNotFoundException(path);
            });

        if (!(entity instanceof TextFile textFile)) {
            throw new NotATextFileException(path);
        }

        textFile.setContent(content);
    }
}
