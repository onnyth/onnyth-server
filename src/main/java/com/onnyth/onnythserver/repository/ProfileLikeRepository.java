package com.onnyth.onnythserver.repository;

import com.onnyth.onnythserver.models.ProfileLike;
import com.onnyth.onnythserver.models.ProfileLikeId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ProfileLikeRepository extends JpaRepository<ProfileLike, ProfileLikeId> {

    boolean existsByLikerIdAndLikedId(UUID likerId, UUID likedId);

    long countByLikedId(UUID likedId);

    void deleteByLikerIdAndLikedId(UUID likerId, UUID likedId);
}
