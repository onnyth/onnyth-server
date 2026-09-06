package com.onnyth.onnythserver.bookmark.adapter.in.rest.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CreateBookmarkRequestValidationTest {

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
    void acceptsValidPayload() {
        CreateBookmarkRequest request = new CreateBookmarkRequest(
                "https://onnyth.com/article",
                "Useful article",
                Set.of("productivity", "wellness")
        );

        assertTrue(validator.validate(request).isEmpty());
    }

    @Test
    void rejectsInvalidUrl() {
        CreateBookmarkRequest request = new CreateBookmarkRequest(
                "onnyth.com/article",
                "Useful article",
                Set.of("productivity")
        );

        assertEquals(1, validator.validate(request).size());
    }

    @Test
    void rejectsTitleAbove255Characters() {
        CreateBookmarkRequest request = new CreateBookmarkRequest(
                "https://onnyth.com/article",
                "a".repeat(256),
                Set.of("productivity")
        );

        assertEquals(1, validator.validate(request).size());
    }

    @Test
    void rejectsBlankTagAndTooLongTag() {
        CreateBookmarkRequest request = new CreateBookmarkRequest(
                "https://onnyth.com/article",
                "Useful article",
                Set.of("   ", "a".repeat(51))
        );

        assertEquals(2, validator.validate(request).size());
    }
}

