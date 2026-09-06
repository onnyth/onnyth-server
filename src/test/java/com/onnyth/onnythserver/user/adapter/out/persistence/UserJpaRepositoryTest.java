package com.onnyth.onnythserver.user.adapter.out.persistence;

import com.onnyth.onnythserver.support.PostgresTestContainer;
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
 * Repository tests for UserJpaRepository using a real PostgreSQL database via
 * Testcontainers.
 * Uses @DataJpaTest which loads only the JPA slice (no web layer, no services).
 */
@DataJpaTest
@ActiveProfiles("test")
class UserJpaRepositoryTest extends PostgresTestContainer {

    @Autowired
    private UserJpaRepository userJpaRepository;

    private UserEntity savedUser;

    private static UserEntity.UserEntityBuilder aUserEntity() {
        return UserEntity.builder()
                .id(UUID.randomUUID())
                .email("test_" + UUID.randomUUID().toString().substring(0, 8) + "@example.com")
                .emailVerified(true)
                .profileComplete(false);
    }

    @BeforeEach
    void setUp() {
        userJpaRepository.deleteAll();
        savedUser = userJpaRepository.save(
                aUserEntity()
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
            Optional<UserEntity> result = userJpaRepository.findByUsername("johndoe");

            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(savedUser.getId());
        }

        @Test
        @DisplayName("returns empty when username does not exist")
        void returnsEmpty_whenNotFound() {
            Optional<UserEntity> result = userJpaRepository.findByUsername("nonexistent");

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
            Optional<UserEntity> result = userJpaRepository.findByEmail(savedUser.getEmail());

            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(savedUser.getId());
        }

        @Test
        @DisplayName("returns empty when email not found")
        void returnsEmpty_whenEmailNotFound() {
            Optional<UserEntity> result = userJpaRepository.findByEmail("unknown@example.com");

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
            assertThat(userJpaRepository.existsByUsername("johndoe")).isTrue();
        }

        @Test
        @DisplayName("returns false when username does not exist")
        void returnsFalse_whenUsernameNotFound() {
            assertThat(userJpaRepository.existsByUsername("nonexistent")).isFalse();
        }
    }

    // ─── existsByUsernameIgnoreCase() ─────────────────────────────────────────

    @Nested
    @DisplayName("existsByUsernameIgnoreCase()")
    class ExistsByUsernameIgnoreCase {

        @Test
        @DisplayName("returns true for exact case match")
        void returnsTrue_exactCase() {
            assertThat(userJpaRepository.existsByUsernameIgnoreCase("johndoe")).isTrue();
        }

        @Test
        @DisplayName("returns true for uppercase variant")
        void returnsTrue_uppercase() {
            assertThat(userJpaRepository.existsByUsernameIgnoreCase("JOHNDOE")).isTrue();
        }

        @Test
        @DisplayName("returns true for mixed case variant")
        void returnsTrue_mixedCase() {
            assertThat(userJpaRepository.existsByUsernameIgnoreCase("JohnDoe")).isTrue();
        }

        @Test
        @DisplayName("returns false when username does not exist")
        void returnsFalse_whenNotFound() {
            assertThat(userJpaRepository.existsByUsernameIgnoreCase("nonexistent")).isFalse();
        }
    }

    // ─── existsByEmail() ──────────────────────────────────────────────────────

    @Nested
    @DisplayName("existsByEmail()")
    class ExistsByEmail {

        @Test
        @DisplayName("returns true when email exists")
        void returnsTrue_whenEmailExists() {
            assertThat(userJpaRepository.existsByEmail(savedUser.getEmail())).isTrue();
        }

        @Test
        @DisplayName("returns false when email does not exist")
        void returnsFalse_whenEmailNotFound() {
            assertThat(userJpaRepository.existsByEmail("unknown@example.com")).isFalse();
        }
    }

    // ─── save() — constraint tests ────────────────────────────────────────────

    @Nested
    @DisplayName("Unique constraints")
    class UniqueConstraints {

        @Test
        @DisplayName("allows saving a user without a username (username is optional)")
        void allowsSavingUserWithoutUsername() {
            UserEntity userWithoutUsername = aUserEntity().build();
            UserEntity saved = userJpaRepository.save(userWithoutUsername);

            assertThat(saved.getId()).isNotNull();
            assertThat(saved.getUsername()).isNull();
        }

        @Test
        @DisplayName("persists user with all fields correctly")
        void persistsAllFields() {
            UUID id = UUID.randomUUID();
            UserEntity user = UserEntity.builder()
                    .id(id)
                    .email("full@example.com")
                    .username("fulluser")
                    .fullName("Full User")
                    .profilePic("https://example.com/pic.jpg")
                    .emailVerified(true)
                    .profileComplete(true)
                    .build();

            userJpaRepository.save(user);
            userJpaRepository.flush();

            Optional<UserEntity> found = userJpaRepository.findById(id);
            assertThat(found).isPresent();
            assertThat(found.get().getUsername()).isEqualTo("fulluser");
            assertThat(found.get().getFullName()).isEqualTo("Full User");
            assertThat(found.get().getProfileComplete()).isTrue();
        }
    }
}
