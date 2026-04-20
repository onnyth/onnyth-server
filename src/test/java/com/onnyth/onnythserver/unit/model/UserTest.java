package com.onnyth.onnythserver.unit.model;

import com.onnyth.onnythserver.models.User;
import com.onnyth.onnythserver.support.TestDataFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for User entity domain logic.
 */
class UserTest {

    @Nested
    @DisplayName("checkAndUpdateProfileCompletion()")
    class CheckAndUpdateProfileCompletion {

        @Test
        @DisplayName("sets profileComplete=true when username, fullName, and profilePic are all set")
        void complete_whenAllFieldsPresent() {
            User user = TestDataFactory.aUser()
                    .username("johndoe")
                    .fullName("John Doe")
                    .profilePic("https://example.com/pic.jpg")
                    .build();

            user.checkAndUpdateProfileCompletion();

            assertThat(user.getProfileComplete()).isTrue();
        }

        @Test
        @DisplayName("sets profileComplete=false when username is missing")
        void incomplete_whenUsernameMissing() {
            User user = TestDataFactory.aUser()
                    .fullName("John Doe")
                    .profilePic("https://example.com/pic.jpg")
                    .build();

            user.checkAndUpdateProfileCompletion();

            assertThat(user.getProfileComplete()).isFalse();
        }

        @Test
        @DisplayName("sets profileComplete=false when fullName is missing")
        void incomplete_whenFullNameMissing() {
            User user = TestDataFactory.aUser()
                    .username("johndoe")
                    .profilePic("https://example.com/pic.jpg")
                    .build();

            user.checkAndUpdateProfileCompletion();

            assertThat(user.getProfileComplete()).isFalse();
        }

        @Test
        @DisplayName("sets profileComplete=false when profilePic is missing")
        void incomplete_whenProfilePicMissing() {
            User user = TestDataFactory.aUser()
                    .username("johndoe")
                    .fullName("John Doe")
                    .build();

            user.checkAndUpdateProfileCompletion();

            assertThat(user.getProfileComplete()).isFalse();
        }

        @Test
        @DisplayName("sets profileComplete=false when username is blank")
        void incomplete_whenUsernameBlank() {
            User user = TestDataFactory.aUser()
                    .username("   ")
                    .fullName("John Doe")
                    .profilePic("https://example.com/pic.jpg")
                    .build();

            user.checkAndUpdateProfileCompletion();

            assertThat(user.getProfileComplete()).isFalse();
        }

        @Test
        @DisplayName("sets profileComplete=false when all fields are null")
        void incomplete_whenAllFieldsNull() {
            User user = TestDataFactory.aUser().build();

            user.checkAndUpdateProfileCompletion();

            assertThat(user.getProfileComplete()).isFalse();
        }

        @Test
        @DisplayName("updates profileComplete from true to false when a field is cleared")
        void updatesFromTrueToFalse() {
            User user = TestDataFactory.aCompleteUser();
            assertThat(user.getProfileComplete()).isTrue();

            user.setUsername(null);
            user.checkAndUpdateProfileCompletion();

            assertThat(user.getProfileComplete()).isFalse();
        }
    }
}
