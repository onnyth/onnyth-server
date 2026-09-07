package com.onnyth.onnythserver.streak.application.port;

import com.onnyth.onnythserver.streak.domain.model.UserStreak;

import java.util.Optional;
import java.util.UUID;

public interface UserStreakRepository {

    UserStreak save(UserStreak userStreak);

    Optional<UserStreak> findByUserId(UUID userId);

    void deleteByUserId(UUID userId);
}
