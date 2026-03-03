package com.onnyth.onnythserver.controller;

import com.onnyth.onnythserver.dto.LeaderboardEntryResponse;
import com.onnyth.onnythserver.dto.LeaderboardResponse;
import com.onnyth.onnythserver.service.LeaderboardService;
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

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Web layer tests for LeaderboardController.
 */
@WebMvcTest(LeaderboardController.class)
@Import({ SecurityConfig.class, MockJwtDecoderConfig.class })
@DisplayName("LeaderboardController")
class LeaderboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LeaderboardService leaderboardService;

    private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    private LeaderboardResponse buildLeaderboardResponse() {
        LeaderboardEntryResponse entry = LeaderboardEntryResponse.builder()
                .rank(1)
                .userId(UUID.randomUUID())
                .username("topuser")
                .fullName("Top User")
                .profilePic("https://example.com/pic.jpg")
                .totalScore(500)
                .rankTier("Platinum")
                .rankBadge("💎")
                .build();

        return LeaderboardResponse.builder()
                .entries(List.of(entry))
                .userRank(3)
                .totalUsers(10)
                .build();
    }

    @Nested
    @DisplayName("GET /api/v1/leaderboards")
    class GetGlobalLeaderboard {

        @Test
        @DisplayName("returns 200 with leaderboard data when authenticated")
        void returns200_whenAuthenticated() throws Exception {
            when(leaderboardService.getGlobalLeaderboard(eq(USER_ID), eq(50), eq(0)))
                    .thenReturn(buildLeaderboardResponse());

            mockMvc.perform(get("/api/v1/leaderboards")
                            .with(jwt().jwt(j -> j.subject(USER_ID.toString()))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.entries").isArray())
                    .andExpect(jsonPath("$.entries[0].rank").value(1))
                    .andExpect(jsonPath("$.entries[0].username").value("topuser"))
                    .andExpect(jsonPath("$.entries[0].totalScore").value(500))
                    .andExpect(jsonPath("$.entries[0].rankTier").value("Platinum"))
                    .andExpect(jsonPath("$.userRank").value(3))
                    .andExpect(jsonPath("$.totalUsers").value(10));
        }

        @Test
        @DisplayName("passes custom limit and offset parameters")
        void passesCustomParams() throws Exception {
            when(leaderboardService.getGlobalLeaderboard(eq(USER_ID), eq(10), eq(20)))
                    .thenReturn(LeaderboardResponse.builder()
                            .entries(List.of())
                            .userRank(null)
                            .totalUsers(0)
                            .build());

            mockMvc.perform(get("/api/v1/leaderboards")
                            .param("limit", "10")
                            .param("offset", "20")
                            .with(jwt().jwt(j -> j.subject(USER_ID.toString()))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.entries").isEmpty());
        }

        @Test
        @DisplayName("uses default limit and offset when not specified")
        void usesDefaults() throws Exception {
            when(leaderboardService.getGlobalLeaderboard(eq(USER_ID), eq(50), eq(0)))
                    .thenReturn(LeaderboardResponse.builder()
                            .entries(List.of())
                            .userRank(1)
                            .totalUsers(1)
                            .build());

            mockMvc.perform(get("/api/v1/leaderboards")
                            .with(jwt().jwt(j -> j.subject(USER_ID.toString()))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.userRank").value(1));
        }

        @Test
        @DisplayName("returns 401 when not authenticated")
        void returns401_whenNotAuthenticated() throws Exception {
            mockMvc.perform(get("/api/v1/leaderboards"))
                    .andExpect(status().isUnauthorized());
        }
    }
}
