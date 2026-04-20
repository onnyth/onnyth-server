package com.onnyth.onnythserver.repository;

import com.onnyth.onnythserver.models.CosmeticCategory;
import com.onnyth.onnythserver.models.CosmeticItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CosmeticItemRepository extends JpaRepository<CosmeticItem, UUID> {

    List<CosmeticItem> findAllByIsActiveTrue();

    List<CosmeticItem> findAllByIsActiveTrueAndCategory(CosmeticCategory category);
}
