package com.onnyth.onnythserver.unit.service;

import com.onnyth.onnythserver.dto.LeaderboardEntryResponse;
import com.onnyth.onnythserver.dto.LeaderboardResponse;
import com.onnyth.onnythserver.models.RankTier;
import com.onnyth.onnythserver.models.User;
import com.onnyth.onnythserver.repository.UserRepository;
import com.onnyth.onnythserver.service.LeaderboardService;
import com.onnyth.onnythserver.support.TestDataFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("LeaderboardService")
class LeaderboardServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private LeaderboardService leaderboardService;

    private User buildRankedUser(String username, long score, RankTier tier) {
        return TestDataFactory.aUser()
                .username(username)
                .fullName(username + " User")
                .totalScore(score)
                .rankTier(tier)
                .build();
    }

    @Nested
    @DisplayName("getGlobalLeaderboard")
    class GetGlobalLeaderboard {

        @Test
        @DisplayName("returns ranked entries sorted by score descending")
        void returnsRankedEntries() {
            User user1 = buildRankedUser("alice", 500, RankTier.PLATINUM);
            User user2 = buildRankedUser("bob", 300, RankTier.GOLD);
            User user3 = buildRankedUser("charlie", 100, RankTier.SILVER);

            Page<User> page = new PageImpl<>(List.of(user1, user2, user3));
            when(userRepository.findAllByOrderByTotalScoreDesc(any(PageRequest.class))).thenReturn(page);
            when(userRepository.count()).thenReturn(3L);

            LeaderboardResponse response = leaderboardService.getGlobalLeaderboard(null, 50, 0);

            assertThat(response.entries()).hasSize(3);
            assertThat(response.entries().get(0).rank()).isEqualTo(1);
            assertThat(response.entries().get(0).username()).isEqualTo("alice");
            assertThat(response.entries().get(0).totalScore()).isEqualTo(500);
            assertThat(response.entries().get(1).rank()).isEqualTo(2);
            assertThat(response.entries().get(2).rank()).isEqualTo(3);
            assertThat(response.totalUsers()).isEqualTo(3);
            assertThat(response.userRank()).isNull();
        }

        @Test
        @DisplayName("includes user's own rank when userId is provided")
        void includesUserRank() {
            UUID userId = UUID.randomUUID();
            User currentUser = TestDataFactory.aUser().id(userId).totalScore(300L).rankTier(RankTier.GOLD).build();

            Page<User> page = new PageImpl<>(List.of());
            when(userRepository.findAllByOrderByTotalScoreDesc(any(PageRequest.class))).thenReturn(page);
            when(userRepository.count()).thenReturn(10L);
            when(userRepository.findById(userId)).thenReturn(Optional.of(currentUser));
            when(userRepository.countByTotalScoreGreaterThan(300L)).thenReturn(2L);

            LeaderboardResponse response = leaderboardService.getGlobalLeaderboard(userId, 50, 0);

            assertThat(response.userRank()).isEqualTo(3); // 2 users above + 1
        }

        @Test
        @DisplayName("clamps limit to max 100")
        void clampsLimitToMax() {
            Page<User> page = new PageImpl<>(List.of());
            when(userRepository.findAllByOrderByTotalScoreDesc(any(PageRequest.class))).thenReturn(page);
            when(userRepository.count()).thenReturn(0L);

            leaderboardService.getGlobalLeaderboard(null, 200, 0);

            // Should not throw — limit clamped to 100
        }

        @Test
        @DisplayName("defaults negative limit and offset to safe values")
        void defaultsNegativeValues() {
            Page<User> page = new PageImpl<>(List.of());
            when(userRepository.findAllByOrderByTotalScoreDesc(any(PageRequest.class))).thenReturn(page);
            when(userRepository.count()).thenReturn(0L);

            LeaderboardResponse response = leaderboardService.getGlobalLeaderboard(null, -1, -5);

            assertThat(response.entries()).isEmpty();
        }

        @Test
        @DisplayName("calculates correct ranks for paginated results with offset")
        void correctRanksWithOffset() {
            User user = buildRankedUser("page2user", 200, RankTier.SILVER);

            Page<User> page = new PageImpl<>(List.of(user));
            when(userRepository.findAllByOrderByTotalScoreDesc(any(PageRequest.class))).thenReturn(page);
            when(userRepository.count()).thenReturn(100L);

            LeaderboardResponse response = leaderboardService.getGlobalLeaderboard(null, 10, 10);

            assertThat(response.entries().get(0).rank()).isEqualTo(11); // offset 10 → first rank is 11
        }
    }

    @Nested
    @DisplayName("getUserRank")
    class GetUserRank {

        @Test
        @DisplayName("returns correct rank based on users with higher scores")
        void returnsCorrectRank() {
            UUID userId = UUID.randomUUID();
            User user = TestDataFactory.aUser().id(userId).totalScore(250L).build();

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(userRepository.countByTotalScoreGreaterThan(250L)).thenReturn(5L);

            Integer rank = leaderboardService.getUserRank(userId);

            assertThat(rank).isEqualTo(6); // 5 above + 1
        }

        @Test
        @DisplayName("returns 1 when user has highest score")
        void returnsOneForTopUser() {
            UUID userId = UUID.randomUUID();
            User user = TestDataFactory.aUser().id(userId).totalScore(1000L).build();

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(userRepository.countByTotalScoreGreaterThan(1000L)).thenReturn(0L);

            Integer rank = leaderboardService.getUserRank(userId);

            assertThat(rank).isEqualTo(1);
        }

        @Test
        @DisplayName("returns null when user not found")
        void returnsNullForMissingUser() {
            UUID userId = UUID.randomUUID();
            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            Integer rank = leaderboardService.getUserRank(userId);

            assertThat(rank).isNull();
        }
    }
}
