[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=udhayapps_file-system-operations&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=udhayapps_file-system-operations)
[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=udhayapps_file-system-operations&metric=bugs)](https://sonarcloud.io/summary/new_code?id=udhayapps_file-system-operations)
[![Code Smells](https://sonarcloud.io/api/project_badges/measure?project=udhayapps_file-system-operations&metric=code_smells)](https://sonarcloud.io/summary/new_code?id=udhayapps_file-system-operations)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=udhayapps_file-system-operations&metric=coverage)](https://sonarcloud.io/summary/new_code?id=udhayapps_file-system-operations)

# 📁 File System Operations in Java

A Java-based implementation of a simple hierarchy-based file system that supports operations like creating, deleting, moving, and writing to entities such as **Drives**, **Folders**, **Text Files**, and **Zip Files**.

---

## 🚀 Features

- Supports four types of entities: Drives, Folders, Text Files, and Zip Files.
- Entities can be created, deleted, moved, text content can be written to other text files.
- Implemented validation for entity names.
- Exception handling for file system operations.
- Tested using **JUnit 5** with **AssertJ**.
- Assumptions (for brevity):
  - No colon required after drive name (ex: C is valid instead of C:) for path handling.
  - Allow dot(.) in entity name to handle .txt/.zip file.
  
---

## 🗂️ Supported Entities

| Entity      | Description                                                      |
|-------------|------------------------------------------------------------------|
| **Drive**   | The root-level container.                                        |
| **Folder**  | A container which may hold other entities like files or folders. |
| **TextFile**| Represents a `.txt` file with editable textual content.          |
| **ZipFile** | Represents a `.zip` file. Can contain other entities.            |

---

## 🔧 Available Operations

The `FileSystemService` class provides the following core operations:

1. **Create an Entity**
   ```java
   FileSystemEntity create(EntityType type, String name, String parentPath)
   ```

2. **Delete an Entity**
   ```java
   void delete(String path)
   ```

3. **Move an Entity**
   ```java
   void move(String sourcePath, String destinationParentPath)
   ```

4. **Write Content to a Text File**
   ```java
   void writeToFile(String path, String content)
   ```

---

## 📦 Project Structure

```
file-system-operations/
├── README.md
├── LICENSE
├── pom.xml
└── src/
    ├── main/
    │   └── java/
    │       └── com/
    │           └── filesystem/
    │               ├── FileSystemService.java
    │               ├── entity/
    │               │   ├── Drive.java
    │               │   ├── Folder.java
    │               │   ├── TextFile.java
    │               │   ├── ZipFile.java
    │               │   └── FileSystemContainer.java
    │               ├── enums/
    │               │   └── EntityType.java
    │               ├── exception/
    │               │   ├── FileSystemException.java
    │               │   ├── IllegalFileSystemOperationException.java
    │               │   ├── NotATextFileException.java
    │               │   ├── PathAlreadyExistsException.java
    │               │   └── PathNotFoundException.java
    │               └── utils/
    │                   └── ValidationUtils.java
    └── test/
        └── java/
            └── com/
                └── filesystem/
                    ├── FileSystemServiceTest.java
                    └── utils/
                        └── ValidationUtilsTest.java
```

---

## ⚡ GitHub Action for SonarCloud Analysis
https://github.com/udhayapps/file-system-operations/actions

## 🌀 SonarCloud Dashboard
https://sonarcloud.io/project/information?id=udhayapps_file-system-operations

---

## 🧪 To run tests using Maven
```bash
  mvn clean compile test
```
---
## 📝 Example Usage

```java
FileSystemService fsService = new FileSystemService();

// Create a Drive
fsService.create(EntityType.DRIVE, "C", null);

// Create a Folder inside Drive
fsService.create(EntityType.FOLDER, "Documents", "C");

// Create a Text File
fsService.create(EntityType.TEXT_FILE, "note.txt", "C" + FileSystems.getDefault().getSeparator() + "Documents");

// Write to Text File
fsService.writeToFile("C/Documents/note.txt", "Hello World!");

// Move the Text File to Drive
fsService.move("C/Documents/note.txt", "C");
```

---
## 🛡️ Exceptions Thrown

| Exception | When thrown                                                                   |
|----------|-------------------------------------------------------------------------------|
| `PathNotFoundException` | When trying to access a non-existent path or parent.                          |
| `PathAlreadyExistsException` | When attempting to create an entity with a duplicate name in the same folder. |
| `IllegalFileSystemOperationException` | For invalid operations like moving a drive or orphaned entities.              |
| `NotATextFileException` | When trying to write to a non-text file (ex: .zip file).                      |
| `NullPointerException` | Throws on null inputs for paths or content.                                   |
| `IllegalArgumentException` | Throws on invalid entity names.                                               |

---
## 📋 License

MIT License – see [LICENSE](LICENSE)

> Copyright © 2025 Udhay Rajendran

---
