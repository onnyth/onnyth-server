package com.onnyth.onnythserver.lifestats.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface ProfileLikeJpaRepository extends JpaRepository<ProfileLikeEntity, ProfileLikeEntityId> {

    boolean existsByIdLikerIdAndIdLikedId(UUID likerId, UUID likedId);

    @Query("SELECT COUNT(p) FROM ProfileLikeEntity p WHERE p.id.likedId = :likedId")
    long countByLikedId(UUID likedId);

    @Modifying
    @Query("DELETE FROM ProfileLikeEntity p WHERE p.id.likerId = :likerId AND p.id.likedId = :likedId")
    void deleteByLikerIdAndLikedId(UUID likerId, UUID likedId);
}
