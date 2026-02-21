package com.onnyth.onnythserver.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onnyth.onnythserver.dto.BulkStatInputRequest;
import com.onnyth.onnythserver.dto.LifeStatResponse;
import com.onnyth.onnythserver.dto.StatInputRequest;
import com.onnyth.onnythserver.dto.StatUpdateRequest;
import com.onnyth.onnythserver.dto.StatUpdateResponse;
import com.onnyth.onnythserver.exceptions.InvalidStatValueException;
import com.onnyth.onnythserver.exceptions.StatNotFoundException;
import com.onnyth.onnythserver.exceptions.UserNotFoundException;
import com.onnyth.onnythserver.models.StatCategory;
import com.onnyth.onnythserver.security.SecurityConfig;
import com.onnyth.onnythserver.service.LifeStatService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Web layer tests for LifeStatController.
 */
@WebMvcTest(LifeStatController.class)
@Import({ SecurityConfig.class, MockJwtDecoderConfig.class })
class LifeStatControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockitoBean
        private LifeStatService lifeStatService;

        private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

        // ─── POST /api/v1/stats ──────────────────────────────────────────────────

        @Nested
        @DisplayName("POST /api/v1/stats")
        class SaveStat {

                @Test
                @DisplayName("returns 201 with saved stat")
                void returns201_withSavedStat() throws Exception {
                        StatInputRequest request = new StatInputRequest(StatCategory.CAREER, 75, null);
                        LifeStatResponse response = LifeStatResponse.builder()
                                        .category(StatCategory.CAREER)
                                        .displayName("Career")
                                        .value(75)
                                        .pointsContributed(75)
                                        .lastUpdated(Instant.now())
                                        .build();

                        when(lifeStatService.saveStat(eq(USER_ID), any(StatInputRequest.class))).thenReturn(response);

                        mockMvc.perform(post("/api/v1/stats")
                                        .with(jwt().jwt(j -> j.subject(USER_ID.toString())))
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request)))
                                        .andExpect(status().isCreated())
                                        .andExpect(jsonPath("$.category").value("CAREER"))
                                        .andExpect(jsonPath("$.value").value(75))
                                        .andExpect(jsonPath("$.displayName").value("Career"));
                }

                @Test
                @DisplayName("returns 400 for invalid stat value")
                void returns400_forInvalidStatValue() throws Exception {
                        StatInputRequest request = new StatInputRequest(StatCategory.CAREER, 0, null);

                        when(lifeStatService.saveStat(eq(USER_ID), any(StatInputRequest.class)))
                                        .thenThrow(new InvalidStatValueException("Value 0 is out of range"));

                        mockMvc.perform(post("/api/v1/stats")
                                        .with(jwt().jwt(j -> j.subject(USER_ID.toString())))
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request)))
                                        .andExpect(status().isBadRequest());
                }

                @Test
                @DisplayName("returns 400 for missing category (validation)")
                void returns400_forMissingCategory() throws Exception {
                        String body = "{\"value\": 75}";

                        mockMvc.perform(post("/api/v1/stats")
                                        .with(jwt().jwt(j -> j.subject(USER_ID.toString())))
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(body))
                                        .andExpect(status().isBadRequest());
                }

                @Test
                @DisplayName("returns 401 when not authenticated")
                void returns401_whenNotAuthenticated() throws Exception {
                        StatInputRequest request = new StatInputRequest(StatCategory.CAREER, 75, null);

                        mockMvc.perform(post("/api/v1/stats")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request)))
                                        .andExpect(status().isUnauthorized());
                }

                @Test
                @DisplayName("returns 404 when user not found")
                void returns404_whenUserNotFound() throws Exception {
                        StatInputRequest request = new StatInputRequest(StatCategory.CAREER, 75, null);

                        when(lifeStatService.saveStat(eq(USER_ID), any(StatInputRequest.class)))
                                        .thenThrow(new UserNotFoundException(USER_ID.toString()));

                        mockMvc.perform(post("/api/v1/stats")
                                        .with(jwt().jwt(j -> j.subject(USER_ID.toString())))
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request)))
                                        .andExpect(status().isNotFound());
                }
        }

        // ─── POST /api/v1/stats/bulk ─────────────────────────────────────────────

        @Nested
        @DisplayName("POST /api/v1/stats/bulk")
        class SaveStatsBulk {

                @Test
                @DisplayName("returns 201 with saved stats")
                void returns201_withSavedStats() throws Exception {
                        BulkStatInputRequest request = new BulkStatInputRequest(List.of(
                                        new StatInputRequest(StatCategory.CAREER, 75, null),
                                        new StatInputRequest(StatCategory.FITNESS, 80, null)));
                        List<LifeStatResponse> responses = List.of(
                                        LifeStatResponse.builder()
                                                        .category(StatCategory.CAREER).displayName("Career")
                                                        .value(75).pointsContributed(75).lastUpdated(Instant.now())
                                                        .build(),
                                        LifeStatResponse.builder()
                                                        .category(StatCategory.FITNESS).displayName("Fitness")
                                                        .value(80).pointsContributed(80).lastUpdated(Instant.now())
                                                        .build());

                        when(lifeStatService.saveStats(eq(USER_ID), any())).thenReturn(responses);

                        mockMvc.perform(post("/api/v1/stats/bulk")
                                        .with(jwt().jwt(j -> j.subject(USER_ID.toString())))
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request)))
                                        .andExpect(status().isCreated())
                                        .andExpect(jsonPath("$").isArray())
                                        .andExpect(jsonPath("$.length()").value(2))
                                        .andExpect(jsonPath("$[0].category").value("CAREER"))
                                        .andExpect(jsonPath("$[1].category").value("FITNESS"));
                }

                @Test
                @DisplayName("returns 400 for empty stats list")
                void returns400_forEmptyStatsList() throws Exception {
                        String body = "{\"stats\": []}";

                        mockMvc.perform(post("/api/v1/stats/bulk")
                                        .with(jwt().jwt(j -> j.subject(USER_ID.toString())))
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(body))
                                        .andExpect(status().isBadRequest());
                }

                @Test
                @DisplayName("returns 401 when not authenticated")
                void returns401_whenNotAuthenticated() throws Exception {
                        BulkStatInputRequest request = new BulkStatInputRequest(List.of(
                                        new StatInputRequest(StatCategory.CAREER, 75, null)));

                        mockMvc.perform(post("/api/v1/stats/bulk")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request)))
                                        .andExpect(status().isUnauthorized());
                }
        }

        // ─── GET /api/v1/stats ───────────────────────────────────────────────────

        @Nested
        @DisplayName("GET /api/v1/stats")
        class GetUserStats {

                @Test
                @DisplayName("returns 200 with user stats")
                void returns200_withUserStats() throws Exception {
                        List<LifeStatResponse> responses = List.of(
                                        LifeStatResponse.builder()
                                                        .category(StatCategory.CAREER).displayName("Career")
                                                        .value(75).pointsContributed(75).lastUpdated(Instant.now())
                                                        .build());

                        when(lifeStatService.getUserStats(USER_ID)).thenReturn(responses);

                        mockMvc.perform(get("/api/v1/stats")
                                        .with(jwt().jwt(j -> j.subject(USER_ID.toString()))))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$").isArray())
                                        .andExpect(jsonPath("$[0].category").value("CAREER"));
                }

        @Test
        @DisplayName("returns 200 with empty list when no stats")
        void returns200_withEmptyList() throws Exception {
            when(lifeStatService.getUserStats(USER_ID)).thenReturn(List.of());

            mockMvc.perform(get("/api/v1/stats")
                            .with(jwt().jwt(j -> j.subject(USER_ID.toString()))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(0));
        }

                @Test
                @DisplayName("returns 401 when not authenticated")
                void returns401_whenNotAuthenticated() throws Exception {
                        mockMvc.perform(get("/api/v1/stats"))
                                        .andExpect(status().isUnauthorized());
                }
        }

        // ─── PUT /api/v1/stats/{category} ───────────────────────────────────────────

        @Nested
        @DisplayName("PUT /api/v1/stats/{category}")
        class UpdateStat {

                @Test
                @DisplayName("returns 200 with updated stat")
                void returns200_withUpdatedStat() throws Exception {
                        StatUpdateRequest request = new StatUpdateRequest(75, "got promotion");
                        StatUpdateResponse response = StatUpdateResponse.builder()
                                        .category(StatCategory.CAREER)
                                        .displayName("Career")
                                        .previousValue(50)
                                        .newValue(75)
                                        .totalScore(300)
                                        .scoreChange(25)
                                        .build();

                        when(lifeStatService.updateStat(eq(USER_ID), eq(StatCategory.CAREER),
                                        any(StatUpdateRequest.class)))
                                        .thenReturn(response);

                        mockMvc.perform(put("/api/v1/stats/CAREER")
                                        .with(jwt().jwt(j -> j.subject(USER_ID.toString())))
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request)))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.category").value("CAREER"))
                                        .andExpect(jsonPath("$.previousValue").value(50))
                                        .andExpect(jsonPath("$.newValue").value(75))
                                        .andExpect(jsonPath("$.scoreChange").value(25))
                                        .andExpect(jsonPath("$.totalScore").value(300));
                }

                @Test
                @DisplayName("returns 400 for invalid value")
                void returns400_forInvalidValue() throws Exception {
                        StatUpdateRequest request = new StatUpdateRequest(0, null);

                        when(lifeStatService.updateStat(eq(USER_ID), eq(StatCategory.CAREER),
                                        any(StatUpdateRequest.class)))
                                        .thenThrow(new InvalidStatValueException("Value 0 is out of range"));

                        mockMvc.perform(put("/api/v1/stats/CAREER")
                                        .with(jwt().jwt(j -> j.subject(USER_ID.toString())))
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request)))
                                        .andExpect(status().isBadRequest());
                }

                @Test
                @DisplayName("returns 401 when not authenticated")
                void returns401_whenNotAuthenticated() throws Exception {
                        StatUpdateRequest request = new StatUpdateRequest(75, null);

                        mockMvc.perform(put("/api/v1/stats/CAREER")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request)))
                                        .andExpect(status().isUnauthorized());
                }

                @Test
                @DisplayName("returns 404 when stat not found")
                void returns404_whenStatNotFound() throws Exception {
                        StatUpdateRequest request = new StatUpdateRequest(75, null);

                        when(lifeStatService.updateStat(eq(USER_ID), eq(StatCategory.CAREER),
                                        any(StatUpdateRequest.class)))
                                        .thenThrow(new StatNotFoundException("Stat CAREER not found"));

                        mockMvc.perform(put("/api/v1/stats/CAREER")
                                        .with(jwt().jwt(j -> j.subject(USER_ID.toString())))
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request)))
                                        .andExpect(status().isNotFound());
                }
        }
}
