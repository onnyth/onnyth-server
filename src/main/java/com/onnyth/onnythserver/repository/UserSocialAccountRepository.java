package com.onnyth.onnythserver.repository;

import com.onnyth.onnythserver.models.SocialPlatform;
import com.onnyth.onnythserver.models.UserSocialAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserSocialAccountRepository extends JpaRepository<UserSocialAccount, UUID> {

    List<UserSocialAccount> findAllByUserId(UUID userId);

    Optional<UserSocialAccount> findByUserIdAndPlatform(UUID userId, SocialPlatform platform);

    @Query("SELECT COALESCE(SUM(s.followerCount), 0) FROM UserSocialAccount s WHERE s.userId = :userId")
    int getTotalFollowerCount(UUID userId);
}
