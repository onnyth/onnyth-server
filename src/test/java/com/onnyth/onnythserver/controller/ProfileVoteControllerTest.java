package com.onnyth.onnythserver.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onnyth.onnythserver.dto.VoteResponse;
import com.onnyth.onnythserver.security.SecurityConfig;
import com.onnyth.onnythserver.service.ProfileVoteService;
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

import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProfileVoteController.class)
@Import({ SecurityConfig.class, MockJwtDecoderConfig.class })
@DisplayName("ProfileVoteController")
class ProfileVoteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProfileVoteService voteService;

    private static final UUID VOTER_ID  = UUID.randomUUID();
    private static final UUID TARGET_ID = UUID.randomUUID();

    private VoteResponse sampleResponse(Boolean myVote, int score) {
        return VoteResponse.builder()
                .targetUserId(TARGET_ID)
                .voteScore(score)
                .myVote(myVote)
                .build();
    }

    // ─── Auth guard ───────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Auth")
    class Auth {

        @Test @DisplayName("POST /votes without JWT → 401")
        void postVote_noJwt_401() throws Exception {
            mockMvc.perform(post("/api/v1/votes")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"targetUserId\":\"" + TARGET_ID + "\",\"isUpvote\":true}"))
                    .andExpect(status().isUnauthorized());
        }

        @Test @DisplayName("DELETE /votes/{id} without JWT → 401")
        void deleteVote_noJwt_401() throws Exception {
            mockMvc.perform(delete("/api/v1/votes/" + TARGET_ID))
                    .andExpect(status().isUnauthorized());
        }

        @Test @DisplayName("GET /votes/{id} without JWT → 401")
        void getVote_noJwt_401() throws Exception {
            mockMvc.perform(get("/api/v1/votes/" + TARGET_ID))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ─── Cast vote ────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("POST /votes — cast/update")
    class CastVote {

        @Test @DisplayName("upvote → 200 with voteScore and myVote=true")
        void upvote_200() throws Exception {
            when(voteService.castVote(any(), eq(TARGET_ID), eq(true)))
                    .thenReturn(sampleResponse(true, 5));

            mockMvc.perform(post("/api/v1/votes")
                            .with(jwt().jwt(j -> j.subject(VOTER_ID.toString())))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    Map.of("targetUserId", TARGET_ID, "isUpvote", true))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.voteScore").value(5))
                    .andExpect(jsonPath("$.myVote").value(true));
        }

        @Test @DisplayName("downvote → 200 with myVote=false")
        void downvote_200() throws Exception {
            when(voteService.castVote(any(), eq(TARGET_ID), eq(false)))
                    .thenReturn(sampleResponse(false, -2));

            mockMvc.perform(post("/api/v1/votes")
                            .with(jwt().jwt(j -> j.subject(VOTER_ID.toString())))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    Map.of("targetUserId", TARGET_ID, "isUpvote", false))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.myVote").value(false));
        }

        @Test @DisplayName("self-vote → service throws → 500 propagated (guard is in service)")
        void selfVote_delegatesToService() throws Exception {
            when(voteService.castVote(any(), any(), eq(true)))
                    .thenThrow(new IllegalArgumentException("Users cannot vote on their own profile"));

            mockMvc.perform(post("/api/v1/votes")
                            .with(jwt().jwt(j -> j.subject(VOTER_ID.toString())))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    Map.of("targetUserId", VOTER_ID, "isUpvote", true))))
                    .andExpect(status().is5xxServerError());
        }

        @Test @DisplayName("null isUpvote field → 400 via @Valid")
        void nullIsUpvote_400() throws Exception {
            mockMvc.perform(post("/api/v1/votes")
                            .with(jwt().jwt(j -> j.subject(VOTER_ID.toString())))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    Map.of("targetUserId", TARGET_ID)))) // isUpvote omitted
                    .andExpect(status().isBadRequest());
        }
    }

    // ─── Remove vote ──────────────────────────────────────────────────────────

    @Nested
    @DisplayName("DELETE /votes/{id} — remove")
    class RemoveVote {

        @Test @DisplayName("existing vote → 200 with myVote=null")
        void remove_200() throws Exception {
            when(voteService.removeVote(any(), eq(TARGET_ID)))
                    .thenReturn(sampleResponse(null, 3));

            mockMvc.perform(delete("/api/v1/votes/" + TARGET_ID)
                            .with(jwt().jwt(j -> j.subject(VOTER_ID.toString()))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.myVote").doesNotExist());
        }

        @Test @DisplayName("no prior vote → 200 idempotent")
        void removeNoVote_200() throws Exception {
            when(voteService.removeVote(any(), eq(TARGET_ID)))
                    .thenReturn(sampleResponse(null, 0));

            mockMvc.perform(delete("/api/v1/votes/" + TARGET_ID)
                            .with(jwt().jwt(j -> j.subject(VOTER_ID.toString()))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.voteScore").value(0));
        }
    }

    // ─── Get my vote ──────────────────────────────────────────────────────────

    @Nested
    @DisplayName("GET /votes/{id} — get current vote")
    class GetMyVote {

        @Test @DisplayName("has upvote → myVote=true")
        void getVote_upvote() throws Exception {
            when(voteService.getMyVote(any(), eq(TARGET_ID)))
                    .thenReturn(sampleResponse(true, 10));

            mockMvc.perform(get("/api/v1/votes/" + TARGET_ID)
                            .with(jwt().jwt(j -> j.subject(VOTER_ID.toString()))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.myVote").value(true));
        }

        @Test @DisplayName("no vote → myVote absent (null)")
        void getVote_null() throws Exception {
            when(voteService.getMyVote(any(), eq(TARGET_ID)))
                    .thenReturn(sampleResponse(null, 0));

            mockMvc.perform(get("/api/v1/votes/" + TARGET_ID)
                            .with(jwt().jwt(j -> j.subject(VOTER_ID.toString()))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.targetUserId").value(TARGET_ID.toString()));
        }

        @Test @DisplayName("has downvote → myVote=false")
        void getVote_downvote() throws Exception {
            when(voteService.getMyVote(any(), eq(TARGET_ID)))
                    .thenReturn(sampleResponse(false, -1));

            mockMvc.perform(get("/api/v1/votes/" + TARGET_ID)
                            .with(jwt().jwt(j -> j.subject(VOTER_ID.toString()))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.myVote").value(false));
        }
    }
}
