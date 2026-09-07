package com.onnyth.onnythserver.friendship.application.usecase;

import com.onnyth.onnythserver.dto.StatComparisonResponse;
import com.onnyth.onnythserver.friendship.adapter.in.rest.dto.FriendProfileResponse;
import com.onnyth.onnythserver.friendship.adapter.in.rest.dto.FriendRequestResponse;
import com.onnyth.onnythserver.friendship.adapter.in.rest.dto.FriendResponse;
import com.onnyth.onnythserver.friendship.application.exception.AlreadyFriendsException;
import com.onnyth.onnythserver.friendship.application.exception.DuplicateFriendRequestException;
import com.onnyth.onnythserver.friendship.application.exception.FriendRequestNotFoundException;
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
import com.onnyth.onnythserver.models.StatDomain;
import com.onnyth.onnythserver.ranking.adapter.in.rest.dto.RankProgressResponse;
import com.onnyth.onnythserver.ranking.application.usecase.RankUseCaseService;
import com.onnyth.onnythserver.service.AchievementUnlockService;
import com.onnyth.onnythserver.user.application.exception.UserNotFoundException;
import com.onnyth.onnythserver.user.application.port.UserRepository;
import com.onnyth.onnythserver.user.domain.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FriendshipUseCaseService {

    private final FriendRequestRepository friendRequestRepository;
    private final FriendshipRepository friendshipRepository;
    private final UserRepository userRepository;
    private final RankUseCaseService rankService;
    private final AchievementUnlockService achievementUnlockService;
    private final UserOccupationRepository occupationRepository;
    private final UserWealthRepository wealthRepository;
    private final UserPhysiqueRepository physiqueRepository;
    private final UserWisdomRepository wisdomRepository;
    private final UserCharismaRepository charismaRepository;

    @Transactional
    public FriendRequestResponse sendFriendRequest(UUID senderId, UUID receiverId) {
        if (senderId.equals(receiverId)) {
            throw new DuplicateFriendRequestException("Cannot send a friend request to yourself");
        }

        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new UserNotFoundException(senderId.toString()));
        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new UserNotFoundException(receiverId.toString()));

        if (friendshipRepository.existsByUserIdAndFriendId(senderId, receiverId)) {
            throw new AlreadyFriendsException("Already friends with user: " + receiverId);
        }

        boolean pendingExists = friendRequestRepository.existsBySenderIdAndReceiverIdAndStatus(
                senderId, receiverId, FriendRequestStatus.PENDING) ||
                friendRequestRepository.existsBySenderIdAndReceiverIdAndStatus(
                        receiverId, senderId, FriendRequestStatus.PENDING);
        if (pendingExists) {
            throw new DuplicateFriendRequestException("A pending friend request already exists");
        }

        FriendRequest request = FriendRequest.builder()
                .senderId(senderId)
                .receiverId(receiverId)
                .status(FriendRequestStatus.PENDING)
                .createdAt(Instant.now())
                .build();
        friendRequestRepository.save(request);

        log.info("Friend request sent: {} → {}", senderId, receiverId);
        return FriendRequestResponse.fromRequest(request, sender, receiver);
    }

    @Transactional
    public FriendRequestResponse acceptFriendRequest(UUID requestId, UUID currentUserId) {
        FriendRequest request = friendRequestRepository.findById(requestId)
                .orElseThrow(() -> new FriendRequestNotFoundException(requestId.toString()));

        if (!request.getReceiverId().equals(currentUserId)) {
            throw new UnauthorizedFriendRequestActionException("Only the receiver can accept a friend request");
        }

        if (request.getStatus() != FriendRequestStatus.PENDING) {
            throw new DuplicateFriendRequestException("Friend request is no longer pending");
        }

        request.setStatus(FriendRequestStatus.ACCEPTED);
        request.setUpdatedAt(Instant.now());
        friendRequestRepository.save(request);

        Friendship f1 = Friendship.builder()
                .userId(request.getSenderId())
                .friendId(request.getReceiverId())
                .createdAt(Instant.now())
                .build();
        Friendship f2 = Friendship.builder()
                .userId(request.getReceiverId())
                .friendId(request.getSenderId())
                .createdAt(Instant.now())
                .build();
        friendshipRepository.save(f1);
        friendshipRepository.save(f2);

        User sender = userRepository.findById(request.getSenderId())
                .orElseThrow(() -> new UserNotFoundException(request.getSenderId().toString()));
        User receiver = userRepository.findById(request.getReceiverId())
                .orElseThrow(() -> new UserNotFoundException(request.getReceiverId().toString()));

        log.info("Friend request accepted: {} ↔ {}", request.getSenderId(), request.getReceiverId());

        achievementUnlockService.checkAndUnlockAchievements(request.getSenderId());
        achievementUnlockService.checkAndUnlockAchievements(request.getReceiverId());

        return FriendRequestResponse.fromRequest(request, sender, receiver);
    }

    @Transactional
    public FriendRequestResponse rejectFriendRequest(UUID requestId, UUID currentUserId) {
        FriendRequest request = friendRequestRepository.findById(requestId)
                .orElseThrow(() -> new FriendRequestNotFoundException(requestId.toString()));

        if (!request.getReceiverId().equals(currentUserId)) {
            throw new UnauthorizedFriendRequestActionException("Only the receiver can reject a friend request");
        }

        if (request.getStatus() != FriendRequestStatus.PENDING) {
            throw new DuplicateFriendRequestException("Friend request is no longer pending");
        }

        request.setStatus(FriendRequestStatus.REJECTED);
        request.setUpdatedAt(Instant.now());
        friendRequestRepository.save(request);

        User sender = userRepository.findById(request.getSenderId())
                .orElseThrow(() -> new UserNotFoundException(request.getSenderId().toString()));
        User receiver = userRepository.findById(request.getReceiverId())
                .orElseThrow(() -> new UserNotFoundException(request.getReceiverId().toString()));

        log.info("Friend request rejected: {} → {}", request.getSenderId(), request.getReceiverId());
        return FriendRequestResponse.fromRequest(request, sender, receiver);
    }

    @Transactional(readOnly = true)
    public List<FriendRequestResponse> getReceivedRequests(UUID userId) {
        List<FriendRequest> requests = friendRequestRepository.findAllByReceiverIdAndStatus(
                userId, FriendRequestStatus.PENDING);
        return mapRequests(requests);
    }

    @Transactional(readOnly = true)
    public List<FriendRequestResponse> getSentRequests(UUID userId) {
        List<FriendRequest> requests = friendRequestRepository.findAllBySenderIdAndStatus(
                userId, FriendRequestStatus.PENDING);
        return mapRequests(requests);
    }

    @Transactional(readOnly = true)
    public long getPendingRequestCount(UUID userId) {
        return friendRequestRepository.countByReceiverIdAndStatus(userId, FriendRequestStatus.PENDING);
    }

    @Transactional(readOnly = true)
    public Page<FriendResponse> getFriends(UUID userId, Pageable pageable) {
        Page<Friendship> friendships = friendshipRepository.findAllByUserId(userId, pageable);
        return friendships.map(friendship -> {
            User friend = userRepository.findById(friendship.getFriendId())
                    .orElseThrow(() -> new UserNotFoundException(friendship.getFriendId().toString()));
            return buildFriendResponse(friend, friendship.getCreatedAt());
        });
    }

    @Transactional(readOnly = true)
    public List<FriendResponse> searchFriends(UUID userId, String query) {
        List<Friendship> friendships = friendshipRepository.searchFriends(userId, query);
        return friendships.stream()
                .map(friendship -> {
                    User friend = userRepository.findById(friendship.getFriendId())
                            .orElseThrow(() -> new UserNotFoundException(friendship.getFriendId().toString()));
                    return buildFriendResponse(friend, friendship.getCreatedAt());
                })
                .toList();
    }

    @Transactional
    public void removeFriend(UUID userId, UUID friendId) {
        if (!friendshipRepository.existsByUserIdAndFriendId(userId, friendId)) {
            throw new NotFriendsException("Not friends with user: " + friendId);
        }

        friendshipRepository.deleteByUserIdAndFriendId(userId, friendId);
        friendshipRepository.deleteByUserIdAndFriendId(friendId, userId);

        log.info("Friendship removed: {} ↔ {}", userId, friendId);
    }

    @Transactional(readOnly = true)
    public FriendProfileResponse getFriendProfile(UUID userId, UUID friendId) {
        if (!friendshipRepository.existsByUserIdAndFriendId(userId, friendId)) {
            throw new NotFriendsException("Not friends with user: " + friendId);
        }

        User friend = userRepository.findById(friendId)
                .orElseThrow(() -> new UserNotFoundException(friendId.toString()));

        RankProgressResponse rankProgress = rankService.getRankProgress(friendId);
        StatComparisonResponse comparison = computeComparison(userId, friendId);

        return FriendProfileResponse.builder()
                .userId(friend.getId())
                .username(friend.getUsername())
                .fullName(friend.getFullName())
                .profilePic(friend.getProfilePic())
                .rankTier(friend.getRankTier() != null ? friend.getRankTier().getDisplayName() : null)
                .totalScore(friend.getTotalScore())
                .rankProgress(rankProgress)
                .comparison(comparison)
                .build();
    }

    private List<FriendRequestResponse> mapRequests(List<FriendRequest> requests) {
        if (requests.isEmpty()) {
            return List.of();
        }

        List<UUID> userIds = new ArrayList<>();
        requests.forEach(request -> {
            userIds.add(request.getSenderId());
            userIds.add(request.getReceiverId());
        });
        Map<UUID, User> usersById = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        return requests.stream()
                .map(request -> FriendRequestResponse.fromRequest(
                        request,
                        usersById.get(request.getSenderId()),
                        usersById.get(request.getReceiverId())))
                .toList();
    }

    private FriendResponse buildFriendResponse(User friend, Instant friendSince) {
        return FriendResponse.builder()
                .userId(friend.getId())
                .username(friend.getUsername())
                .fullName(friend.getFullName())
                .profilePic(friend.getProfilePic())
                .rankTier(friend.getRankTier() != null ? friend.getRankTier().getDisplayName() : null)
                .totalScore(friend.getTotalScore())
                .friendSince(friendSince)
                .build();
    }

    private StatComparisonResponse computeComparison(UUID userId, UUID friendId) {
        User me = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId.toString()));
        User friend = userRepository.findById(friendId)
                .orElseThrow(() -> new UserNotFoundException(friendId.toString()));

        long scoreDiff = me.getTotalScore() - friend.getTotalScore();

        List<String> higherIn = new ArrayList<>();
        List<String> lowerIn = new ArrayList<>();

        for (StatDomain domain : StatDomain.values()) {
            int myScore = getDomainScore(userId, domain);
            int friendScore = getDomainScore(friendId, domain);
            if (myScore > friendScore) {
                higherIn.add(domain.getDisplayName());
            } else if (myScore < friendScore) {
                lowerIn.add(domain.getDisplayName());
            }
        }

        return StatComparisonResponse.builder()
                .scoreDifference(scoreDiff)
                .higherIn(higherIn)
                .lowerIn(lowerIn)
                .build();
    }

    private int getDomainScore(UUID userId, StatDomain domain) {
        return switch (domain) {
            case OCCUPATION -> occupationRepository.findByUserIdAndIsCurrentTrue(userId)
                    .map(occupation -> occupation.getScore()).orElse(0);
            case WEALTH -> wealthRepository.findByUserId(userId)
                    .map(wealth -> wealth.getScore()).orElse(0);
            case PHYSIQUE -> physiqueRepository.findByUserId(userId)
                    .map(physique -> physique.getScore()).orElse(0);
            case WISDOM -> wisdomRepository.findByUserId(userId)
                    .map(wisdom -> wisdom.getScore()).orElse(0);
            case CHARISMA -> charismaRepository.findByUserId(userId)
                    .map(charisma -> charisma.getScore()).orElse(0);
        };
    }
}
