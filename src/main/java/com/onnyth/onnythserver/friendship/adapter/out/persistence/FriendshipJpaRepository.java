package com.onnyth.onnythserver.friendship.adapter.out.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface FriendshipJpaRepository extends JpaRepository<FriendshipEntity, UUID> {

    Page<FriendshipEntity> findAllByUserId(UUID userId, Pageable pageable);

    boolean existsByUserIdAndFriendId(UUID userId, UUID friendId);

    void deleteByUserIdAndFriendId(UUID userId, UUID friendId);

    @Query("SELECT f FROM FriendshipEntity f JOIN UserEntity u ON f.friendId = u.id " +
            "WHERE f.userId = :userId AND (LOWER(u.username) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<FriendshipEntity> searchFriends(@Param("userId") UUID userId, @Param("query") String query);

    List<FriendshipEntity> findAllByUserId(UUID userId);

    @Query("SELECT f.friendId FROM FriendshipEntity f WHERE f.userId = :userId")
    List<UUID> findFriendIdsByUserId(@Param("userId") UUID userId);

    void deleteAllByUserId(UUID userId);

    void deleteAllByFriendId(UUID friendId);
}
