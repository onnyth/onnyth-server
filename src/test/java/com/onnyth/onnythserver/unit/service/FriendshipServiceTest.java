package com.onnyth.onnythserver.unit.service;

import com.onnyth.onnythserver.dto.FriendProfileResponse;
import com.onnyth.onnythserver.dto.FriendRequestResponse;
import com.onnyth.onnythserver.dto.FriendResponse;
import com.onnyth.onnythserver.dto.RankProgressResponse;
import com.onnyth.onnythserver.exceptions.*;
import com.onnyth.onnythserver.models.*;
import com.onnyth.onnythserver.repository.FriendRequestRepository;
import com.onnyth.onnythserver.repository.FriendshipRepository;
import com.onnyth.onnythserver.repository.UserRepository;
import com.onnyth.onnythserver.service.AchievementUnlockService;
import com.onnyth.onnythserver.service.FriendshipService;
import com.onnyth.onnythserver.service.LifeStatService;
import com.onnyth.onnythserver.service.RankService;
import com.onnyth.onnythserver.support.TestDataFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FriendshipService")
class FriendshipServiceTest {

    @Mock
    private FriendRequestRepository friendRequestRepository;
    @Mock
    private FriendshipRepository friendshipRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private LifeStatService lifeStatService;
    @Mock
    private RankService rankService;
    @Mock
    private AchievementUnlockService achievementUnlockService;

    @InjectMocks
    private FriendshipService friendshipService;

    private User buildUser(UUID id, String username) {
        return TestDataFactory.aUser()
                .id(id)
                .username(username)
                .fullName(username + " Name")
                .rankTier(RankTier.SILVER)
                .build();
    }

    @Nested
    @DisplayName("sendFriendRequest")
    class SendFriendRequest {

        @Test
        @DisplayName("sends request successfully")
        void sendsRequest() {
            UUID senderId = UUID.randomUUID();
            UUID receiverId = UUID.randomUUID();
            User sender = buildUser(senderId, "alice");
            User receiver = buildUser(receiverId, "bob");

            when(userRepository.findById(senderId)).thenReturn(Optional.of(sender));
            when(userRepository.findById(receiverId)).thenReturn(Optional.of(receiver));
            when(friendshipRepository.existsByUserIdAndFriendId(senderId, receiverId)).thenReturn(false);
            when(friendRequestRepository.existsBySenderIdAndReceiverIdAndStatus(any(), any(), any()))
                    .thenReturn(false);
            when(friendRequestRepository.save(any())).thenAnswer(i -> {
                FriendRequest r = i.getArgument(0);
                r.setId(UUID.randomUUID());
                return r;
            });

            FriendRequestResponse response = friendshipService.sendFriendRequest(senderId, receiverId);

            assertThat(response.senderUsername()).isEqualTo("alice");
            assertThat(response.status()).isEqualTo("PENDING");
            verify(friendRequestRepository).save(any(FriendRequest.class));
        }

        @Test
        @DisplayName("throws when sending to self")
        void throwsOnSelf() {
            UUID userId = UUID.randomUUID();
            assertThatThrownBy(() -> friendshipService.sendFriendRequest(userId, userId))
                    .isInstanceOf(DuplicateFriendRequestException.class);
        }

        @Test
        @DisplayName("throws when already friends")
        void throwsWhenAlreadyFriends() {
            UUID senderId = UUID.randomUUID();
            UUID receiverId = UUID.randomUUID();
            when(userRepository.findById(senderId)).thenReturn(Optional.of(buildUser(senderId, "alice")));
            when(userRepository.findById(receiverId)).thenReturn(Optional.of(buildUser(receiverId, "bob")));
            when(friendshipRepository.existsByUserIdAndFriendId(senderId, receiverId)).thenReturn(true);

            assertThatThrownBy(() -> friendshipService.sendFriendRequest(senderId, receiverId))
                    .isInstanceOf(AlreadyFriendsException.class);
        }
    }

    @Nested
    @DisplayName("acceptFriendRequest")
    class AcceptFriendRequest {

        @Test
        @DisplayName("accepts and creates bidirectional friendships")
        void acceptsRequest() {
            UUID requestId = UUID.randomUUID();
            UUID senderId = UUID.randomUUID();
            UUID receiverId = UUID.randomUUID();
            FriendRequest request = FriendRequest.builder()
                    .id(requestId).senderId(senderId).receiverId(receiverId)
                    .status(FriendRequestStatus.PENDING).build();

            when(friendRequestRepository.findById(requestId)).thenReturn(Optional.of(request));
            when(friendRequestRepository.save(any())).thenAnswer(i -> i.getArgument(0));
            when(friendshipRepository.save(any())).thenAnswer(i -> i.getArgument(0));
            when(userRepository.findById(senderId)).thenReturn(Optional.of(buildUser(senderId, "alice")));
            when(userRepository.findById(receiverId)).thenReturn(Optional.of(buildUser(receiverId, "bob")));

            FriendRequestResponse response = friendshipService.acceptFriendRequest(requestId, receiverId);

            assertThat(response.status()).isEqualTo("ACCEPTED");
            verify(friendshipRepository, times(2)).save(any(Friendship.class));
        }

        @Test
        @DisplayName("throws when non-receiver tries to accept")
        void throwsWhenNotReceiver() {
            UUID requestId = UUID.randomUUID();
            UUID senderId = UUID.randomUUID();
            UUID receiverId = UUID.randomUUID();
            FriendRequest request = FriendRequest.builder()
                    .id(requestId).senderId(senderId).receiverId(receiverId)
                    .status(FriendRequestStatus.PENDING).build();

            when(friendRequestRepository.findById(requestId)).thenReturn(Optional.of(request));

            assertThatThrownBy(() -> friendshipService.acceptFriendRequest(requestId, senderId))
                    .isInstanceOf(UnauthorizedFriendRequestActionException.class);
        }
    }

    @Nested
    @DisplayName("rejectFriendRequest")
    class RejectFriendRequest {

        @Test
        @DisplayName("rejects request")
        void rejectsRequest() {
            UUID requestId = UUID.randomUUID();
            UUID senderId = UUID.randomUUID();
            UUID receiverId = UUID.randomUUID();
            FriendRequest request = FriendRequest.builder()
                    .id(requestId).senderId(senderId).receiverId(receiverId)
                    .status(FriendRequestStatus.PENDING).build();

            when(friendRequestRepository.findById(requestId)).thenReturn(Optional.of(request));
            when(friendRequestRepository.save(any())).thenAnswer(i -> i.getArgument(0));
            when(userRepository.findById(senderId)).thenReturn(Optional.of(buildUser(senderId, "alice")));
            when(userRepository.findById(receiverId)).thenReturn(Optional.of(buildUser(receiverId, "bob")));

            FriendRequestResponse response = friendshipService.rejectFriendRequest(requestId, receiverId);
            assertThat(response.status()).isEqualTo("REJECTED");
        }
    }

    @Nested
    @DisplayName("getFriends")
    class GetFriends {

        @Test
        @DisplayName("returns paginated friends list")
        void returnsFriendsList() {
            UUID userId = UUID.randomUUID();
            UUID friendId = UUID.randomUUID();
            Friendship f = Friendship.builder()
                    .userId(userId).friendId(friendId).createdAt(Instant.now()).build();
            User friend = buildUser(friendId, "charlie");

            when(friendshipRepository.findAllByUserId(eq(userId), any()))
                    .thenReturn(new PageImpl<>(List.of(f)));
            when(userRepository.findById(friendId)).thenReturn(Optional.of(friend));

            Page<FriendResponse> page = friendshipService.getFriends(userId, PageRequest.of(0, 20));
            assertThat(page.getContent()).hasSize(1);
            assertThat(page.getContent().get(0).username()).isEqualTo("charlie");
        }
    }

    @Nested
    @DisplayName("removeFriend")
    class RemoveFriend {

        @Test
        @DisplayName("removes bidirectional friendship")
        void removesFriend() {
            UUID userId = UUID.randomUUID();
            UUID friendId = UUID.randomUUID();
            when(friendshipRepository.existsByUserIdAndFriendId(userId, friendId)).thenReturn(true);

            friendshipService.removeFriend(userId, friendId);

            verify(friendshipRepository).deleteByUserIdAndFriendId(userId, friendId);
            verify(friendshipRepository).deleteByUserIdAndFriendId(friendId, userId);
        }

        @Test
        @DisplayName("throws when not friends")
        void throwsWhenNotFriends() {
            UUID userId = UUID.randomUUID();
            UUID friendId = UUID.randomUUID();
            when(friendshipRepository.existsByUserIdAndFriendId(userId, friendId)).thenReturn(false);

            assertThatThrownBy(() -> friendshipService.removeFriend(userId, friendId))
                    .isInstanceOf(NotFriendsException.class);
        }
    }

    @Nested
    @DisplayName("getFriendProfile")
    class GetFriendProfile {

        @Test
        @DisplayName("returns friend profile with comparison")
        void returnsFriendProfile() {
            UUID userId = UUID.randomUUID();
            UUID friendId = UUID.randomUUID();
            User me = buildUser(userId, "alice");
            me.setTotalScore(500L);
            User friend = buildUser(friendId, "bob");
            friend.setTotalScore(300L);

            when(friendshipRepository.existsByUserIdAndFriendId(userId, friendId)).thenReturn(true);
            when(userRepository.findById(friendId)).thenReturn(Optional.of(friend));
            when(userRepository.findById(userId)).thenReturn(Optional.of(me));
            when(lifeStatService.getUserStats(friendId)).thenReturn(List.of());
            when(lifeStatService.getUserStats(userId)).thenReturn(List.of());
            when(rankService.getRankProgress(friendId)).thenReturn(
                    RankProgressResponse.builder()
                            .currentTier("Silver").currentBadge("🥈")
                            .currentScore(300).build());

            FriendProfileResponse response = friendshipService.getFriendProfile(userId, friendId);

            assertThat(response.username()).isEqualTo("bob");
            assertThat(response.comparison().scoreDifference()).isEqualTo(200);
        }
    }
}
