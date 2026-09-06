package com.onnyth.onnythserver.user.adapter.in.rest;

import com.onnyth.onnythserver.profile.adapter.in.rest.dto.ProfileCardResponse;
import com.onnyth.onnythserver.profile.application.usecase.ProfileUseCaseService;
import com.onnyth.onnythserver.user.application.usecase.UserUseCaseService;
import com.onnyth.onnythserver.user.domain.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserController {

    private final UserUseCaseService userUseCaseService;
    private final ProfileUseCaseService profileService;

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userUseCaseService.getAllUsers());
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<User> getUserById(@PathVariable UUID id) {
        return userUseCaseService.getUserById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/users/email/{email}")
    public ResponseEntity<User> getUserByEmail(@PathVariable String email) {
        return userUseCaseService.getUserByEmail(email)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/users")
    public ResponseEntity<User> createUser(@RequestBody User user) {
        return ResponseEntity.ok(userUseCaseService.createUser(user));
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<User> updateUser(@PathVariable UUID id, @RequestBody User user) {
        return ResponseEntity.ok(userUseCaseService.updateUser(id, user));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
        userUseCaseService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Public endpoint — returns the profile card for any user.
     */
    @GetMapping("/v1/users/{userId}/card")
    public ResponseEntity<ProfileCardResponse> getUserProfileCard(@PathVariable UUID userId) {
        return ResponseEntity.ok(profileService.getProfileCard(userId));
    }
}
