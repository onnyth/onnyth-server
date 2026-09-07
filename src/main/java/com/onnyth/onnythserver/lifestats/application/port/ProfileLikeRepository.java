package com.onnyth.onnythserver.lifestats.application.port;

import com.onnyth.onnythserver.lifestats.domain.model.ProfileLike;

import java.util.UUID;

public interface ProfileLikeRepository {

    ProfileLike save(ProfileLike profileLike);

    boolean existsByLikerIdAndLikedId(UUID likerId, UUID likedId);

    long countByLikedId(UUID likedId);

    void deleteByLikerIdAndLikedId(UUID likerId, UUID likedId);
}
