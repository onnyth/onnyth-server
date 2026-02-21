package com.onnyth.onnythserver.repository;

import com.onnyth.onnythserver.models.User;
import com.onnyth.onnythserver.support.PostgresTestContainer;
import com.onnyth.onnythserver.support.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Repository tests for UserRepository using a real PostgreSQL database via
 * Testcontainers.
 * Uses @DataJpaTest which loads only the JPA slice (no web layer, no services).
 */
@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest extends PostgresTestContainer {

    @Autowired
    private UserRepository userRepository;

    private User savedUser;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        savedUser = userRepository.save(
                TestDataFactory.aUser()
                        .username("johndoe")
                        .fullName("John Doe")
                        .build());
    }

    // ─── findByUsername() ─────────────────────────────────────────────────────

    @Nested
    @DisplayName("findByUsername()")
    class FindByUsername {

        @Test
        @DisplayName("returns user when username matches exactly")
        void returnsUser_whenExactMatch() {
            Optional<User> result = userRepository.findByUsername("johndoe");

            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(savedUser.getId());
        }

        @Test
        @DisplayName("returns empty when username does not exist")
        void returnsEmpty_whenNotFound() {
            Optional<User> result = userRepository.findByUsername("nonexistent");

            assertThat(result).isEmpty();
        }
    }

    // ─── findByEmail() ────────────────────────────────────────────────────────

    @Nested
    @DisplayName("findByEmail()")
    class FindByEmail {

        @Test
        @DisplayName("returns user when email matches")
        void returnsUser_whenEmailMatches() {
            Optional<User> result = userRepository.findByEmail(savedUser.getEmail());

            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(savedUser.getId());
        }

        @Test
        @DisplayName("returns empty when email not found")
        void returnsEmpty_whenEmailNotFound() {
            Optional<User> result = userRepository.findByEmail("unknown@example.com");

            assertThat(result).isEmpty();
        }
    }

    // ─── existsByUsername() ───────────────────────────────────────────────────

    @Nested
    @DisplayName("existsByUsername()")
    class ExistsByUsername {

        @Test
        @DisplayName("returns true when username exists (exact case)")
        void returnsTrue_whenUsernameExists() {
            assertThat(userRepository.existsByUsername("johndoe")).isTrue();
        }

        @Test
        @DisplayName("returns false when username does not exist")
        void returnsFalse_whenUsernameNotFound() {
            assertThat(userRepository.existsByUsername("nonexistent")).isFalse();
        }
    }

    // ─── existsByUsernameIgnoreCase() ─────────────────────────────────────────

    @Nested
    @DisplayName("existsByUsernameIgnoreCase()")
    class ExistsByUsernameIgnoreCase {

        @Test
        @DisplayName("returns true for exact case match")
        void returnsTrue_exactCase() {
            assertThat(userRepository.existsByUsernameIgnoreCase("johndoe")).isTrue();
        }

        @Test
        @DisplayName("returns true for uppercase variant")
        void returnsTrue_uppercase() {
            assertThat(userRepository.existsByUsernameIgnoreCase("JOHNDOE")).isTrue();
        }

        @Test
        @DisplayName("returns true for mixed case variant")
        void returnsTrue_mixedCase() {
            assertThat(userRepository.existsByUsernameIgnoreCase("JohnDoe")).isTrue();
        }

        @Test
        @DisplayName("returns false when username does not exist")
        void returnsFalse_whenNotFound() {
            assertThat(userRepository.existsByUsernameIgnoreCase("nonexistent")).isFalse();
        }
    }

    // ─── existsByEmail() ──────────────────────────────────────────────────────

    @Nested
    @DisplayName("existsByEmail()")
    class ExistsByEmail {

        @Test
        @DisplayName("returns true when email exists")
        void returnsTrue_whenEmailExists() {
            assertThat(userRepository.existsByEmail(savedUser.getEmail())).isTrue();
        }

        @Test
        @DisplayName("returns false when email does not exist")
        void returnsFalse_whenEmailNotFound() {
            assertThat(userRepository.existsByEmail("unknown@example.com")).isFalse();
        }
    }

    // ─── save() — constraint tests ────────────────────────────────────────────

    @Nested
    @DisplayName("Unique constraints")
    class UniqueConstraints {

        @Test
        @DisplayName("allows saving a user without a username (username is optional)")
        void allowsSavingUserWithoutUsername() {
            User userWithoutUsername = TestDataFactory.aUser().build();
            User saved = userRepository.save(userWithoutUsername);

            assertThat(saved.getId()).isNotNull();
            assertThat(saved.getUsername()).isNull();
        }

        @Test
        @DisplayName("persists user with all fields correctly")
        void persistsAllFields() {
            UUID id = UUID.randomUUID();
            User user = User.builder()
                    .id(id)
                    .email("full@example.com")
                    .username("fulluser")
                    .fullName("Full User")
                    .profilePic("https://example.com/pic.jpg")
                    .emailVerified(true)
                    .profileComplete(true)
                    .build();

            User saved = userRepository.save(user);
            userRepository.flush();

            Optional<User> found = userRepository.findById(id);
            assertThat(found).isPresent();
            assertThat(found.get().getUsername()).isEqualTo("fulluser");
            assertThat(found.get().getFullName()).isEqualTo("Full User");
            assertThat(found.get().getProfileComplete()).isTrue();
        }
    }
}
