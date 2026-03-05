package com.onnyth.onnythserver.repository;

import com.onnyth.onnythserver.models.Friendship;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface FriendshipRepository extends JpaRepository<Friendship, UUID> {

    Page<Friendship> findAllByUserId(UUID userId, Pageable pageable);

    boolean existsByUserIdAndFriendId(UUID userId, UUID friendId);

    void deleteByUserIdAndFriendId(UUID userId, UUID friendId);

    @Query("SELECT f FROM Friendship f JOIN User u ON f.friendId = u.id " +
            "WHERE f.userId = :userId AND (LOWER(u.username) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<Friendship> searchFriends(@Param("userId") UUID userId, @Param("query") String query);

    List<Friendship> findAllByUserId(UUID userId);

    @Query("SELECT f.friendId FROM Friendship f WHERE f.userId = :userId")
    List<UUID> findFriendIdsByUserId(@Param("userId") UUID userId);
}
