package com.onnyth.onnythserver.lifestats.adapter.out.persistence;

import com.onnyth.onnythserver.lifestats.application.port.UserSocialAccountRepository;
import com.onnyth.onnythserver.lifestats.domain.model.SocialPlatform;
import com.onnyth.onnythserver.lifestats.domain.model.UserSocialAccount;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class UserSocialAccountRepositoryAdapter implements UserSocialAccountRepository {

    private final UserSocialAccountJpaRepository userSocialAccountJpaRepository;

    @Override
    public UserSocialAccount save(UserSocialAccount socialAccount) {
        UserSocialAccountEntity saved = userSocialAccountJpaRepository.save(UserSocialAccountPersistenceMapper.toEntity(socialAccount));
        return UserSocialAccountPersistenceMapper.toDomain(saved);
    }

    @Override
    public Optional<UserSocialAccount> findById(UUID id) {
        return userSocialAccountJpaRepository.findById(id).map(UserSocialAccountPersistenceMapper::toDomain);
    }

    @Override
    public List<UserSocialAccount> findAllByUserId(UUID userId) {
        return userSocialAccountJpaRepository.findAllByUserId(userId).stream()
                .map(UserSocialAccountPersistenceMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<UserSocialAccount> findByUserIdAndPlatform(UUID userId, SocialPlatform platform) {
        return userSocialAccountJpaRepository.findByUserIdAndPlatform(userId, platform)
                .map(UserSocialAccountPersistenceMapper::toDomain);
    }

    @Override
    public int getTotalFollowerCount(UUID userId) {
        return userSocialAccountJpaRepository.getTotalFollowerCount(userId);
    }
}
