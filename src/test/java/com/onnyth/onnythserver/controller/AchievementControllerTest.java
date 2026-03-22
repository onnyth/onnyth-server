package com.onnyth.onnythserver.controller;

import com.onnyth.onnythserver.dto.AchievementResponse;
import com.onnyth.onnythserver.dto.AchievementStatsResponse;
import com.onnyth.onnythserver.dto.DisplayedBadgeResponse;
import com.onnyth.onnythserver.models.AchievementCategory;
import com.onnyth.onnythserver.security.SecurityConfig;
import com.onnyth.onnythserver.service.AchievementService;
import com.onnyth.onnythserver.support.MockJwtDecoderConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AchievementController.class)
@Import({ SecurityConfig.class, MockJwtDecoderConfig.class })
@DisplayName("AchievementController")
class AchievementControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private AchievementService achievementService;

    private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID ACH_ID = UUID.fromString("00000000-0000-0000-0000-000000000010");

    @Nested
    @DisplayName("GET /api/v1/achievements")
    class GetAll {
        @Test
        @DisplayName("returns all achievements")
        void returnsAll() throws Exception {
            AchievementResponse resp = AchievementResponse.builder()
                    .id(ACH_ID).name("Century Club").description("Reach 100").icon("💯")
                    .category("Milestone").points(10).isUnlocked(true).progress(100)
                    .unlockedAt(Instant.now()).build();
            when(achievementService.getAllAchievements(USER_ID)).thenReturn(List.of(resp));

            mockMvc.perform(get("/api/v1/achievements")
                    .with(jwt().jwt(j -> j.subject(USER_ID.toString()))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].name").value("Century Club"))
                    .andExpect(jsonPath("$[0].isUnlocked").value(true));
        }

        @Test
        @DisplayName("returns 401 when not authenticated")
        void returns401() throws Exception {
            mockMvc.perform(get("/api/v1/achievements"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/achievements/stats")
    class GetStats {
        @Test
        @DisplayName("returns achievement stats")
        void returnsStats() throws Exception {
            AchievementStatsResponse stats = AchievementStatsResponse.builder()
                    .totalAchievements(12).unlockedCount(3).totalPoints(200).earnedPoints(45).build();
            when(achievementService.getAchievementStats(USER_ID)).thenReturn(stats);

            mockMvc.perform(get("/api/v1/achievements/stats")
                    .with(jwt().jwt(j -> j.subject(USER_ID.toString()))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalAchievements").value(12))
                    .andExpect(jsonPath("$.unlockedCount").value(3));
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/achievements/displayed")
    class UpdateDisplayed {
        @Test
        @DisplayName("updates displayed badges")
        void updatesBadges() throws Exception {
            DisplayedBadgeResponse badge = DisplayedBadgeResponse.builder()
                    .id(ACH_ID).name("Century Club").icon("💯").achievementId(ACH_ID).build();
            when(achievementService.updateDisplayedBadges(eq(USER_ID), any())).thenReturn(List.of(badge));

            mockMvc.perform(put("/api/v1/achievements/displayed")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"achievementIds\":[\"" + ACH_ID + "\"]}")
                    .with(jwt().jwt(j -> j.subject(USER_ID.toString()))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].name").value("Century Club"));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/achievements/unlocked")
    class GetUnlocked {
        @Test
        @DisplayName("returns unlocked achievements")
        void returnsUnlocked() throws Exception {
            AchievementResponse resp = AchievementResponse.builder()
                    .id(ACH_ID).name("Century Club").isUnlocked(true).progress(100).build();
            when(achievementService.getUnlockedAchievements(USER_ID)).thenReturn(List.of(resp));

            mockMvc.perform(get("/api/v1/achievements/unlocked")
                    .with(jwt().jwt(j -> j.subject(USER_ID.toString()))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].isUnlocked").value(true));
        }
    }
}
