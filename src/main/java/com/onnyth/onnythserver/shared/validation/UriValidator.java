package com.onnyth.onnythserver.shared.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.net.URI;
import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

public class UriValidator implements ConstraintValidator<ValidUri, String> {

    private Set<String> allowedSchemes;
    private boolean requireHost;

    @Override
    public void initialize(ValidUri constraintAnnotation) {
        allowedSchemes = Arrays.stream(constraintAnnotation.schemes())
                .map(scheme -> scheme.toLowerCase(Locale.ROOT))
                .collect(Collectors.toSet());
        requireHost = constraintAnnotation.requireHost();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true;
        }

        try {
            URI uri = URI.create(value.trim());

            if (!uri.isAbsolute()) {
                return false;
            }

            String scheme = uri.getScheme();
            if (scheme == null || !allowedSchemes.contains(scheme.toLowerCase(Locale.ROOT))) {
                return false;
            }

            if (requireHost && (uri.getHost() == null || uri.getHost().isBlank())) {
                return false;
            }

            return true;
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }
}

