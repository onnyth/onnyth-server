package com.onnyth.onnythserver;

import com.onnyth.onnythserver.support.PostgresTestContainer;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Smoke test — verifies the full Spring application context loads correctly
 * with a real PostgreSQL database (via Testcontainers).
 */
@SpringBootTest
@ActiveProfiles("test")
class OnnythServerApplicationTests extends PostgresTestContainer {

    @Test
    void contextLoads() {
        // If the context loads without exception, the test passes.
        // This catches misconfigured beans, missing properties, and wiring errors.
    }
}
