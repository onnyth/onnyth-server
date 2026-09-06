package com.onnyth.onnythserver.lifestats.application.port;

import com.onnyth.onnythserver.lifestats.domain.model.SocialPlatform;
import com.onnyth.onnythserver.lifestats.domain.model.UserSocialAccount;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserSocialAccountRepository {

    UserSocialAccount save(UserSocialAccount socialAccount);

    Optional<UserSocialAccount> findById(UUID id);

    List<UserSocialAccount> findAllByUserId(UUID userId);

    Optional<UserSocialAccount> findByUserIdAndPlatform(UUID userId, SocialPlatform platform);

    int getTotalFollowerCount(UUID userId);
}
