package com.onnyth.onnythserver.unit.service;

import com.onnyth.onnythserver.dto.LeaderboardResponse;
import com.onnyth.onnythserver.dto.UserLeaderboardPositionResponse;
import com.onnyth.onnythserver.models.RankTier;
import com.onnyth.onnythserver.models.User;
import com.onnyth.onnythserver.repository.*;
import com.onnyth.onnythserver.service.LeaderboardService;
import com.onnyth.onnythserver.service.LeaderboardSnapshotService;
import com.onnyth.onnythserver.support.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("LeaderboardService")
class LeaderboardServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private FriendshipRepository friendshipRepository;
    @Mock
    private UserOccupationRepository occupationRepository;
    @Mock
    private UserWealthRepository wealthRepository;
    @Mock
    private UserPhysiqueRepository physiqueRepository;
    @Mock
    private UserWisdomRepository wisdomRepository;
    @Mock
    private UserCharismaRepository charismaRepository;
    @Mock
    private LeaderboardSnapshotService snapshotService;

    @InjectMocks
    private LeaderboardService leaderboardService;

    private static final UUID USER_A = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID USER_B = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final UUID USER_C = UUID.fromString("00000000-0000-0000-0000-000000000003");

    private User userA, userB, userC;

    @BeforeEach
    void setUp() {
        userA = TestDataFactory.aUser().id(USER_A).username("alice").fullName("Alice").totalScore(500L)
                .rankTier(RankTier.GOLD).build();
        userB = TestDataFactory.aUser().id(USER_B).username("bob").fullName("Bob").totalScore(300L)
                .rankTier(RankTier.SILVER).build();
        userC = TestDataFactory.aUser().id(USER_C).username("charlie").fullName("Charlie").totalScore(700L)
                .rankTier(RankTier.PLATINUM).build();
    }

    @Nested
    @DisplayName("getFriendsLeaderboard")
    class GetFriendsLeaderboard {

        @Test
        @DisplayName("returns sorted leaderboard with current user marked")
        void returnsSortedLeaderboard() {
            when(friendshipRepository.findFriendIdsByUserId(USER_A))
                    .thenReturn(List.of(USER_B, USER_C));
            when(userRepository.findAllById(any()))
                    .thenReturn(List.of(userA, userB, userC));
            when(snapshotService.getPositionChanges(any(), any())).thenReturn(Map.of());
            when(snapshotService.getSnapshotUserIds(any())).thenReturn(Set.of());

            LeaderboardResponse response = leaderboardService.getFriendsLeaderboard(USER_A, PageRequest.of(0, 20));

            assertThat(response.entries()).hasSize(3);
            // Charlie (700) first, Alice (500) second, Bob (300) third
            assertThat(response.entries().get(0).username()).isEqualTo("charlie");
            assertThat(response.entries().get(0).position()).isEqualTo(1);
            assertThat(response.entries().get(1).username()).isEqualTo("alice");
            assertThat(response.entries().get(1).isCurrentUser()).isTrue();
            assertThat(response.entries().get(2).username()).isEqualTo("bob");
            assertThat(response.currentUserPosition()).isEqualTo(2);
            assertThat(response.currentUserScore()).isEqualTo(500L);
            assertThat(response.totalFriends()).isEqualTo(2);
        }

        @Test
        @DisplayName("returns empty entries for user with no friends")
        void emptyForNoFriends() {
            when(friendshipRepository.findFriendIdsByUserId(USER_A))
                    .thenReturn(List.of());
            when(userRepository.findAllById(any()))
                    .thenReturn(List.of(userA));
            when(snapshotService.getPositionChanges(any(), any())).thenReturn(Map.of());
            when(snapshotService.getSnapshotUserIds(any())).thenReturn(Set.of());

            LeaderboardResponse response = leaderboardService.getFriendsLeaderboard(USER_A, PageRequest.of(0, 20));

            assertThat(response.entries()).hasSize(1);
            assertThat(response.entries().get(0).isCurrentUser()).isTrue();
            assertThat(response.totalFriends()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("getUserPosition")
    class GetUserPosition {

        @Test
        @DisplayName("returns correct position and user ahead")
        void returnsPositionWithUserAhead() {
            when(friendshipRepository.findFriendIdsByUserId(USER_A))
                    .thenReturn(List.of(USER_B, USER_C));
            when(userRepository.findAllById(any()))
                    .thenReturn(List.of(userA, userB, userC));

            UserLeaderboardPositionResponse response = leaderboardService.getUserPosition(USER_A);

            assertThat(response.position()).isEqualTo(2); // Charlie is #1
            assertThat(response.totalParticipants()).isEqualTo(3);
            assertThat(response.score()).isEqualTo(500L);
            assertThat(response.pointsToNextPosition()).isEqualTo(200L); // 700 - 500
            assertThat(response.userAheadUsername()).isEqualTo("charlie");
            assertThat(response.userAheadId()).isEqualTo(USER_C);
        }

        @Test
        @DisplayName("returns zero gap when in first place")
        void firstPlace() {
            when(friendshipRepository.findFriendIdsByUserId(USER_C))
                    .thenReturn(List.of(USER_A, USER_B));
            when(userRepository.findAllById(any()))
                    .thenReturn(List.of(userA, userB, userC));

            UserLeaderboardPositionResponse response = leaderboardService.getUserPosition(USER_C);

            assertThat(response.position()).isEqualTo(1);
            assertThat(response.pointsToNextPosition()).isEqualTo(0);
            assertThat(response.userAheadUsername()).isNull();
        }
    }
}
