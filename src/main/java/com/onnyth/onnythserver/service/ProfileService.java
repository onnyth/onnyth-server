package com.onnyth.onnythserver.service;

import com.onnyth.onnythserver.dto.ProfileCardResponse;
import com.onnyth.onnythserver.dto.ProfileResponse;
import com.onnyth.onnythserver.dto.ProfileUpdateRequest;
import com.onnyth.onnythserver.exceptions.UsernameAlreadyExistsException;
import com.onnyth.onnythserver.exceptions.UserNotFoundException;
import com.onnyth.onnythserver.models.User;
import com.onnyth.onnythserver.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProfileService {

    private final UserRepository userRepository;
    private final StorageService storageService;
    private final LifeStatService lifeStatService;

    /**
     * Get user profile by user ID.
     */
    public ProfileResponse getProfile(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId.toString()));
        return ProfileResponse.fromUser(user);
    }

    /**
     * Update user profile with the provided data.
     */
    @Transactional
    public ProfileResponse updateProfile(UUID userId, ProfileUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId.toString()));

        // Update username if provided
        if (request.username() != null && !request.username().isBlank()) {
            String newUsername = request.username().trim();

            // Check if username is different and already taken by another user
            if (!newUsername.equalsIgnoreCase(user.getUsername())) {
                if (userRepository.existsByUsernameIgnoreCase(newUsername)) {
                    throw new UsernameAlreadyExistsException(newUsername);
                }
            }
            user.setUsername(newUsername);
        }

        // Update full name if provided
        if (request.fullName() != null) {
            user.setFullName(request.fullName().trim());
        }

        // Update profile picture if provided
        if (request.profilePic() != null && !request.profilePic().isBlank()) {
            user.setProfilePic(request.profilePic().trim());
        }

        // Update timestamp and check profile completion
        user.setUpdatedAt(Instant.now());
        user.checkAndUpdateProfileCompletion();

        User savedUser = userRepository.save(user);
        log.info("Profile updated for user: {}", userId);

        return ProfileResponse.fromUser(savedUser);
    }

    /**
     * Check if a username is available.
     * 
     * @param username      the username to check
     * @param currentUserId optional current user ID to exclude from check
     * @return true if username is available, false otherwise
     */
    public boolean isUsernameAvailable(String username, UUID currentUserId) {
        if (username == null || username.isBlank()) {
            return false;
        }

        // If current user already has this username, it's available for them
        if (currentUserId != null) {
            User currentUser = userRepository.findById(currentUserId).orElse(null);
            if (currentUser != null && username.equalsIgnoreCase(currentUser.getUsername())) {
                return true;
            }
        }

        return !userRepository.existsByUsernameIgnoreCase(username.trim());
    }

    /**
     * Upload profile picture and update user profile.
     */
    @Transactional
    public ProfileResponse uploadProfilePicture(UUID userId, byte[] imageData, String contentType,
            String originalFilename) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId.toString()));

        // Delete old profile picture if exists
        if (user.getProfilePic() != null && !user.getProfilePic().isBlank()) {
            storageService.deleteFile(user.getProfilePic());
        }

        // Upload new profile picture
        String imageUrl = storageService.uploadProfilePicture(userId, imageData, contentType, originalFilename);

        user.setProfilePic(imageUrl);
        user.setUpdatedAt(Instant.now());
        user.checkAndUpdateProfileCompletion();

        User savedUser = userRepository.save(user);
        log.info("Profile picture uploaded for user: {}", userId);

        return ProfileResponse.fromUser(savedUser);
    }

    /**
     * Get the RPG-style profile card for a user.
     * Includes gamification data: total score and rank tier.
     *
     * @param userId the user's ID
     * @return a ProfileCardResponse with user info and rank data
     */
    public ProfileCardResponse getProfileCard(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId.toString()));

        // Calculate total score from all life stats
        long totalScore = lifeStatService.calculateTotalScore(userId);

        return ProfileCardResponse.fromUser(user, totalScore);
    }
}
