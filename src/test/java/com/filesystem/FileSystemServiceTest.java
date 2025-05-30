package com.filesystem;

import com.filesystem.entity.*;
import com.filesystem.enums.EntityType;
import com.filesystem.exception.IllegalFileSystemOperationException;
import com.filesystem.exception.NotATextFileException;
import com.filesystem.exception.PathAlreadyExistsException;
import com.filesystem.exception.PathNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

import java.nio.file.FileSystems;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

class FileSystemServiceTest {

    private static final String PATH_SEPARATOR = FileSystems.getDefault().getSeparator();
    private static final String C_DRIVE = "C";
    private static final String DOCUMENTS_FOLDER = "Documents";
    private static final String TEXT_FILE = "note.txt";
    private static final String ZIP_FILE = "files.zip";
    private static final String DOCUMENTS_PATH = C_DRIVE + PATH_SEPARATOR + DOCUMENTS_FOLDER;
    private static final String TEXT_FILE_PATH = C_DRIVE + PATH_SEPARATOR + DOCUMENTS_FOLDER + PATH_SEPARATOR + TEXT_FILE;
    private static final String ZIP_FILE_PATH = C_DRIVE + PATH_SEPARATOR + DOCUMENTS_FOLDER + PATH_SEPARATOR + ZIP_FILE;

    private FileSystemService fileSystemService;

    @BeforeEach
    void setUp() {
        fileSystemService = new FileSystemService();
    }

    @Nested
    class CreateTests {
        @Test
        void testCreateDrive() {
            FileSystemEntity drive = fileSystemService.create(EntityType.DRIVE, C_DRIVE, null);

            assertNotNull(drive);
            assertEquals(EntityType.DRIVE, drive.getType());
            assertEquals(C_DRIVE, drive.getName());
            assertEquals(C_DRIVE, drive.getPath());
            assertTrue(drive.getParentEntity().isEmpty());
        }

        @Test
        void testCreateDrive_WithParentPath_ThrowsException() {
            assertThatThrownBy(() -> fileSystemService.create(EntityType.DRIVE, "D", C_DRIVE))
                .isInstanceOf(IllegalFileSystemOperationException.class)
                .hasMessage("Drive cannot have parent path.");
        }

        @Test
        void testCreateDrive_Duplicate_ThrowsException() {
            fileSystemService.create(EntityType.DRIVE, C_DRIVE, null);

            assertThatThrownBy(() -> fileSystemService.create(EntityType.DRIVE, C_DRIVE, null))
                .isInstanceOf(PathAlreadyExistsException.class)
                .hasMessage("Path already exists for: C" + PATH_SEPARATOR + "Drive");
        }

        @Test
        void testCreateFolder_InDrive() {
            fileSystemService.create(EntityType.DRIVE, C_DRIVE, null);

            FileSystemEntity folder = fileSystemService.create(EntityType.FOLDER, DOCUMENTS_FOLDER, C_DRIVE);

            assertNotNull(folder);
            assertEquals(EntityType.FOLDER, folder.getType());
            assertEquals(DOCUMENTS_FOLDER, folder.getName());
            assertEquals(DOCUMENTS_PATH, folder.getPath());
            assertTrue(folder.getParentEntity().isPresent());
            assertEquals(C_DRIVE, folder.getParentEntity().get().getName());
        }

        @Test
        void testCreateTextFile_InFolder() {
            fileSystemService.create(EntityType.DRIVE, C_DRIVE, null);
            fileSystemService.create(EntityType.FOLDER, DOCUMENTS_FOLDER, C_DRIVE);

            FileSystemEntity textFile = fileSystemService.create(EntityType.TEXT_FILE, TEXT_FILE, DOCUMENTS_PATH);

            assertNotNull(textFile);
            assertEquals(EntityType.TEXT_FILE, textFile.getType());
            assertEquals(TEXT_FILE, textFile.getName());
            assertEquals(TEXT_FILE_PATH, textFile.getPath());
            assertTrue(textFile.getParentEntity().isPresent());
            assertEquals(DOCUMENTS_FOLDER, textFile.getParentEntity().get().getName());
            assertEquals("", ((TextFile) textFile).getContent());
        }

        @Test
        void testCreateZipFile_InFolder() {
            fileSystemService.create(EntityType.DRIVE, C_DRIVE, null);
            fileSystemService.create(EntityType.FOLDER, DOCUMENTS_FOLDER, C_DRIVE);

            FileSystemEntity zipFile = fileSystemService.create(EntityType.ZIP_FILE, ZIP_FILE, DOCUMENTS_PATH);

            assertNotNull(zipFile);
            assertEquals(EntityType.ZIP_FILE, zipFile.getType());
            assertEquals(ZIP_FILE, zipFile.getName());
            assertEquals(ZIP_FILE_PATH, zipFile.getPath());
            assertTrue(zipFile.getParentEntity().isPresent());
            assertEquals(DOCUMENTS_FOLDER, zipFile.getParentEntity().get().getName());
        }

        @Test
        void testCreateEntity_WithNonExistentParentPath_ThrowsException() {
            assertThatThrownBy(() -> fileSystemService.create(EntityType.FOLDER, DOCUMENTS_FOLDER, C_DRIVE))
                .isInstanceOf(PathNotFoundException.class)
                .hasMessage("Path not found: Parent path not found or is not a container: C");
        }

        @Test
        void testCreateEntity_WithNullParentPath_ThrowsException() {
            assertThatThrownBy(() -> fileSystemService.create(EntityType.FOLDER, DOCUMENTS_FOLDER, null))
                .isInstanceOf(PathNotFoundException.class)
                .hasMessage("Path not found: Parent path must be specified for non-drive entities.");
        }

        @Test
        void testCreateDuplicateEntity_InSameParent_ThrowsException() {
            fileSystemService.create(EntityType.DRIVE, C_DRIVE, null);
            fileSystemService.create(EntityType.FOLDER, DOCUMENTS_FOLDER, C_DRIVE);

            assertThatThrownBy(() -> fileSystemService.create(EntityType.FOLDER, DOCUMENTS_FOLDER, C_DRIVE))
                .isInstanceOf(PathAlreadyExistsException.class)
                .hasMessage("Path already exists for: " + DOCUMENTS_PATH);
        }
    }

    @Nested
    class DeleteTests {
        @Test
        void testDelete_Drive() {
            fileSystemService.create(EntityType.DRIVE, C_DRIVE, null);

            fileSystemService.delete(C_DRIVE);

            assertThatThrownBy(() -> fileSystemService.create(EntityType.FOLDER, DOCUMENTS_FOLDER, C_DRIVE))
                .isInstanceOf(PathNotFoundException.class)
                .hasMessage("Path not found: Parent path not found or is not a container: C");
        }

        @Test
        void testDelete_Folder() {
            fileSystemService.create(EntityType.DRIVE, C_DRIVE, null);
            fileSystemService.create(EntityType.FOLDER, DOCUMENTS_FOLDER, C_DRIVE);

            fileSystemService.delete(DOCUMENTS_PATH);

            assertThatThrownBy(() -> fileSystemService.create(EntityType.FOLDER, "Pictures", DOCUMENTS_PATH))
                .isInstanceOf(PathNotFoundException.class)
                .hasMessage("Path not found: Parent path not found or is not a container: " + DOCUMENTS_PATH);
        }

        @Test
        void testDelete_TextFile() {
            fileSystemService.create(EntityType.DRIVE, C_DRIVE, null);
            fileSystemService.create(EntityType.FOLDER, DOCUMENTS_FOLDER, C_DRIVE);
            fileSystemService.create(EntityType.TEXT_FILE, TEXT_FILE, DOCUMENTS_PATH);

            fileSystemService.delete(TEXT_FILE_PATH);

            assertThatThrownBy(() -> fileSystemService.writeToFile(TEXT_FILE_PATH, "Some content"))
                .isInstanceOf(PathNotFoundException.class)
                .hasMessage("Path not found: " + TEXT_FILE_PATH);
        }

        @Test
        void testDelete_NonExistentPath_ThrowsException() {
            assertThatThrownBy(() -> fileSystemService.delete("C"))
                .isInstanceOf(PathNotFoundException.class)
                .hasMessage("Path not found: C");
        }

        @Test
        void testDelete_NullPath_ThrowsException() {
            assertThatThrownBy(() -> fileSystemService.delete(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    class MoveTests {
        @Test
        void testMove_Folder() {
            final String PICTURES_PATH = C_DRIVE + PATH_SEPARATOR + "Pictures";
            final String ALBUMS_PATH = PICTURES_PATH + PATH_SEPARATOR + "Albums";
            final String MOVED_PATH = DOCUMENTS_PATH + PATH_SEPARATOR + "Albums";

            fileSystemService.create(EntityType.DRIVE, C_DRIVE, null);
            fileSystemService.create(EntityType.FOLDER, DOCUMENTS_FOLDER, C_DRIVE);
            fileSystemService.create(EntityType.FOLDER, "Pictures", C_DRIVE);
            fileSystemService.create(EntityType.FOLDER, "Albums", PICTURES_PATH);

            fileSystemService.move(ALBUMS_PATH, DOCUMENTS_PATH);

            assertThatThrownBy(() -> fileSystemService.create(EntityType.TEXT_FILE, "itinerary.txt", ALBUMS_PATH))
                .isInstanceOf(PathNotFoundException.class)
                .hasMessage("Path not found: Parent path not found or is not a container: " + ALBUMS_PATH);

            FileSystemEntity movedFolder = fileSystemService.create(EntityType.TEXT_FILE, "itinerary.txt", MOVED_PATH);
            assertNotNull(movedFolder);
        }

        @Test
        void testMove_TextFile() {
            fileSystemService.create(EntityType.DRIVE, C_DRIVE, null);
            fileSystemService.create(EntityType.FOLDER, DOCUMENTS_FOLDER, C_DRIVE);
            fileSystemService.create(EntityType.FOLDER, "Backup", C_DRIVE);
            fileSystemService.create(EntityType.TEXT_FILE, TEXT_FILE, DOCUMENTS_PATH);
            fileSystemService.writeToFile(TEXT_FILE_PATH, "Some content");

            fileSystemService.move(TEXT_FILE_PATH, "C" + PATH_SEPARATOR + "Backup");

            assertThatThrownBy(() -> fileSystemService.writeToFile(TEXT_FILE_PATH, "New content"))
                .isInstanceOf(PathNotFoundException.class)
                .hasMessage("Path not found: " + TEXT_FILE_PATH);

            // Verify the file was moved with its content
            fileSystemService.writeToFile("C" + PATH_SEPARATOR + "Backup" + PATH_SEPARATOR + "note.txt", "New content");
        }

        @Test
        void testMove_Drive_ThrowsException() {
            fileSystemService.create(EntityType.DRIVE, C_DRIVE, null);
            fileSystemService.create(EntityType.DRIVE, "D", null);
            fileSystemService.create(EntityType.FOLDER, "Backup", "D");

            assertThatThrownBy(() -> fileSystemService.move(C_DRIVE, "D" + PATH_SEPARATOR + "Backup"))
                .isInstanceOf(IllegalFileSystemOperationException.class)
                .hasMessage("Cannot move a Drive.");
        }

        @Test
        void testMove_ToNonExistentDestination_ThrowsException() {
            fileSystemService.create(EntityType.DRIVE, C_DRIVE, null);
            fileSystemService.create(EntityType.FOLDER, DOCUMENTS_FOLDER, C_DRIVE);
            fileSystemService.create(EntityType.TEXT_FILE, TEXT_FILE, DOCUMENTS_PATH);

            // C/Backup is a non-existent destination path, should throw PathNotFoundException
            assertThatThrownBy(() -> fileSystemService.move(TEXT_FILE_PATH, "C/Backup"))
                .isInstanceOf(PathNotFoundException.class)
                .hasMessage("Path not found: Destination path not found: C/Backup");
        }

        @Test
        void testMove_NonExistentSource_ThrowsException() {
            fileSystemService.create(EntityType.DRIVE, C_DRIVE, null);
            fileSystemService.create(EntityType.FOLDER, DOCUMENTS_FOLDER, C_DRIVE);

            // C/Documents/note.txt is a non-existent source path, should throw PathNotFoundException
            assertThatThrownBy(() -> fileSystemService.move(TEXT_FILE_PATH, C_DRIVE))
                .isInstanceOf(PathNotFoundException.class)
                .hasMessage("Path not found: Source path not found: " +  TEXT_FILE_PATH);
        }

        @Test
        void testMove_ToSameParentDoesNothing() {
            fileSystemService.create(EntityType.DRIVE, C_DRIVE, null);
            fileSystemService.create(EntityType.FOLDER, DOCUMENTS_FOLDER, C_DRIVE);
            fileSystemService.create(EntityType.TEXT_FILE, TEXT_FILE, DOCUMENTS_PATH);
            fileSystemService.writeToFile(TEXT_FILE_PATH, "Some content");

            fileSystemService.move(TEXT_FILE_PATH, DOCUMENTS_PATH);

            // Do nothing / does not throw exception
            assertDoesNotThrow(() -> fileSystemService.writeToFile(TEXT_FILE_PATH, "Updated content"));
        }
    }

    @Nested
    class WriteToFileTests {
        @Test
        void testWriteTo_TextFile() {
            fileSystemService.create(EntityType.DRIVE, C_DRIVE, null);
            fileSystemService.create(EntityType.FOLDER, DOCUMENTS_FOLDER, C_DRIVE);
            FileSystemEntity textFile = fileSystemService.create(EntityType.TEXT_FILE, TEXT_FILE, DOCUMENTS_PATH);

            fileSystemService.writeToFile(TEXT_FILE_PATH, "Some content");

            assertEquals("Some content", ((TextFile) textFile).getContent());
        }

        @Test
        void testWriteTo_NonExistentFile_ThrowsException() {
            fileSystemService.create(EntityType.DRIVE, C_DRIVE, null);
            fileSystemService.create(EntityType.FOLDER, DOCUMENTS_FOLDER, C_DRIVE);

            assertThatThrownBy(() -> fileSystemService.writeToFile(TEXT_FILE_PATH, "Some content"))
                .isInstanceOf(PathNotFoundException.class)
                .hasMessage("Path not found: " + TEXT_FILE_PATH);
        }

        @Test
        void testWriteTo_Folder_ThrowsException() {
            fileSystemService.create(EntityType.DRIVE, C_DRIVE, null);
            fileSystemService.create(EntityType.FOLDER, DOCUMENTS_FOLDER, C_DRIVE);

            assertThatThrownBy(() -> fileSystemService.writeToFile(DOCUMENTS_PATH, "Some content"))
                .isInstanceOf(NotATextFileException.class)
                .hasMessage("Not a text file: " + DOCUMENTS_PATH);
        }

        @Test
        void testWriteTo_ZipFile_ThrowsException() {
            fileSystemService.create(EntityType.DRIVE, C_DRIVE, null);
            fileSystemService.create(EntityType.FOLDER, DOCUMENTS_FOLDER, C_DRIVE);
            fileSystemService.create(EntityType.ZIP_FILE, ZIP_FILE, DOCUMENTS_PATH);

            assertThatThrownBy(() -> fileSystemService.writeToFile(ZIP_FILE_PATH, "Some content"))
                .isInstanceOf(NotATextFileException.class)
                .hasMessage("Not a text file: " + ZIP_FILE_PATH);
        }

        @Test
        void testWriteToFile_WithNullPath_ThrowsException() {
            assertThatThrownBy(() -> fileSystemService.writeToFile(null, "Some content"))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        void testWriteToFile_WithNullContent_ThrowsException() {
            fileSystemService.create(EntityType.DRIVE, C_DRIVE, null);
            fileSystemService.create(EntityType.FOLDER, DOCUMENTS_FOLDER, C_DRIVE);
            fileSystemService.create(EntityType.TEXT_FILE, TEXT_FILE, DOCUMENTS_PATH);

            assertThatThrownBy(() -> fileSystemService.writeToFile(TEXT_FILE_PATH, null))
                .isInstanceOf(NullPointerException.class);
        }
    }
}
