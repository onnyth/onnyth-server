package com.onnyth.onnythserver.bookmark.adapter.out.persistence;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BookmarkEntityValidationTest {

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void tearDownValidator() {
        validatorFactory.close();
    }

    @Test
    void acceptsTagsWithinLimit() {
        Set<String> tags = Set.of("finance", "productivity");
        assertTrue(validator.validateValue(BookmarkEntity.class, "tags", tags).isEmpty());
    }

    @Test
    void rejectsTagLongerThanFiftyCharacters() {
        Set<String> tags = Set.of("a".repeat(51));
        assertEquals(1, validator.validateValue(BookmarkEntity.class, "tags", tags).size());
    }

    @Test
    void acceptsNullTagsBecauseCollectionIsOptional() {
        assertTrue(validator.validateValue(BookmarkEntity.class, "tags", null).isEmpty());
    }
}

