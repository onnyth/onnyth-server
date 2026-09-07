package com.onnyth.onnythserver.lifestats.adapter.out.persistence;

import com.onnyth.onnythserver.lifestats.application.port.ProfileLikeRepository;
import com.onnyth.onnythserver.lifestats.domain.model.ProfileLike;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class ProfileLikeRepositoryAdapter implements ProfileLikeRepository {

    private final ProfileLikeJpaRepository profileLikeJpaRepository;

    @Override
    public ProfileLike save(ProfileLike profileLike) {
        ProfileLikeEntity saved = profileLikeJpaRepository.save(ProfileLikePersistenceMapper.toEntity(profileLike));
        return ProfileLikePersistenceMapper.toDomain(saved);
    }

    @Override
    public boolean existsByLikerIdAndLikedId(UUID likerId, UUID likedId) {
        return profileLikeJpaRepository.existsByIdLikerIdAndIdLikedId(likerId, likedId);
    }

    @Override
    public long countByLikedId(UUID likedId) {
        return profileLikeJpaRepository.countByLikedId(likedId);
    }

    @Override
    public void deleteByLikerIdAndLikedId(UUID likerId, UUID likedId) {
        profileLikeJpaRepository.deleteByLikerIdAndLikedId(likerId, likedId);
    }
}
