package com.onnyth.onnythserver.service;

import com.onnyth.onnythserver.events.XpAwardedEvent;
import com.onnyth.onnythserver.exceptions.UserNotFoundException;
import com.onnyth.onnythserver.models.User;
import com.onnyth.onnythserver.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class XpService {

    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Award XP to a user and publish an XpAwardedEvent.
     *
     * @return the user's new total XP
     */
    @Transactional
    public long awardXp(UUID userId, int amount) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId.toString()));

        long newTotalXp = user.getXp() + amount;
        user.setXp(newTotalXp);
        userRepository.save(user);

        log.info("XP awarded: userId={}, amount={}, newTotal={}", userId, amount, newTotalXp);

        eventPublisher.publishEvent(new XpAwardedEvent(userId, amount, newTotalXp));

        return newTotalXp;
    }
}
