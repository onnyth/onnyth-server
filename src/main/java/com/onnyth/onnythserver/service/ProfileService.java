package com.onnyth.onnythserver.service;

import com.onnyth.onnythserver.dto.DomainScoreDto;
import com.onnyth.onnythserver.dto.ProfileCardResponse;
import com.onnyth.onnythserver.dto.ProfileResponse;
import com.onnyth.onnythserver.dto.ProfileUpdateRequest;
import com.onnyth.onnythserver.exceptions.UsernameAlreadyExistsException;
import com.onnyth.onnythserver.exceptions.UserNotFoundException;
import com.onnyth.onnythserver.models.*;
import com.onnyth.onnythserver.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProfileService {

    private final UserRepository userRepository;
    private final StorageService storageService;
    private final UserStreakRepository userStreakRepository;
    private final UserOccupationRepository occupationRepository;
    private final UserWealthRepository wealthRepository;
    private final UserPhysiqueRepository physiqueRepository;
    private final UserWisdomRepository wisdomRepository;
    private final UserCharismaRepository charismaRepository;

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

        if (request.username() != null && !request.username().isBlank()) {
            String newUsername = request.username().trim();
            if (!newUsername.equalsIgnoreCase(user.getUsername())) {
                if (userRepository.existsByUsernameIgnoreCase(newUsername)) {
                    throw new UsernameAlreadyExistsException(newUsername);
                }
            }
            user.setUsername(newUsername);
        }

        if (request.fullName() != null) {
            user.setFullName(request.fullName().trim());
        }

        if (request.profilePic() != null && !request.profilePic().isBlank()) {
            user.setProfilePic(request.profilePic().trim());
        }

        user.setUpdatedAt(Instant.now());
        user.checkAndUpdateProfileCompletion();

        User savedUser = userRepository.save(user);
        log.info("Profile updated for user: {}", userId);
        return ProfileResponse.fromUser(savedUser);
    }

    /**
     * Check if a username is available.
     */
    public boolean isUsernameAvailable(String username, UUID currentUserId) {
        if (username == null || username.isBlank()) return false;
        if (currentUserId != null) {
            User currentUser = userRepository.findById(currentUserId).orElse(null);
            if (currentUser != null && username.equalsIgnoreCase(currentUser.getUsername())) return true;
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

        if (user.getProfilePic() != null && !user.getProfilePic().isBlank()) {
            storageService.deleteFile(user.getProfilePic());
        }

        String imageUrl = storageService.uploadProfilePicture(userId, imageData, contentType, originalFilename);
        user.setProfilePic(imageUrl);
        user.setUpdatedAt(Instant.now());
        user.checkAndUpdateProfileCompletion();

        User savedUser = userRepository.save(user);
        log.info("Profile picture uploaded for user: {}", userId);
        return ProfileResponse.fromUser(savedUser);
    }

    /**
     * Get the enriched OnnythID profile card for a user (self or viewer).
     * Fetches domain scores from each domain table and embeds them.
     */
    @Transactional(readOnly = true)
    public ProfileCardResponse getProfileCard(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId.toString()));

        // Current streak
        int streak = userStreakRepository.findByUserId(userId)
                .map(s -> s.getCurrentStreak() != null ? s.getCurrentStreak() : 0)
                .orElse(0);

        // Domain scores
        DomainScoreDto occScore = occupationRepository.findByUserIdAndIsCurrentTrue(userId)
                .map(o -> new DomainScoreDto("OCCUPATION", o.getScore() != null ? o.getScore() : 0,
                        labelForOccupationScore(o.getScore()), rankBadgeForScore(o.getScore())))
                .orElse(DomainScoreDto.empty("OCCUPATION"));

        DomainScoreDto wealthScore = wealthRepository.findByUserId(userId)
                .map(w -> new DomainScoreDto("WEALTH", w.getScore() != null ? w.getScore() : 0,
                        labelForWealthScore(w.getScore()), rankBadgeForScore(w.getScore())))
                .orElse(DomainScoreDto.empty("WEALTH"));

        DomainScoreDto physiqueScore = physiqueRepository.findByUserId(userId)
                .map(p -> new DomainScoreDto("PHYSIQUE", p.getScore() != null ? p.getScore() : 0,
                        labelForPhysiqueScore(p.getFitnessLevel()), rankBadgeForScore(p.getScore())))
                .orElse(DomainScoreDto.empty("PHYSIQUE"));

        DomainScoreDto wisdomScore = wisdomRepository.findByUserId(userId)
                .map(w -> new DomainScoreDto("WISDOM", w.getScore() != null ? w.getScore() : 0,
                        labelForWisdomScore(w.getEducationLevel()), rankBadgeForScore(w.getScore())))
                .orElse(DomainScoreDto.empty("WISDOM"));

        DomainScoreDto charismaScore = charismaRepository.findByUserId(userId)
                .map(c -> new DomainScoreDto("CHARISMA", c.getScore() != null ? c.getScore() : 0,
                        labelForCharismaScore(c.getScore()), rankBadgeForScore(c.getScore())))
                .orElse(DomainScoreDto.empty("CHARISMA"));

        return ProfileCardResponse.fromUser(user, streak, occScore, wealthScore, physiqueScore, wisdomScore, charismaScore);
    }

    /**
     * Set the active background color for a user's profile.
     * Clears any equipped background cosmetic when a solid color is chosen.
     */
    @Transactional
    public ProfileCardResponse setActiveBackgroundColor(UUID userId, String hexColor) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId.toString()));
        user.setActiveBackgroundColor(hexColor);
        user.setActiveBackgroundCosmetic(null); // solid color overrides cosmetic
        user.setUpdatedAt(Instant.now());
        userRepository.save(user);
        return getProfileCard(userId);
    }

    // ─── Domain label helpers ────────────────────────────────────────────────

    private String labelForOccupationScore(Integer score) {
        if (score == null || score == 0) return "Not set";
        if (score >= 80) return "Executive";
        if (score >= 60) return "Senior Professional";
        if (score >= 40) return "Mid-Level";
        if (score >= 20) return "Entry Level";
        return "Getting Started";
    }

    private String labelForWealthScore(Integer score) {
        if (score == null || score == 0) return "Not set";
        if (score >= 80) return "High Net Worth";
        if (score >= 55) return "Affluent";
        if (score >= 35) return "Comfortable";
        if (score >= 15) return "Building";
        return "Early Stage";
    }

    private String labelForPhysiqueScore(FitnessLevel level) {
        if (level == null) return "Not set";
        return switch (level) {
            case ATHLETE -> "Athlete";
            case ELITE -> "Elite";
            case ADVANCED -> "Advanced";
            case INTERMEDIATE -> "Intermediate";
            case BEGINNER -> "Beginner";
        };
    }

    private String labelForWisdomScore(String educationLevel) {
        if (educationLevel == null || educationLevel.isBlank()) return "Not set";
        return switch (educationLevel.toUpperCase()) {
            case "PHD" -> "Doctorate";
            case "MASTERS" -> "Master's";
            case "BACHELORS" -> "Bachelor's";
            case "ASSOCIATE" -> "Associate's";
            case "HIGH_SCHOOL" -> "High School";
            default -> "Educated";
        };
    }

    private String labelForCharismaScore(Integer score) {
        if (score == null || score == 0) return "Not set";
        if (score >= 80) return "Iconic";
        if (score >= 55) return "Influential";
        if (score >= 35) return "Social";
        if (score >= 15) return "Rising";
        return "Newcomer";
    }

    private String rankBadgeForScore(Integer score) {
        if (score == null || score < 20) return "🥉";
        if (score < 45) return "🥈";
        if (score < 70) return "🥇";
        if (score < 90) return "💎";
        return "👑";
    }
}
