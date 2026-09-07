package com.onnyth.onnythserver.activity.adapter.out.persistence;

import com.onnyth.onnythserver.models.StatDomain;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ActivityTypeJpaRepository extends JpaRepository<ActivityTypeEntity, UUID> {

    List<ActivityTypeEntity> findAllByIsActiveTrue();

    List<ActivityTypeEntity> findAllByCategoryAndIsActiveTrue(StatDomain category);

    Optional<ActivityTypeEntity> findByIdAndIsActiveTrue(UUID id);
}
