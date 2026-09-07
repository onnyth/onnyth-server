package com.onnyth.onnythserver.store.application.usecase;

import com.onnyth.onnythserver.store.adapter.in.rest.dto.CosmeticItemResponse;
import com.onnyth.onnythserver.store.application.exception.CosmeticAlreadyOwnedException;
import com.onnyth.onnythserver.store.application.exception.CosmeticNotFoundException;
import com.onnyth.onnythserver.store.application.exception.InsufficientCoinsException;
import com.onnyth.onnythserver.store.application.port.CosmeticItemRepository;
import com.onnyth.onnythserver.store.application.port.UserCosmeticRepository;
import com.onnyth.onnythserver.store.domain.model.CosmeticCategory;
import com.onnyth.onnythserver.store.domain.model.CosmeticItem;
import com.onnyth.onnythserver.store.domain.model.UserCosmetic;
import com.onnyth.onnythserver.user.application.exception.UserNotFoundException;
import com.onnyth.onnythserver.user.application.port.UserRepository;
import com.onnyth.onnythserver.user.domain.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StoreUseCaseService {

    private final CosmeticItemRepository cosmeticItemRepository;
    private final UserCosmeticRepository userCosmeticRepository;
    private final UserRepository userRepository;

    /**
     * List store items with ownership info for the current user.
     */
    @Transactional(readOnly = true)
    public List<CosmeticItemResponse> getStoreItems(UUID userId, CosmeticCategory category) {
        List<CosmeticItem> items = category != null
                ? cosmeticItemRepository.findAllByIsActiveTrueAndCategory(category)
                : cosmeticItemRepository.findAllByIsActiveTrue();

        Set<UUID> ownedIds = userCosmeticRepository.findAllByUserId(userId).stream()
                .map(UserCosmetic::getCosmeticItemId)
                .collect(Collectors.toSet());

        Set<UUID> equippedIds = userCosmeticRepository.findAllByUserId(userId).stream()
                .filter(UserCosmetic::getIsEquipped)
                .map(UserCosmetic::getCosmeticItemId)
                .collect(Collectors.toSet());

        return items.stream()
                .map(item -> CosmeticItemResponse.fromEntity(
                        item,
                        ownedIds.contains(item.getId()),
                        equippedIds.contains(item.getId())))
                .toList();
    }

    /**
     * Purchase a cosmetic item — deducts Onnyth Coins from user.
     */
    @Transactional
    public CosmeticItemResponse purchaseItem(UUID userId, UUID itemId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId.toString()));

        CosmeticItem item = cosmeticItemRepository.findById(itemId)
                .orElseThrow(() -> new CosmeticNotFoundException(itemId.toString()));

        if (userCosmeticRepository.existsByUserIdAndCosmeticItemId(userId, itemId)) {
            throw new CosmeticAlreadyOwnedException(itemId.toString());
        }

        if (user.getOnnythCoins() < item.getPrice()) {
            throw new InsufficientCoinsException(user.getOnnythCoins(), item.getPrice());
        }

        user.setOnnythCoins(user.getOnnythCoins() - item.getPrice());
        userRepository.save(user);

        UserCosmetic userCosmetic = UserCosmetic.builder()
                .userId(userId)
                .cosmeticItemId(itemId)
                .purchasedAt(Instant.now())
                .isEquipped(false)
                .build();
        userCosmeticRepository.save(userCosmetic);

        log.info("Cosmetic purchased: userId={}, itemId={}, price={}", userId, itemId, item.getPrice());

        return CosmeticItemResponse.fromEntity(item, true, false);
    }

    /**
     * Equip a cosmetic item.
     * For FRAME and BACKGROUND categories, also updates the active cosmetic reference
     * on the User entity so the profile card reflects the change immediately.
     */
    @Transactional
    public List<CosmeticItemResponse> equipItem(UUID userId, UUID itemId) {
        UserCosmetic userCosmetic = userCosmeticRepository.findByUserIdAndCosmeticItemId(userId, itemId)
                .orElseThrow(() -> new CosmeticNotFoundException(itemId.toString()));

        userCosmetic.setIsEquipped(true);
        userCosmeticRepository.save(userCosmetic);

        CosmeticItem item = cosmeticItemRepository.findById(itemId).orElse(null);
        if (item != null) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new UserNotFoundException(userId.toString()));
            if (item.getCategory() == CosmeticCategory.FRAME) {
                user.setActiveFrameCosmetic(item);
            } else if (item.getCategory() == CosmeticCategory.BACKGROUND) {
                user.setActiveBackgroundCosmetic(item);
                user.setActiveBackgroundColor(null);
            }
            userRepository.save(user);
        }

        log.info("Cosmetic equipped: userId={}, itemId={}", userId, itemId);
        return getInventory(userId);
    }

    /**
     * Get user's owned cosmetics.
     */
    @Transactional(readOnly = true)
    public List<CosmeticItemResponse> getInventory(UUID userId) {
        List<UserCosmetic> owned = userCosmeticRepository.findAllByUserId(userId);

        return owned.stream()
                .map(uc -> {
                    CosmeticItem item = cosmeticItemRepository.findById(uc.getCosmeticItemId()).orElse(null);
                    if (item == null) return null;
                    return CosmeticItemResponse.fromEntity(item, true, uc.getIsEquipped());
                })
                .filter(java.util.Objects::nonNull)
                .toList();
    }
}
