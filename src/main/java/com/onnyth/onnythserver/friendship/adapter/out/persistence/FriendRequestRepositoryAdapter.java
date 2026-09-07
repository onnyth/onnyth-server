package com.onnyth.onnythserver.friendship.adapter.out.persistence;

import com.onnyth.onnythserver.friendship.application.port.FriendRequestRepository;
import com.onnyth.onnythserver.friendship.domain.model.FriendRequest;
import com.onnyth.onnythserver.friendship.domain.model.FriendRequestStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class FriendRequestRepositoryAdapter implements FriendRequestRepository {

    private final FriendRequestJpaRepository friendRequestJpaRepository;

    @Override
    public FriendRequest save(FriendRequest friendRequest) {
        FriendRequestEntity saved = friendRequestJpaRepository.save(FriendRequestPersistenceMapper.toEntity(friendRequest));
        return FriendRequestPersistenceMapper.toDomain(saved);
    }

    @Override
    public Optional<FriendRequest> findById(UUID id) {
        return friendRequestJpaRepository.findById(id).map(FriendRequestPersistenceMapper::toDomain);
    }

    @Override
    public List<FriendRequest> findAllByReceiverIdAndStatus(UUID receiverId, FriendRequestStatus status) {
        return friendRequestJpaRepository.findAllByReceiverIdAndStatus(receiverId, status).stream()
                .map(FriendRequestPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public List<FriendRequest> findAllBySenderIdAndStatus(UUID senderId, FriendRequestStatus status) {
        return friendRequestJpaRepository.findAllBySenderIdAndStatus(senderId, status).stream()
                .map(FriendRequestPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public long countByReceiverIdAndStatus(UUID receiverId, FriendRequestStatus status) {
        return friendRequestJpaRepository.countByReceiverIdAndStatus(receiverId, status);
    }

    @Override
    public boolean existsBySenderIdAndReceiverIdAndStatus(UUID senderId, UUID receiverId, FriendRequestStatus status) {
        return friendRequestJpaRepository.existsBySenderIdAndReceiverIdAndStatus(senderId, receiverId, status);
    }

    @Override
    public Optional<FriendRequest> findBySenderIdAndReceiverId(UUID senderId, UUID receiverId) {
        return friendRequestJpaRepository.findBySenderIdAndReceiverId(senderId, receiverId)
                .map(FriendRequestPersistenceMapper::toDomain);
    }

    @Override
    public void deleteAllBySenderId(UUID senderId) {
        friendRequestJpaRepository.deleteAllBySenderId(senderId);
    }

    @Override
    public void deleteAllByReceiverId(UUID receiverId) {
        friendRequestJpaRepository.deleteAllByReceiverId(receiverId);
    }
}
