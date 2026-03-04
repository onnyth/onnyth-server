package com.onnyth.onnythserver.controller;

import com.onnyth.onnythserver.dto.FriendProfileResponse;
import com.onnyth.onnythserver.dto.FriendRequestResponse;
import com.onnyth.onnythserver.dto.FriendResponse;
import com.onnyth.onnythserver.dto.StatComparisonResponse;
import com.onnyth.onnythserver.exceptions.*;
import com.onnyth.onnythserver.security.SecurityConfig;
import com.onnyth.onnythserver.service.FriendshipService;
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

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FriendController.class)
@Import({ SecurityConfig.class, MockJwtDecoderConfig.class })
@DisplayName("FriendController")
class FriendControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FriendshipService friendshipService;

    private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID FRIEND_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final UUID REQUEST_ID = UUID.fromString("00000000-0000-0000-0000-000000000099");

    @Nested
    @DisplayName("POST /api/v1/friends/request/{userId}")
    class SendRequest {

        @Test
        @DisplayName("returns 200 on success")
        void returns200() throws Exception {
            FriendRequestResponse response = FriendRequestResponse.builder()
                    .requestId(REQUEST_ID).senderId(USER_ID).receiverId(FRIEND_ID)
                    .senderUsername("alice").status("PENDING").createdAt(Instant.now()).build();

            when(friendshipService.sendFriendRequest(USER_ID, FRIEND_ID)).thenReturn(response);

            mockMvc.perform(post("/api/v1/friends/request/" + FRIEND_ID)
                    .with(jwt().jwt(j -> j.subject(USER_ID.toString()))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("PENDING"));
        }

        @Test
        @DisplayName("returns 409 when already friends")
        void returns409() throws Exception {
            when(friendshipService.sendFriendRequest(USER_ID, FRIEND_ID))
                    .thenThrow(new AlreadyFriendsException("Already friends"));

            mockMvc.perform(post("/api/v1/friends/request/" + FRIEND_ID)
                            .with(jwt().jwt(j -> j.subject(USER_ID.toString()))))
                    .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("returns 401 when not authenticated")
        void returns401() throws Exception {
            mockMvc.perform(post("/api/v1/friends/request/" + FRIEND_ID))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/friends/request/{id}/accept")
    class AcceptRequest {

        @Test
        @DisplayName("returns 200 on success")
        void returns200() throws Exception {
            FriendRequestResponse response = FriendRequestResponse.builder()
                    .requestId(REQUEST_ID).status("ACCEPTED").createdAt(Instant.now()).build();
            when(friendshipService.acceptFriendRequest(REQUEST_ID, USER_ID)).thenReturn(response);

            mockMvc.perform(put("/api/v1/friends/request/" + REQUEST_ID + "/accept")
                    .with(jwt().jwt(j -> j.subject(USER_ID.toString()))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("ACCEPTED"));
        }

        @Test
        @DisplayName("returns 403 when non-receiver")
        void returns403() throws Exception {
            when(friendshipService.acceptFriendRequest(REQUEST_ID, USER_ID))
                    .thenThrow(new UnauthorizedFriendRequestActionException("Not the receiver"));

            mockMvc.perform(put("/api/v1/friends/request/" + REQUEST_ID + "/accept")
                            .with(jwt().jwt(j -> j.subject(USER_ID.toString()))))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/friends")
    class GetFriends {

        @Test
        @DisplayName("returns paginated friends list")
        void returns200() throws Exception {
            FriendResponse friend = FriendResponse.builder()
                    .userId(FRIEND_ID).username("bob").totalScore(300)
                    .friendSince(Instant.now()).build();
            when(friendshipService.getFriends(eq(USER_ID), any()))
                    .thenReturn(new PageImpl<>(List.of(friend)));

            mockMvc.perform(get("/api/v1/friends")
                    .with(jwt().jwt(j -> j.subject(USER_ID.toString()))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].username").value("bob"));
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/friends/{userId}")
    class RemoveFriend {

        @Test
        @DisplayName("returns 204 on success")
        void returns204() throws Exception {
            doNothing().when(friendshipService).removeFriend(USER_ID, FRIEND_ID);

            mockMvc.perform(delete("/api/v1/friends/" + FRIEND_ID)
                    .with(jwt().jwt(j -> j.subject(USER_ID.toString()))))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("returns 400 when not friends")
        void returns400() throws Exception {
            doThrow(new NotFriendsException("Not friends"))
                    .when(friendshipService).removeFriend(USER_ID, FRIEND_ID);

            mockMvc.perform(delete("/api/v1/friends/" + FRIEND_ID)
                    .with(jwt().jwt(j -> j.subject(USER_ID.toString()))))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/friends/{userId}/profile")
    class GetFriendProfile {

        @Test
        @DisplayName("returns 200 with friend profile and comparison")
        void returns200() throws Exception {
            FriendProfileResponse response = FriendProfileResponse.builder()
                    .userId(FRIEND_ID).username("bob").totalScore(300)
                    .comparison(StatComparisonResponse.builder()
                            .scoreDifference(200).higherIn(List.of("Career"))
                            .lowerIn(List.of()).build())
                    .stats(List.of()).build();
            when(friendshipService.getFriendProfile(USER_ID, FRIEND_ID)).thenReturn(response);

            mockMvc.perform(get("/api/v1/friends/" + FRIEND_ID + "/profile")
                    .with(jwt().jwt(j -> j.subject(USER_ID.toString()))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.username").value("bob"))
                    .andExpect(jsonPath("$.comparison.scoreDifference").value(200));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/friends/requests/count")
    class GetRequestCount {

        @Test
        @DisplayName("returns pending count")
        void returnsPendingCount() throws Exception {
            when(friendshipService.getPendingRequestCount(USER_ID)).thenReturn(5L);

            mockMvc.perform(get("/api/v1/friends/requests/count")
                            .with(jwt().jwt(j -> j.subject(USER_ID.toString()))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.count").value(5));
        }
    }
}
