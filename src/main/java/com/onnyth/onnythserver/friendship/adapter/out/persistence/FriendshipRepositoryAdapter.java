package com.onnyth.onnythserver.friendship.adapter.out.persistence;

import com.onnyth.onnythserver.friendship.application.port.FriendshipRepository;
import com.onnyth.onnythserver.friendship.domain.model.Friendship;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class FriendshipRepositoryAdapter implements FriendshipRepository {

    private final FriendshipJpaRepository friendshipJpaRepository;

    @Override
    public Friendship save(Friendship friendship) {
        FriendshipEntity saved = friendshipJpaRepository.save(FriendshipPersistenceMapper.toEntity(friendship));
        return FriendshipPersistenceMapper.toDomain(saved);
    }

    @Override
    public Page<Friendship> findAllByUserId(UUID userId, Pageable pageable) {
        return friendshipJpaRepository.findAllByUserId(userId, pageable)
                .map(FriendshipPersistenceMapper::toDomain);
    }

    @Override
    public boolean existsByUserIdAndFriendId(UUID userId, UUID friendId) {
        return friendshipJpaRepository.existsByUserIdAndFriendId(userId, friendId);
    }

    @Override
    public void deleteByUserIdAndFriendId(UUID userId, UUID friendId) {
        friendshipJpaRepository.deleteByUserIdAndFriendId(userId, friendId);
    }

    @Override
    public List<Friendship> searchFriends(UUID userId, String query) {
        return friendshipJpaRepository.searchFriends(userId, query).stream()
                .map(FriendshipPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public List<Friendship> findAllByUserId(UUID userId) {
        return friendshipJpaRepository.findAllByUserId(userId).stream()
                .map(FriendshipPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public List<UUID> findFriendIdsByUserId(UUID userId) {
        return friendshipJpaRepository.findFriendIdsByUserId(userId);
    }

    @Override
    public void deleteAllByUserId(UUID userId) {
        friendshipJpaRepository.deleteAllByUserId(userId);
    }

    @Override
    public void deleteAllByFriendId(UUID friendId) {
        friendshipJpaRepository.deleteAllByFriendId(friendId);
    }
}
