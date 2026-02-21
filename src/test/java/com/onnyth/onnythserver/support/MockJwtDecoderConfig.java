package com.onnyth.onnythserver.support;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import static org.mockito.Mockito.mock;

/**
 * Test configuration that provides a mock JwtDecoder bean.
 * Import this in @WebMvcTest classes that also import SecurityConfig,
 * so the OAuth2 resource server can be configured without a real Supabase JWKS
 * endpoint.
 */
@TestConfiguration
public class MockJwtDecoderConfig {

    @Bean
    public JwtDecoder jwtDecoder() {
        return mock(JwtDecoder.class);
    }
}
