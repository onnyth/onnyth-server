package com.onnyth.onnythserver.quest.application.usecase;

import com.onnyth.onnythserver.models.StatDomain;
import com.onnyth.onnythserver.quest.adapter.in.rest.dto.QuestCompletionResponse;
import com.onnyth.onnythserver.quest.adapter.in.rest.dto.QuestListResponse;
import com.onnyth.onnythserver.quest.adapter.in.rest.dto.QuestResponse;
import com.onnyth.onnythserver.quest.application.exception.QuestAlreadyCompletedException;
import com.onnyth.onnythserver.quest.application.exception.QuestExpiredException;
import com.onnyth.onnythserver.quest.application.exception.QuestNotFoundException;
import com.onnyth.onnythserver.quest.application.port.QuestCompletionRepository;
import com.onnyth.onnythserver.quest.application.port.QuestRepository;
import com.onnyth.onnythserver.quest.domain.model.Quest;
import com.onnyth.onnythserver.quest.domain.model.QuestCompletion;
import com.onnyth.onnythserver.quest.domain.model.QuestStatus;
import com.onnyth.onnythserver.ranking.application.usecase.RankUseCaseService;
import com.onnyth.onnythserver.ranking.domain.model.RankTier;
import com.onnyth.onnythserver.support.TestDataFactory;
import com.onnyth.onnythserver.user.application.port.UserRepository;
import com.onnyth.onnythserver.user.domain.model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("QuestUseCaseService")
class QuestUseCaseServiceTest {

    @Mock
    private QuestRepository questRepository;
    @Mock
    private QuestCompletionRepository questCompletionRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private RankUseCaseService rankService;

    @InjectMocks
    private QuestUseCaseService questService;

    private Quest buildQuest(String title, int xpReward, StatDomain category) {
        return Quest.builder()
                .id(UUID.randomUUID())
                .title(title)
                .description("Test quest: " + title)
                .xpReward(xpReward)
                .category(category)
                .status(QuestStatus.ACTIVE)
                .createdAt(Instant.now())
                .build();
    }

    @Nested
    @DisplayName("getActiveQuests")
    class GetActiveQuests {

        @Test
        @DisplayName("returns active quests with completion status")
        void returnsActiveQuestsWithCompletionStatus() {
            UUID userId = UUID.randomUUID();
            Quest quest1 = buildQuest("Quest A", 50, StatDomain.PHYSIQUE);
            Quest quest2 = buildQuest("Quest B", 30, StatDomain.OCCUPATION);

            QuestCompletion completion = QuestCompletion.builder()
                    .userId(userId)
                    .questId(quest1.getId())
                    .build();

            when(questRepository.findAllByStatus(QuestStatus.ACTIVE)).thenReturn(List.of(quest1, quest2));
            when(questCompletionRepository.findAllByUserId(userId)).thenReturn(List.of(completion));

            QuestListResponse response = questService.getActiveQuests(userId);

            assertThat(response.quests()).hasSize(2);
            assertThat(response.completedCount()).isEqualTo(1);
            assertThat(response.totalCount()).isEqualTo(2);
            assertThat(response.quests().get(0).completed()).isTrue();
            assertThat(response.quests().get(1).completed()).isFalse();
        }

        @Test
        @DisplayName("returns empty list when no active quests")
        void returnsEmptyWhenNoQuests() {
            UUID userId = UUID.randomUUID();
            when(questRepository.findAllByStatus(QuestStatus.ACTIVE)).thenReturn(List.of());
            when(questCompletionRepository.findAllByUserId(userId)).thenReturn(List.of());

            QuestListResponse response = questService.getActiveQuests(userId);

            assertThat(response.quests()).isEmpty();
            assertThat(response.completedCount()).isZero();
            assertThat(response.totalCount()).isZero();
        }
    }

    @Nested
    @DisplayName("getQuestById")
    class GetQuestById {

        @Test
        @DisplayName("returns quest with completion status")
        void returnsQuestWithCompletion() {
            UUID userId = UUID.randomUUID();
            Quest quest = buildQuest("Test", 50, StatDomain.WISDOM);

            when(questRepository.findById(quest.getId())).thenReturn(Optional.of(quest));
            when(questCompletionRepository.existsByUserIdAndQuestId(userId, quest.getId())).thenReturn(true);

            QuestResponse response = questService.getQuestById(quest.getId(), userId);

            assertThat(response.title()).isEqualTo("Test");
            assertThat(response.completed()).isTrue();
        }

        @Test
        @DisplayName("throws QuestNotFoundException when quest does not exist")
        void throwsWhenNotFound() {
            UUID questId = UUID.randomUUID();
            when(questRepository.findById(questId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> questService.getQuestById(questId, UUID.randomUUID()))
                    .isInstanceOf(QuestNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("completeQuest")
    class CompleteQuest {

        @Test
        @DisplayName("completes quest, awards XP, and updates rank")
        void completesQuestAndAwardsXp() {
            UUID userId = UUID.randomUUID();
            Quest quest = buildQuest("Fitness Quest", 100, StatDomain.PHYSIQUE);
            User user = TestDataFactory.aUser().id(userId).totalScore(200L).rankTier(RankTier.SILVER).build();

            when(questRepository.findByIdAndStatus(quest.getId(), QuestStatus.ACTIVE)).thenReturn(Optional.of(quest));
            when(questCompletionRepository.existsByUserIdAndQuestId(userId, quest.getId())).thenReturn(false);
            when(userRepository.findById(userId)).thenReturn(Optional.of(user));

            QuestCompletionResponse response = questService.completeQuest(userId, quest.getId());

            assertThat(response.xpAwarded()).isEqualTo(100);
            assertThat(response.newTotalScore()).isEqualTo(300);
            assertThat(response.questTitle()).isEqualTo("Fitness Quest");
            verify(questCompletionRepository).save(any(QuestCompletion.class));
            verify(userRepository).save(user);
            verify(rankService).updateUserRank(userId);
        }

        @Test
        @DisplayName("throws QuestNotFoundException when quest not active")
        void throwsWhenNotActive() {
            UUID questId = UUID.randomUUID();
            when(questRepository.findByIdAndStatus(questId, QuestStatus.ACTIVE)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> questService.completeQuest(UUID.randomUUID(), questId))
                    .isInstanceOf(QuestNotFoundException.class);
        }

        @Test
        @DisplayName("throws QuestExpiredException when quest past deadline")
        void throwsWhenExpired() {
            UUID userId = UUID.randomUUID();
            Quest quest = buildQuest("Expired", 50, StatDomain.OCCUPATION);
            quest.setDeadline(Instant.now().minus(1, ChronoUnit.DAYS));

            when(questRepository.findByIdAndStatus(quest.getId(), QuestStatus.ACTIVE)).thenReturn(Optional.of(quest));

            assertThatThrownBy(() -> questService.completeQuest(userId, quest.getId()))
                    .isInstanceOf(QuestExpiredException.class);
        }

        @Test
        @DisplayName("throws QuestAlreadyCompletedException on double completion")
        void throwsOnDoubleCompletion() {
            UUID userId = UUID.randomUUID();
            Quest quest = buildQuest("Already Done", 50, StatDomain.WEALTH);

            when(questRepository.findByIdAndStatus(quest.getId(), QuestStatus.ACTIVE)).thenReturn(Optional.of(quest));
            when(questCompletionRepository.existsByUserIdAndQuestId(userId, quest.getId())).thenReturn(true);

            assertThatThrownBy(() -> questService.completeQuest(userId, quest.getId()))
                    .isInstanceOf(QuestAlreadyCompletedException.class);
        }
    }
}
