package com.onnyth.onnythserver.friendship.application.usecase;

import com.onnyth.onnythserver.friendship.adapter.in.rest.dto.FriendProfileResponse;
import com.onnyth.onnythserver.friendship.adapter.in.rest.dto.FriendRequestResponse;
import com.onnyth.onnythserver.friendship.adapter.in.rest.dto.FriendResponse;
import com.onnyth.onnythserver.friendship.application.exception.AlreadyFriendsException;
import com.onnyth.onnythserver.friendship.application.exception.DuplicateFriendRequestException;
import com.onnyth.onnythserver.friendship.application.exception.NotFriendsException;
import com.onnyth.onnythserver.friendship.application.exception.UnauthorizedFriendRequestActionException;
import com.onnyth.onnythserver.friendship.application.port.FriendRequestRepository;
import com.onnyth.onnythserver.friendship.application.port.FriendshipRepository;
import com.onnyth.onnythserver.friendship.domain.model.FriendRequest;
import com.onnyth.onnythserver.friendship.domain.model.FriendRequestStatus;
import com.onnyth.onnythserver.friendship.domain.model.Friendship;
import com.onnyth.onnythserver.lifestats.application.port.UserCharismaRepository;
import com.onnyth.onnythserver.lifestats.application.port.UserOccupationRepository;
import com.onnyth.onnythserver.lifestats.application.port.UserPhysiqueRepository;
import com.onnyth.onnythserver.lifestats.application.port.UserWealthRepository;
import com.onnyth.onnythserver.lifestats.application.port.UserWisdomRepository;
import com.onnyth.onnythserver.ranking.adapter.in.rest.dto.RankProgressResponse;
import com.onnyth.onnythserver.ranking.application.usecase.RankUseCaseService;
import com.onnyth.onnythserver.ranking.domain.model.RankTier;
import com.onnyth.onnythserver.achievement.application.usecase.AchievementUnlockUseCaseService;
import com.onnyth.onnythserver.support.TestDataFactory;
import com.onnyth.onnythserver.user.application.port.UserRepository;
import com.onnyth.onnythserver.user.domain.model.User;
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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("FriendshipUseCaseService")
class FriendshipUseCaseServiceTest {

    @Mock
    private FriendRequestRepository friendRequestRepository;
    @Mock
    private FriendshipRepository friendshipRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private RankUseCaseService rankService;
    @Mock
    private AchievementUnlockUseCaseService achievementUnlockService;
    @Mock
    private UserOccupationRepository occupationRepository;
    @Mock
    private UserWealthRepository wealthRepository;
    @Mock
    private UserPhysiqueRepository physiqueRepository;
    @Mock
    private UserWisdomRepository wisdomRepository;
    @Mock
    private UserCharismaRepository charismaRepository;

    @InjectMocks
    private FriendshipUseCaseService friendshipService;

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
            when(friendRequestRepository.save(any())).thenAnswer(invocation -> {
                FriendRequest request = invocation.getArgument(0);
                request.setId(UUID.randomUUID());
                return request;
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
                    .id(requestId)
                    .senderId(senderId)
                    .receiverId(receiverId)
                    .status(FriendRequestStatus.PENDING)
                    .build();

            when(friendRequestRepository.findById(requestId)).thenReturn(Optional.of(request));
            when(friendRequestRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
            when(friendshipRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
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
                    .id(requestId)
                    .senderId(senderId)
                    .receiverId(receiverId)
                    .status(FriendRequestStatus.PENDING)
                    .build();

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
                    .id(requestId)
                    .senderId(senderId)
                    .receiverId(receiverId)
                    .status(FriendRequestStatus.PENDING)
                    .build();

            when(friendRequestRepository.findById(requestId)).thenReturn(Optional.of(request));
            when(friendRequestRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
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
            Friendship friendship = Friendship.builder()
                    .userId(userId)
                    .friendId(friendId)
                    .createdAt(Instant.now())
                    .build();
            User friend = buildUser(friendId, "charlie");

            when(friendshipRepository.findAllByUserId(eq(userId), any()))
                    .thenReturn(new PageImpl<>(List.of(friendship)));
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
            when(rankService.getRankProgress(friendId)).thenReturn(
                    RankProgressResponse.builder()
                            .currentTier("Silver")
                            .currentBadge("🥈")
                            .currentScore(300)
                            .build());

            when(occupationRepository.findByUserIdAndIsCurrentTrue(any())).thenReturn(Optional.empty());
            when(wealthRepository.findByUserId(any())).thenReturn(Optional.empty());
            when(physiqueRepository.findByUserId(any())).thenReturn(Optional.empty());
            when(wisdomRepository.findByUserId(any())).thenReturn(Optional.empty());
            when(charismaRepository.findByUserId(any())).thenReturn(Optional.empty());

            FriendProfileResponse response = friendshipService.getFriendProfile(userId, friendId);

            assertThat(response.username()).isEqualTo("bob");
            assertThat(response.comparison().scoreDifference()).isEqualTo(200);
        }
    }
}
