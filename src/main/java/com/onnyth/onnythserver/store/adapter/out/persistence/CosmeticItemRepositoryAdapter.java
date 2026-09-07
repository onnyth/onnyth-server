package com.onnyth.onnythserver.store.adapter.out.persistence;

import com.onnyth.onnythserver.store.application.port.CosmeticItemRepository;
import com.onnyth.onnythserver.store.domain.model.CosmeticCategory;
import com.onnyth.onnythserver.store.domain.model.CosmeticItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class CosmeticItemRepositoryAdapter implements CosmeticItemRepository {

    private final CosmeticItemJpaRepository cosmeticItemJpaRepository;

    @Override
    public CosmeticItem save(CosmeticItem cosmeticItem) {
        CosmeticItemEntity saved = cosmeticItemJpaRepository.save(CosmeticItemPersistenceMapper.toEntity(cosmeticItem));
        return CosmeticItemPersistenceMapper.toDomain(saved);
    }

    @Override
    public Optional<CosmeticItem> findById(UUID id) {
        return cosmeticItemJpaRepository.findById(id).map(CosmeticItemPersistenceMapper::toDomain);
    }

    @Override
    public List<CosmeticItem> findAllByIsActiveTrue() {
        return cosmeticItemJpaRepository.findAllByIsActiveTrue().stream()
                .map(CosmeticItemPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public List<CosmeticItem> findAllByIsActiveTrueAndCategory(CosmeticCategory category) {
        return cosmeticItemJpaRepository.findAllByIsActiveTrueAndCategory(category).stream()
                .map(CosmeticItemPersistenceMapper::toDomain)
                .toList();
    }
}
