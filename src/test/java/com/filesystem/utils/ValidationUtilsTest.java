package com.filesystem.utils;

import com.filesystem.enums.EntityType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ValidationUtilsTest {

    @Test
    void testNullEntityName_ThrowsNullPointerException() {
        String entityTypeDisplayName = EntityType.DRIVE.getDisplayName();
        assertThatThrownBy(() -> ValidationUtils.validateEntityName(null, entityTypeDisplayName))
            .isInstanceOf(NullPointerException.class)
            .hasMessageContaining(EntityType.DRIVE.getDisplayName() + " name cannot be null.");
    }

    @Test
    void testEmptyEntityName_ThrowsIllegalArgumentException() {
        String entityTypeDisplayName = EntityType.DRIVE.getDisplayName();
        assertThatThrownBy(() -> ValidationUtils.validateEntityName("", entityTypeDisplayName))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining(EntityType.DRIVE.getDisplayName() + " name cannot be empty.");
    }

    @Test
    void testEntityName_WithSpaces_ThrowsIllegalArgumentException() {
        String entityTypeDisplayName = EntityType.DRIVE.getDisplayName();
        assertThatThrownBy(() -> ValidationUtils.validateEntityName("Some Entity Name", entityTypeDisplayName))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining(EntityType.DRIVE.getDisplayName() + " name must be alphanumeric");
    }

    @Test
    void testEntityName_WithSpecialCharacters_ThrowsIllegalArgumentException() {
        String entityTypeDisplayName = EntityType.DRIVE.getDisplayName();
        assertThatThrownBy(() -> ValidationUtils.validateEntityName("Some@Entity$Name", entityTypeDisplayName))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining(EntityType.DRIVE.getDisplayName() + " name must be alphanumeric");
    }

    @Test
    void testEntityName_OnlyAlphabets() {
        String result = ValidationUtils.validateEntityName("ValidEntityName", EntityType.DRIVE.getDisplayName());
        assertEquals("ValidEntityName", result);
    }

    @Test
    void testEntityName_OnlyNumbers() {
        String result = ValidationUtils.validateEntityName("123456", EntityType.DRIVE.getDisplayName());
        assertEquals("123456", result);
    }

    @Test
    void testEntityName_Alphanumeric() {
        String result = ValidationUtils.validateEntityName("Valid123EntityName", EntityType.DRIVE.getDisplayName());
        assertEquals("Valid123EntityName", result);
    }

    @Test
    void testEntityName_AlphanumericIncludingDots() {
        String result = ValidationUtils.validateEntityName("ValidTextFile.txt", EntityType.DRIVE.getDisplayName());
        assertEquals("ValidTextFile.txt", result);
    }
}
