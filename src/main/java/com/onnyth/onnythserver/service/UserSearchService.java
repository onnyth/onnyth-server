package com.onnyth.onnythserver.service;

import com.onnyth.onnythserver.dto.UserSearchResponse;
import com.onnyth.onnythserver.models.FriendRequestStatus;
import com.onnyth.onnythserver.models.User;
import com.onnyth.onnythserver.repository.FriendRequestRepository;
import com.onnyth.onnythserver.repository.FriendshipRepository;
import com.onnyth.onnythserver.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserSearchService {

    private final UserRepository userRepository;
    private final FriendshipRepository friendshipRepository;
    private final FriendRequestRepository friendRequestRepository;

    /**
     * Search users by username or full name (case-insensitive partial match).
     * Excludes the searching user and annotates friendship/request status.
     */
    @Transactional(readOnly = true)
    public Page<UserSearchResponse> searchUsers(String query, UUID currentUserId, Pageable pageable) {
        Page<User> users = userRepository.searchByUsernameOrFullName(query, currentUserId, pageable);

        return users.map(user -> {
            boolean isFriend = friendshipRepository.existsByUserIdAndFriendId(currentUserId, user.getId());
            boolean requestPending = friendRequestRepository.existsBySenderIdAndReceiverIdAndStatus(
                    currentUserId, user.getId(), FriendRequestStatus.PENDING) ||
                    friendRequestRepository.existsBySenderIdAndReceiverIdAndStatus(
                            user.getId(), currentUserId, FriendRequestStatus.PENDING);

            return UserSearchResponse.builder()
                    .userId(user.getId())
                    .username(user.getUsername())
                    .fullName(user.getFullName())
                    .profilePic(user.getProfilePic())
                    .rankTier(user.getRankTier() != null ? user.getRankTier().getDisplayName() : null)
                    .isFriend(isFriend)
                    .requestPending(requestPending)
                    .build();
        });
    }
}
