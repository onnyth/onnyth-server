package com.onnyth.onnythserver.service;

import com.onnyth.onnythserver.dto.LeaderboardEntryResponse;
import com.onnyth.onnythserver.dto.LeaderboardResponse;
import com.onnyth.onnythserver.models.User;
import com.onnyth.onnythserver.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Service for computing leaderboard rankings from existing user scores.
 * Uses direct queries on the users table — no separate leaderboard entity.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LeaderboardService {

    private static final int MAX_LIMIT = 100;
    private static final int DEFAULT_LIMIT = 50;

    private final UserRepository userRepository;

    /**
     * Get the global leaderboard sorted by total score descending.
     *
     * @param userId the requesting user's ID (nullable — for anonymous/public
     *               access)
     * @param limit  max entries to return (1–100, default 50)
     * @param offset pagination offset (0-based)
     * @return LeaderboardResponse with entries, user's own rank, and total count
     */
    @Transactional(readOnly = true)
    public LeaderboardResponse getGlobalLeaderboard(UUID userId, int limit, int offset) {
        int effectiveLimit = Math.max(1, Math.min(limit <= 0 ? DEFAULT_LIMIT : limit, MAX_LIMIT));
        int effectiveOffset = Math.max(0, offset);

        int pageNumber = effectiveOffset / effectiveLimit;
        PageRequest pageRequest = PageRequest.of(pageNumber, effectiveLimit);

        Page<User> page = userRepository.findAllByOrderByTotalScoreDesc(pageRequest);
        long totalUsers = userRepository.count();

        int startRank = effectiveOffset + 1;
        List<LeaderboardEntryResponse> entries = new ArrayList<>();
        int rank = startRank;
        for (User user : page.getContent()) {
            entries.add(LeaderboardEntryResponse.fromUser(user, rank));
            rank++;
        }

        Integer userRank = null;
        if (userId != null) {
            userRank = getUserRank(userId);
        }

        log.info("Leaderboard fetched: {} entries, offset={}, totalUsers={}", entries.size(), effectiveOffset,
                totalUsers);

        return LeaderboardResponse.builder()
                .entries(entries)
                .userRank(userRank)
                .totalUsers(totalUsers)
                .build();
    }

    /**
     * Compute a user's global rank position (1-based).
     * Rank = number of users with a strictly higher score + 1.
     *
     * @return the user's rank, or null if user not found
     */
    @Transactional(readOnly = true)
    public Integer getUserRank(UUID userId) {
        return userRepository.findById(userId)
                .map(user -> {
                    long usersAbove = userRepository.countByTotalScoreGreaterThan(user.getTotalScore());
                    return (int) usersAbove + 1;
                })
                .orElse(null);
    }
}
