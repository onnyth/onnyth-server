package com.onnyth.onnythserver.store.adapter.out.persistence;

import com.onnyth.onnythserver.store.domain.model.CosmeticCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CosmeticItemJpaRepository extends JpaRepository<CosmeticItemEntity, UUID> {

    List<CosmeticItemEntity> findAllByIsActiveTrue();

    List<CosmeticItemEntity> findAllByIsActiveTrueAndCategory(CosmeticCategory category);
}
