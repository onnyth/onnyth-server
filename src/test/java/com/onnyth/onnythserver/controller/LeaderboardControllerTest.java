package com.onnyth.onnythserver.controller;

import com.onnyth.onnythserver.dto.*;
import com.onnyth.onnythserver.models.StatDomain;
import com.onnyth.onnythserver.security.SecurityConfig;
import com.onnyth.onnythserver.service.LeaderboardService;
import com.onnyth.onnythserver.support.MockJwtDecoderConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LeaderboardController.class)
@Import({ SecurityConfig.class, MockJwtDecoderConfig.class })
@DisplayName("LeaderboardController")
class LeaderboardControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @MockitoBean
        private LeaderboardService leaderboardService;

        private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

        @Nested
        @DisplayName("GET /api/v1/leaderboard")
        class GetLeaderboard {

                @Test
                @DisplayName("returns overall leaderboard when no category")
                void returnsOverallLeaderboard() throws Exception {
                        LeaderboardEntryResponse entry = LeaderboardEntryResponse.builder()
                                        .position(1).userId(USER_ID).username("alice").totalScore(500)
                                        .isCurrentUser(true).build();
                        LeaderboardResponse response = LeaderboardResponse.builder()
                                        .entries(List.of(entry)).totalFriends(2)
                                        .currentUserPosition(1).currentUserScore(500).build();

                        when(leaderboardService.getFriendsLeaderboard(eq(USER_ID), any())).thenReturn(response);

                        mockMvc.perform(get("/api/v1/leaderboard")
                                        .with(jwt().jwt(j -> j.subject(USER_ID.toString()))))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.entries[0].username").value("alice"))
                                        .andExpect(jsonPath("$.currentUserPosition").value(1))
                                        .andExpect(jsonPath("$.totalFriends").value(2));
                }

                @Test
                @DisplayName("returns category leaderboard when category param present")
                void returnsCategoryLeaderboard() throws Exception {
                        CategoryLeaderboardEntryResponse entry = CategoryLeaderboardEntryResponse.builder()
                                        .position(1).userId(USER_ID).username("alice").categoryValue(80)
                                        .category("Physique").isCurrentUser(true).build();

                        when(leaderboardService.getLeaderboardByCategory(eq(USER_ID), eq(StatDomain.PHYSIQUE), any()))
                                        .thenReturn(new PageImpl<>(List.of(entry)));

                        mockMvc.perform(get("/api/v1/leaderboard")
                                        .param("category", "PHYSIQUE")
                                        .with(jwt().jwt(j -> j.subject(USER_ID.toString()))))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.content[0].categoryValue").value(80));
                }

                @Test
                @DisplayName("returns 401 when not authenticated")
                void returns401() throws Exception {
                        mockMvc.perform(get("/api/v1/leaderboard"))
                                        .andExpect(status().isUnauthorized());
                }
        }

        @Nested
        @DisplayName("GET /api/v1/leaderboard/my-position")
        class GetMyPosition {

                @Test
                @DisplayName("returns user position with user ahead")
                void returnsPosition() throws Exception {
                        UserLeaderboardPositionResponse response = UserLeaderboardPositionResponse.builder()
                                        .position(2).totalParticipants(5).score(500)
                                        .pointsToNextPosition(200).userAheadUsername("charlie")
                                        .userAheadId(UUID.randomUUID()).build();

                        when(leaderboardService.getUserPosition(USER_ID)).thenReturn(response);

                        mockMvc.perform(get("/api/v1/leaderboard/my-position")
                                        .with(jwt().jwt(j -> j.subject(USER_ID.toString()))))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.position").value(2))
                                        .andExpect(jsonPath("$.pointsToNextPosition").value(200))
                                        .andExpect(jsonPath("$.userAheadUsername").value("charlie"));
                }
        }
}
