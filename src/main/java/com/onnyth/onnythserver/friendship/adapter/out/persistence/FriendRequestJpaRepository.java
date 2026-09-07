package com.onnyth.onnythserver.friendship.adapter.out.persistence;

import com.onnyth.onnythserver.friendship.domain.model.FriendRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FriendRequestJpaRepository extends JpaRepository<FriendRequestEntity, UUID> {

    List<FriendRequestEntity> findAllByReceiverIdAndStatus(UUID receiverId, FriendRequestStatus status);

    List<FriendRequestEntity> findAllBySenderIdAndStatus(UUID senderId, FriendRequestStatus status);

    long countByReceiverIdAndStatus(UUID receiverId, FriendRequestStatus status);

    boolean existsBySenderIdAndReceiverIdAndStatus(UUID senderId, UUID receiverId, FriendRequestStatus status);

    Optional<FriendRequestEntity> findBySenderIdAndReceiverId(UUID senderId, UUID receiverId);

    void deleteAllBySenderId(UUID senderId);

    void deleteAllByReceiverId(UUID receiverId);
}
