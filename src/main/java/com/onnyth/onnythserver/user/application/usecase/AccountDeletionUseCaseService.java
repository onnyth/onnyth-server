package com.onnyth.onnythserver.user.application.usecase;

import com.onnyth.onnythserver.activity.application.port.ActivityLogRepository;
import com.onnyth.onnythserver.achievement.application.port.UserAchievementRepository;
import com.onnyth.onnythserver.feed.application.port.FeedEventRepository;
import com.onnyth.onnythserver.friendship.application.port.FriendRequestRepository;
import com.onnyth.onnythserver.friendship.application.port.FriendshipRepository;
import com.onnyth.onnythserver.lifestats.application.port.*;
import com.onnyth.onnythserver.profile.application.port.ProfileVoteRepository;
import com.onnyth.onnythserver.quest.application.port.QuestCompletionRepository;
import com.onnyth.onnythserver.registration.application.port.RegistrationDraftRepository;
import com.onnyth.onnythserver.scoring.application.port.ScoreHistoryRepository;
import com.onnyth.onnythserver.store.application.port.UserCosmeticRepository;
import com.onnyth.onnythserver.streak.application.port.UserStreakRepository;
import com.onnyth.onnythserver.user.application.exception.UserNotFoundException;
import com.onnyth.onnythserver.user.application.port.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Handles permanent account deletion.
 * Deletes all user-related data across all tables, then removes the user record.
 * Required by Apple App Store guidelines (Section 5.1.1(v)).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AccountDeletionUseCaseService {

    private final UserRepository userRepository;
    private final ActivityLogRepository activityLogRepository;
    private final UserStreakRepository userStreakRepository;
    private final UserAchievementRepository userAchievementRepository;
    private final UserCosmeticRepository userCosmeticRepository;
    private final FriendshipRepository friendshipRepository;
    private final FriendRequestRepository friendRequestRepository;
    private final FeedEventRepository feedEventRepository;
    private final QuestCompletionRepository questCompletionRepository;
    private final ProfileVoteRepository profileVoteRepository;
    private final ScoreHistoryRepository scoreHistoryRepository;
    private final RegistrationDraftRepository registrationDraftRepository;
    private final UserOccupationRepository occupationRepository;
    private final UserWealthRepository wealthRepository;
    private final UserPhysiqueRepository physiqueRepository;
    private final UserWisdomRepository wisdomRepository;
    private final UserCharismaRepository charismaRepository;

    /**
     * Permanently delete a user account and all associated data.
     */
    @Transactional
    public void deleteAccount(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException(userId.toString());
        }

        log.info("Starting account deletion for userId={}", userId);

        activityLogRepository.deleteAllByUserId(userId);
        userStreakRepository.deleteByUserId(userId);
        userAchievementRepository.deleteAllByUserId(userId);
        userCosmeticRepository.deleteAllByUserId(userId);
        feedEventRepository.deleteAllByUserId(userId);
        questCompletionRepository.deleteAllByUserId(userId);
        profileVoteRepository.deleteAllByVoterId(userId);
        profileVoteRepository.deleteAllByTargetId(userId);
        scoreHistoryRepository.deleteAllByUserId(userId);
        registrationDraftRepository.deleteByUserId(userId);

        occupationRepository.deleteAllByUserId(userId);
        wealthRepository.deleteByUserId(userId);
        physiqueRepository.deleteByUserId(userId);
        wisdomRepository.deleteByUserId(userId);
        charismaRepository.deleteByUserId(userId);

        friendshipRepository.deleteAllByUserId(userId);
        friendshipRepository.deleteAllByFriendId(userId);
        friendRequestRepository.deleteAllBySenderId(userId);
        friendRequestRepository.deleteAllByReceiverId(userId);

        userRepository.deleteById(userId);

        log.info("Account deletion complete for userId={}", userId);
    }
}
