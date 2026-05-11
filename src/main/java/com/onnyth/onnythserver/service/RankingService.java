package com.onnyth.onnythserver.service;

import com.onnyth.onnythserver.models.User;
import com.onnyth.onnythserver.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Periodically computes and caches world and country rankings for all users.
 *
 * Rankings are stored directly on the users table (world_rank, country_rank)
 * so the client gets cached values instantly on every profile card load — no
 * expensive live ranking query needed.
 *
 * Schedule: every 15 minutes (configurable via property onnyth.ranking.interval-ms).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RankingService {

    private final UserRepository userRepository;

    /**
     * Recompute world ranks for all users.
     * Run on startup and every 15 minutes thereafter.
     */
    @Transactional
    @Scheduled(fixedDelayString = "${onnyth.ranking.interval-ms:900000}", initialDelay = 5000)
    public void computeWorldRanks() {
        List<User> users = userRepository.findAllOrderedByScoreDesc();
        for (int i = 0; i < users.size(); i++) {
            users.get(i).setWorldRank(i + 1);
        }
        userRepository.saveAll(users);
        log.info("[RankingService] World ranks updated for {} users", users.size());
    }

    /**
     * Recompute country ranks for each country bucket.
     * Run on startup and every 15 minutes thereafter (offset by 1 minute from world ranks).
     */
    @Transactional
    @Scheduled(fixedDelayString = "${onnyth.ranking.interval-ms:900000}", initialDelay = 65000)
    public void computeCountryRanks() {
        List<String> countries = userRepository.findDistinctCountries();
        int totalUpdated = 0;
        for (String country : countries) {
            List<User> countryUsers = userRepository.findByCountryOrderByScoreDesc(country);
            for (int i = 0; i < countryUsers.size(); i++) {
                countryUsers.get(i).setCountryRank(i + 1);
            }
            userRepository.saveAll(countryUsers);
            totalUpdated += countryUsers.size();
        }
        log.info("[RankingService] Country ranks updated for {} users across {} countries",
                totalUpdated, countries.size());
    }
}
