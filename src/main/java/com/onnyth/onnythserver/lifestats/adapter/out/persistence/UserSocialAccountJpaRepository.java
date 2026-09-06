package com.onnyth.onnythserver.lifestats.adapter.out.persistence;

import com.onnyth.onnythserver.lifestats.domain.model.SocialPlatform;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserSocialAccountJpaRepository extends JpaRepository<UserSocialAccountEntity, UUID> {

    List<UserSocialAccountEntity> findAllByUserId(UUID userId);

    Optional<UserSocialAccountEntity> findByUserIdAndPlatform(UUID userId, SocialPlatform platform);

    @Query("SELECT COALESCE(SUM(s.followerCount), 0) FROM UserSocialAccountEntity s WHERE s.userId = :userId")
    int getTotalFollowerCount(UUID userId);
}
