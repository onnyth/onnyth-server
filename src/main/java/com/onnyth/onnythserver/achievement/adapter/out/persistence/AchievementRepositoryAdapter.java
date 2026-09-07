package com.onnyth.onnythserver.achievement.adapter.out.persistence;

import com.onnyth.onnythserver.achievement.application.port.AchievementRepository;
import com.onnyth.onnythserver.achievement.domain.model.Achievement;
import com.onnyth.onnythserver.achievement.domain.model.AchievementCategory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class AchievementRepositoryAdapter implements AchievementRepository {

    private final AchievementJpaRepository achievementJpaRepository;

    @Override
    public Achievement save(Achievement achievement) {
        AchievementEntity saved = achievementJpaRepository.save(AchievementPersistenceMapper.toEntity(achievement));
        return AchievementPersistenceMapper.toDomain(saved);
    }

    @Override
    public Optional<Achievement> findById(UUID id) {
        return achievementJpaRepository.findById(id).map(AchievementPersistenceMapper::toDomain);
    }

    @Override
    public List<Achievement> findAllById(Iterable<UUID> ids) {
        return achievementJpaRepository.findAllById(ids).stream()
                .map(AchievementPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public List<Achievement> findAllByCategory(AchievementCategory category) {
        return achievementJpaRepository.findAllByCategory(category).stream()
                .map(AchievementPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<Achievement> findByCode(String code) {
        return achievementJpaRepository.findByCode(code).map(AchievementPersistenceMapper::toDomain);
    }

    @Override
    public List<Achievement> findAllByIsActiveTrue() {
        return achievementJpaRepository.findAllByIsActiveTrue().stream()
                .map(AchievementPersistenceMapper::toDomain)
                .toList();
    }
}
