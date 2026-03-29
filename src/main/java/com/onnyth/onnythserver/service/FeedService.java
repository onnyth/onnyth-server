package com.onnyth.onnythserver.service;

import com.onnyth.onnythserver.dto.FeedEventResponse;
import com.onnyth.onnythserver.models.FeedEvent;
import com.onnyth.onnythserver.models.FeedEventType;
import com.onnyth.onnythserver.models.User;
import com.onnyth.onnythserver.repository.FeedEventRepository;
import com.onnyth.onnythserver.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FeedService {

    private final FeedEventRepository feedEventRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    /**
     * Create a feed event.
     */
    @Transactional
    public void createFeedEvent(UUID userId, FeedEventType type, Object eventData) {
        String jsonData;
        try {
            jsonData = objectMapper.writeValueAsString(eventData);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize feed event data", e);
            jsonData = "{}";
        }

        FeedEvent event = FeedEvent.builder()
                .userId(userId)
                .eventType(type)
                .eventData(jsonData)
                .createdAt(Instant.now())
                .build();
        feedEventRepository.save(event);

        log.debug("Feed event created: userId={}, type={}", userId, type);
    }

    /**
     * Get the friend feed for a user (paginated).
     */
    @Transactional(readOnly = true)
    public Page<FeedEventResponse> getFriendFeed(UUID userId, Pageable pageable) {
        Page<FeedEvent> events = feedEventRepository.findFriendFeed(userId, pageable);

        // Batch load user profiles
        var userIds = events.getContent().stream()
                .map(FeedEvent::getUserId)
                .distinct()
                .toList();
        Map<UUID, User> userMap = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        return events.map(event -> {
            User user = userMap.get(event.getUserId());
            return FeedEventResponse.builder()
                    .id(event.getId())
                    .userId(event.getUserId())
                    .username(user != null ? user.getUsername() : null)
                    .profilePic(user != null ? user.getProfilePic() : null)
                    .eventType(event.getEventType())
                    .eventData(event.getEventData())
                    .createdAt(event.getCreatedAt())
                    .build();
        });
    }
}
