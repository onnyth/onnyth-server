package com.onnyth.onnythserver.shared.utils;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

@Component
public class RequestHasher {

    private static final String ALGORITHM = "SHA-256";

    public String hash(String requestBody) {
        try {
            MessageDigest digest = MessageDigest.getInstance(ALGORITHM);
            byte[] hashBytes = digest.digest(requestBody.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(ALGORITHM + " algorithm not available", e);
        }
    }
}
