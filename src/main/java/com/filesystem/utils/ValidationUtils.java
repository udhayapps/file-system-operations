package com.filesystem.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Pattern;
import java.util.Optional;

/**
 * Validation utility for the file system entities.
 */
public final class ValidationUtils {
    private static final Logger log = LoggerFactory.getLogger(ValidationUtils.class);

    private ValidationUtils() { //NOSONAR S1144
        throw new UnsupportedOperationException("ValidationUtils cannot be instantiated. Use static methods only.");
    }

    // Permitting dot(.) in entity name to allow handling of file path extensions(.txt/.zip)
    private static final Pattern ALPHANUMERIC_PATTERN = Pattern.compile("^[a-zA-Z0-9.]+$");

    /**
     * Validates the entity name to be not null, not empty and to be an alphanumeric string.
     *
     * @param entityName                Entity name to validate.
     * @param entityTypeForErrorMessage Error message for the entity type.
     * @return Validated entity name.
     * @throws NullPointerException     Throws if the entity name is null.
     * @throws IllegalArgumentException Throws if the entity name is empty or not alphanumeric.
     */
    public static String validateEntityName(String entityName, String entityTypeForErrorMessage) {
        Optional.ofNullable(entityName)
            .map(name -> {
                if (name.isEmpty()) {
                    log.error("{} name cannot be empty.", entityTypeForErrorMessage);
                    throw new IllegalArgumentException(entityTypeForErrorMessage + " name cannot be empty.");
                }
                return name;
            })
            .orElseThrow(() -> {
                log.error("{} name cannot be null.", entityTypeForErrorMessage);
                return new NullPointerException(entityTypeForErrorMessage + " name cannot be null.");
            });

        if (!ALPHANUMERIC_PATTERN.matcher(entityName).matches()) {
            log.error("{} name must be alphanumeric: {}", entityTypeForErrorMessage, entityName);
            throw new IllegalArgumentException(entityTypeForErrorMessage + " name must be alphanumeric: " + entityName);
        }

        return entityName;
    }
}
