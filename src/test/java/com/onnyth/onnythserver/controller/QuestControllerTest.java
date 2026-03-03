package com.onnyth.onnythserver.controller;

import com.onnyth.onnythserver.dto.QuestCompletionResponse;
import com.onnyth.onnythserver.dto.QuestListResponse;
import com.onnyth.onnythserver.dto.QuestResponse;
import com.onnyth.onnythserver.exceptions.QuestAlreadyCompletedException;
import com.onnyth.onnythserver.exceptions.QuestExpiredException;
import com.onnyth.onnythserver.exceptions.QuestNotFoundException;
import com.onnyth.onnythserver.service.QuestService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.onnyth.onnythserver.security.SecurityConfig;
import com.onnyth.onnythserver.support.MockJwtDecoderConfig;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(QuestController.class)
@Import({ SecurityConfig.class, MockJwtDecoderConfig.class })
@DisplayName("QuestController")
class QuestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private QuestService questService;

    private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID QUEST_ID = UUID.fromString("00000000-0000-0000-0000-000000000099");

    @Nested
    @DisplayName("GET /api/v1/quests/active")
    class GetActiveQuests {

        @Test
        @DisplayName("returns 200 with active quests")
        void returns200_withActiveQuests() throws Exception {
            QuestResponse quest = QuestResponse.builder()
                    .id(QUEST_ID)
                    .title("Daily Fitness")
                    .description("Complete a workout")
                    .xpReward(50)
                    .category("FITNESS")
                    .status("ACTIVE")
                    .completed(false)
                    .build();

            when(questService.getActiveQuests(USER_ID))
                    .thenReturn(QuestListResponse.builder()
                            .quests(List.of(quest))
                            .completedCount(0)
                            .totalCount(1)
                            .build());

            mockMvc.perform(get("/api/v1/quests/active")
                    .with(jwt().jwt(j -> j.subject(USER_ID.toString()))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.quests").isArray())
                    .andExpect(jsonPath("$.quests[0].title").value("Daily Fitness"))
                    .andExpect(jsonPath("$.quests[0].xpReward").value(50))
                    .andExpect(jsonPath("$.quests[0].completed").value(false))
                    .andExpect(jsonPath("$.totalCount").value(1));
        }

        @Test
        @DisplayName("returns 401 when not authenticated")
        void returns401_whenNotAuthenticated() throws Exception {
            mockMvc.perform(get("/api/v1/quests/active"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/quests/{id}")
    class GetQuestById {

        @Test
        @DisplayName("returns 200 with quest details")
        void returns200_withQuestDetails() throws Exception {
            QuestResponse quest = QuestResponse.builder()
                    .id(QUEST_ID)
                    .title("Career Growth")
                    .xpReward(75)
                    .category("CAREER")
                    .status("ACTIVE")
                    .completed(true)
                    .build();

            when(questService.getQuestById(QUEST_ID, USER_ID)).thenReturn(quest);

            mockMvc.perform(get("/api/v1/quests/" + QUEST_ID)
                    .with(jwt().jwt(j -> j.subject(USER_ID.toString()))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.title").value("Career Growth"))
                    .andExpect(jsonPath("$.completed").value(true));
        }

        @Test
        @DisplayName("returns 404 when quest not found")
        void returns404_whenNotFound() throws Exception {
            when(questService.getQuestById(QUEST_ID, USER_ID))
                    .thenThrow(new QuestNotFoundException(QUEST_ID.toString()));

            mockMvc.perform(get("/api/v1/quests/" + QUEST_ID)
                            .with(jwt().jwt(j -> j.subject(USER_ID.toString()))))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("POST /api/v1/quests/{id}/complete")
    class CompleteQuest {

        @Test
        @DisplayName("returns 200 with completion response on success")
        void returns200_onSuccess() throws Exception {
            QuestCompletionResponse response = QuestCompletionResponse.builder()
                    .questId(QUEST_ID)
                    .questTitle("Fitness Quest")
                    .xpAwarded(100)
                    .newTotalScore(500)
                    .rankTier("Platinum")
                    .build();

            when(questService.completeQuest(USER_ID, QUEST_ID)).thenReturn(response);

            mockMvc.perform(post("/api/v1/quests/" + QUEST_ID + "/complete")
                    .with(jwt().jwt(j -> j.subject(USER_ID.toString()))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.xpAwarded").value(100))
                    .andExpect(jsonPath("$.newTotalScore").value(500))
                    .andExpect(jsonPath("$.rankTier").value("Platinum"));
        }

        @Test
        @DisplayName("returns 404 when quest not found")
        void returns404_whenNotFound() throws Exception {
            when(questService.completeQuest(USER_ID, QUEST_ID))
                    .thenThrow(new QuestNotFoundException(QUEST_ID.toString()));

            mockMvc.perform(post("/api/v1/quests/" + QUEST_ID + "/complete")
                            .with(jwt().jwt(j -> j.subject(USER_ID.toString()))))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("returns 409 when quest already completed")
        void returns409_whenAlreadyCompleted() throws Exception {
            when(questService.completeQuest(USER_ID, QUEST_ID))
                    .thenThrow(new QuestAlreadyCompletedException(QUEST_ID.toString()));

            mockMvc.perform(post("/api/v1/quests/" + QUEST_ID + "/complete")
                            .with(jwt().jwt(j -> j.subject(USER_ID.toString()))))
                    .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("returns 400 when quest expired")
        void returns400_whenExpired() throws Exception {
            when(questService.completeQuest(USER_ID, QUEST_ID))
                    .thenThrow(new QuestExpiredException(QUEST_ID.toString()));

            mockMvc.perform(post("/api/v1/quests/" + QUEST_ID + "/complete")
                            .with(jwt().jwt(j -> j.subject(USER_ID.toString()))))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("returns 401 when not authenticated")
        void returns401_whenNotAuthenticated() throws Exception {
            mockMvc.perform(post("/api/v1/quests/" + QUEST_ID + "/complete"))
                    .andExpect(status().isUnauthorized());
        }
    }
}
