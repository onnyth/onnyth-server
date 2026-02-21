package com.onnyth.onnythserver.unit.service;

import com.onnyth.onnythserver.models.User;
import com.onnyth.onnythserver.repository.UserRepository;
import com.onnyth.onnythserver.service.UserService;
import com.onnyth.onnythserver.support.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserService.
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private UUID testUserId;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testUser = TestDataFactory.aUserWithId(testUserId);
    }

    @Nested
    @DisplayName("getAllUsers()")
    class GetAllUsers {

        @Test
        @DisplayName("returns all users from repository")
        void returnsAllUsers() {
            List<User> users = List.of(testUser, TestDataFactory.aUser().build());
            when(userRepository.findAll()).thenReturn(users);

            List<User> result = userService.getAllUsers();

            assertThat(result).hasSize(2);
            verify(userRepository).findAll();
        }

        @Test
        @DisplayName("returns empty list when no users exist")
        void returnsEmptyList() {
            when(userRepository.findAll()).thenReturn(List.of());

            List<User> result = userService.getAllUsers();

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("getUserById()")
    class GetUserById {

        @Test
        @DisplayName("returns user when found")
        void returnsUserWhenFound() {
            when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));

            Optional<User> result = userService.getUserById(testUserId);

            assertThat(result).isPresent().contains(testUser);
        }

        @Test
        @DisplayName("returns empty Optional when user not found")
        void returnsEmptyWhenNotFound() {
            when(userRepository.findById(testUserId)).thenReturn(Optional.empty());

            Optional<User> result = userService.getUserById(testUserId);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("getUserByEmail()")
    class GetUserByEmail {

        @Test
        @DisplayName("returns user when email matches")
        void returnsUserWhenFound() {
            String email = "test@example.com";
            when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));

            Optional<User> result = userService.getUserByEmail(email);

            assertThat(result).isPresent().contains(testUser);
        }

        @Test
        @DisplayName("returns empty Optional when email not found")
        void returnsEmptyWhenNotFound() {
            when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

            Optional<User> result = userService.getUserByEmail("unknown@example.com");

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("createUser()")
    class CreateUser {

        @Test
        @DisplayName("saves and returns the new user")
        void savesAndReturnsUser() {
            when(userRepository.save(testUser)).thenReturn(testUser);

            User result = userService.createUser(testUser);

            assertThat(result).isEqualTo(testUser);
            verify(userRepository).save(testUser);
        }
    }

    @Nested
    @DisplayName("updateUser()")
    class UpdateUser {

        @Test
        @DisplayName("updates username and email when user exists")
        void updatesWhenUserExists() {
            User updatedData = TestDataFactory.aUser()
                    .username("newusername")
                    .email("new@example.com")
                    .build();

            when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

            User result = userService.updateUser(testUserId, updatedData);

            assertThat(result.getUsername()).isEqualTo("newusername");
            assertThat(result.getEmail()).isEqualTo("new@example.com");
        }

        @Test
        @DisplayName("throws RuntimeException when user not found")
        void throwsWhenUserNotFound() {
            when(userRepository.findById(testUserId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.updateUser(testUserId, testUser))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("User not found");
        }
    }

    @Nested
    @DisplayName("deleteUser()")
    class DeleteUser {

        @Test
        @DisplayName("calls deleteById on repository")
        void callsDeleteById() {
            userService.deleteUser(testUserId);

            verify(userRepository).deleteById(testUserId);
        }
    }
}
