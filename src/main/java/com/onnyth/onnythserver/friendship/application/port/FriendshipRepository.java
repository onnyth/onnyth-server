package com.onnyth.onnythserver.friendship.application.port;

import com.onnyth.onnythserver.friendship.domain.model.Friendship;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface FriendshipRepository {

    Friendship save(Friendship friendship);

    Page<Friendship> findAllByUserId(UUID userId, Pageable pageable);

    boolean existsByUserIdAndFriendId(UUID userId, UUID friendId);

    void deleteByUserIdAndFriendId(UUID userId, UUID friendId);

    List<Friendship> searchFriends(UUID userId, String query);

    List<Friendship> findAllByUserId(UUID userId);

    List<UUID> findFriendIdsByUserId(UUID userId);

    void deleteAllByUserId(UUID userId);

    void deleteAllByFriendId(UUID friendId);
}
