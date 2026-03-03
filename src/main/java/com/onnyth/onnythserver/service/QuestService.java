package com.onnyth.onnythserver.service;

import com.onnyth.onnythserver.dto.QuestCompletionResponse;
import com.onnyth.onnythserver.dto.QuestListResponse;
import com.onnyth.onnythserver.dto.QuestResponse;
import com.onnyth.onnythserver.exceptions.QuestAlreadyCompletedException;
import com.onnyth.onnythserver.exceptions.QuestExpiredException;
import com.onnyth.onnythserver.exceptions.QuestNotFoundException;
import com.onnyth.onnythserver.exceptions.UserNotFoundException;
import com.onnyth.onnythserver.models.Quest;
import com.onnyth.onnythserver.models.QuestCompletion;
import com.onnyth.onnythserver.models.QuestStatus;
import com.onnyth.onnythserver.models.User;
import com.onnyth.onnythserver.repository.QuestCompletionRepository;
import com.onnyth.onnythserver.repository.QuestRepository;
import com.onnyth.onnythserver.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuestService {

    private final QuestRepository questRepository;
    private final QuestCompletionRepository questCompletionRepository;
    private final UserRepository userRepository;
    private final RankService rankService;

    /**
     * Get all active quests with the user's completion status.
     */
    @Transactional(readOnly = true)
    public QuestListResponse getActiveQuests(UUID userId) {
        List<Quest> activeQuests = questRepository.findAllByStatus(QuestStatus.ACTIVE);

        Set<UUID> completedQuestIds = questCompletionRepository.findAllByUserId(userId)
                .stream()
                .map(QuestCompletion::getQuestId)
                .collect(Collectors.toSet());

        List<QuestResponse> questResponses = activeQuests.stream()
                .map(quest -> QuestResponse.fromQuest(quest, completedQuestIds.contains(quest.getId())))
                .toList();

        int completedCount = (int) questResponses.stream().filter(QuestResponse::completed).count();

        return QuestListResponse.builder()
                .quests(questResponses)
                .completedCount(completedCount)
                .totalCount(questResponses.size())
                .build();
    }

    /**
     * Get a single quest by ID.
     */
    @Transactional(readOnly = true)
    public QuestResponse getQuestById(UUID questId, UUID userId) {
        Quest quest = questRepository.findById(questId)
                .orElseThrow(() -> new QuestNotFoundException(questId.toString()));

        boolean completed = questCompletionRepository.existsByUserIdAndQuestId(userId, questId);
        return QuestResponse.fromQuest(quest, completed);
    }

    /**
     * Complete a quest: validate, record completion, award XP, trigger rank
     * recalculation.
     */
    @Transactional
    public QuestCompletionResponse completeQuest(UUID userId, UUID questId) {
        Quest quest = questRepository.findByIdAndStatus(questId, QuestStatus.ACTIVE)
                .orElseThrow(() -> new QuestNotFoundException(questId.toString()));

        // Check deadline
        if (quest.getDeadline() != null && quest.getDeadline().isBefore(Instant.now())) {
            throw new QuestExpiredException(questId.toString());
        }

        // Check double completion
        if (questCompletionRepository.existsByUserIdAndQuestId(userId, questId)) {
            throw new QuestAlreadyCompletedException(questId.toString());
        }

        // Find user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId.toString()));

        // Record completion
        QuestCompletion completion = QuestCompletion.builder()
                .userId(userId)
                .questId(questId)
                .completedAt(Instant.now())
                .build();
        questCompletionRepository.save(completion);

        // Award XP — add to totalScore
        long newTotalScore = user.getTotalScore() + quest.getXpReward();
        user.setTotalScore(newTotalScore);
        userRepository.save(user);

        // Trigger rank recalculation
        rankService.updateUserRank(userId);

        log.info("Quest completed: userId={}, questId={}, xp={}, newScore={}",
                userId, questId, quest.getXpReward(), newTotalScore);

        return QuestCompletionResponse.builder()
                .questId(questId)
                .questTitle(quest.getTitle())
                .xpAwarded(quest.getXpReward())
                .newTotalScore(newTotalScore)
                .rankTier(user.getRankTier().getDisplayName())
                .build();
    }
}
