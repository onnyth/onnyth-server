package com.onnyth.onnythserver.repository;

import com.onnyth.onnythserver.models.FriendRequest;
import com.onnyth.onnythserver.models.FriendRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FriendRequestRepository extends JpaRepository<FriendRequest, UUID> {

    List<FriendRequest> findAllByReceiverIdAndStatus(UUID receiverId, FriendRequestStatus status);

    List<FriendRequest> findAllBySenderIdAndStatus(UUID senderId, FriendRequestStatus status);

    long countByReceiverIdAndStatus(UUID receiverId, FriendRequestStatus status);

    boolean existsBySenderIdAndReceiverIdAndStatus(UUID senderId, UUID receiverId, FriendRequestStatus status);

    Optional<FriendRequest> findBySenderIdAndReceiverId(UUID senderId, UUID receiverId);
}
