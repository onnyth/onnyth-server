package com.onnyth.onnythserver.store.application.port;

import com.onnyth.onnythserver.store.domain.model.CosmeticCategory;
import com.onnyth.onnythserver.store.domain.model.CosmeticItem;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CosmeticItemRepository {

    CosmeticItem save(CosmeticItem cosmeticItem);

    Optional<CosmeticItem> findById(UUID id);

    List<CosmeticItem> findAllByIsActiveTrue();

    List<CosmeticItem> findAllByIsActiveTrueAndCategory(CosmeticCategory category);
}
