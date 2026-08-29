package com.onnyth.onnythserver.validation;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UriValidatorTest {

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
    void acceptsValidHttpsUrl() {
        UrlPayload payload = new UrlPayload("https://onnyth.com/article");

        int violations = validator.validate(payload).size();

        assertEquals(0, violations);
    }

    @Test
    void rejectsUrlWithoutScheme() {
        UrlPayload payload = new UrlPayload("onnyth.com/article");

        int violations = validator.validate(payload).size();

        assertEquals(1, violations);
    }

    @Test
    void rejectsUnsupportedScheme() {
        UrlPayload payload = new UrlPayload("ftp://onnyth.com/article");

        int violations = validator.validate(payload).size();

        assertEquals(1, violations);
    }

    @Test
    void blankUrlIsHandledByNotBlankConstraint() {
        UrlPayload payload = new UrlPayload("   ");

        int violations = validator.validate(payload).size();

        assertEquals(1, violations);
    }

    @Test
    void acceptsUppercaseScheme() {
        UrlPayload payload = new UrlPayload("HTTPS://onnyth.com/article");

        boolean valid = validator.validate(payload).isEmpty();

        assertTrue(valid);
    }

    private record UrlPayload(
            @NotBlank
            @ValidUri
            String url
    ) {
    }
}

