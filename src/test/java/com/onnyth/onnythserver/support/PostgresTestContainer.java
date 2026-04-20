package com.onnyth.onnythserver.support;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Singleton PostgreSQL Testcontainer shared across all integration/repository
 * tests.
 * Starting once per JVM run keeps the test suite fast.
 * Usage: extend this class or call registerProperties()
 * via @DynamicPropertySource.
 */
public abstract class PostgresTestContainer {

    static final PostgreSQLContainer<?> POSTGRES = createContainer();

    @SuppressWarnings("resource") // Singleton container — closed via shutdown hook, not try-with-resources
    private static PostgreSQLContainer<?> createContainer() {
        PostgreSQLContainer<?> container = new PostgreSQLContainer<>("postgres:16-alpine")
                .withDatabaseName("onnyth_test")
                .withUsername("test")
                .withPassword("test")
                .withReuse(true);
        container.start();
        Runtime.getRuntime().addShutdownHook(new Thread(container::stop));
        return container;
    }

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }
}
