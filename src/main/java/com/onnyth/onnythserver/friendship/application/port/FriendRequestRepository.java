package com.onnyth.onnythserver.friendship.application.port;

import com.onnyth.onnythserver.friendship.domain.model.FriendRequest;
import com.onnyth.onnythserver.friendship.domain.model.FriendRequestStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FriendRequestRepository {

    FriendRequest save(FriendRequest friendRequest);

    Optional<FriendRequest> findById(UUID id);

    List<FriendRequest> findAllByReceiverIdAndStatus(UUID receiverId, FriendRequestStatus status);

    List<FriendRequest> findAllBySenderIdAndStatus(UUID senderId, FriendRequestStatus status);

    long countByReceiverIdAndStatus(UUID receiverId, FriendRequestStatus status);

    boolean existsBySenderIdAndReceiverIdAndStatus(UUID senderId, UUID receiverId, FriendRequestStatus status);

    Optional<FriendRequest> findBySenderIdAndReceiverId(UUID senderId, UUID receiverId);

    void deleteAllBySenderId(UUID senderId);

    void deleteAllByReceiverId(UUID receiverId);
}
