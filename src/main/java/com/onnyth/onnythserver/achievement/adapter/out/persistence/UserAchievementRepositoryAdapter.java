package com.onnyth.onnythserver.achievement.adapter.out.persistence;

import com.onnyth.onnythserver.achievement.application.port.UserAchievementRepository;
import com.onnyth.onnythserver.achievement.domain.model.UserAchievement;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class UserAchievementRepositoryAdapter implements UserAchievementRepository {

    private final UserAchievementJpaRepository userAchievementJpaRepository;

    @Override
    public UserAchievement save(UserAchievement userAchievement) {
        UserAchievementEntity saved = userAchievementJpaRepository.save(UserAchievementPersistenceMapper.toEntity(userAchievement));
        return UserAchievementPersistenceMapper.toDomain(saved);
    }

    @Override
    public List<UserAchievement> findAllByUserId(UUID userId) {
        return userAchievementJpaRepository.findAllByUserId(userId).stream()
                .map(UserAchievementPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<UserAchievement> findByUserIdAndAchievementId(UUID userId, UUID achievementId) {
        return userAchievementJpaRepository.findByUserIdAndAchievementId(userId, achievementId)
                .map(UserAchievementPersistenceMapper::toDomain);
    }

    @Override
    public int countByUserId(UUID userId) {
        return userAchievementJpaRepository.countByUserId(userId);
    }

    @Override
    public boolean existsByUserIdAndAchievementId(UUID userId, UUID achievementId) {
        return userAchievementJpaRepository.existsByUserIdAndAchievementId(userId, achievementId);
    }

    @Override
    public void deleteAllByUserId(UUID userId) {
        userAchievementJpaRepository.deleteAllByUserId(userId);
    }
}
