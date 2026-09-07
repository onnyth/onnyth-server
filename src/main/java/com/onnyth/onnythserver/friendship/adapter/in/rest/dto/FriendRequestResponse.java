package com.onnyth.onnythserver.friendship.adapter.in.rest.dto;

import com.onnyth.onnythserver.friendship.domain.model.FriendRequest;
import com.onnyth.onnythserver.user.domain.model.User;
import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Builder
public record FriendRequestResponse(
        UUID requestId,
        UUID senderId,
        String senderUsername,
        String senderFullName,
        String senderProfilePic,
        String senderRankTier,
        UUID receiverId,
        String receiverUsername,
        String status,
        Instant createdAt) {

    public static FriendRequestResponse fromRequest(FriendRequest request, User sender, User receiver) {
        return FriendRequestResponse.builder()
                .requestId(request.getId())
                .senderId(sender.getId())
                .senderUsername(sender.getUsername())
                .senderFullName(sender.getFullName())
                .senderProfilePic(sender.getProfilePic())
                .senderRankTier(sender.getRankTier() != null ? sender.getRankTier().getDisplayName() : null)
                .receiverId(receiver.getId())
                .receiverUsername(receiver.getUsername())
                .status(request.getStatus().name())
                .createdAt(request.getCreatedAt())
                .build();
    }
}
