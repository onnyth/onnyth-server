package com.onnyth.onnythserver.repository;

import com.onnyth.onnythserver.models.ActivityType;
import com.onnyth.onnythserver.models.StatDomain;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ActivityTypeRepository extends JpaRepository<ActivityType, UUID> {

    List<ActivityType> findAllByIsActiveTrue();

    List<ActivityType> findAllByCategoryAndIsActiveTrue(StatDomain category);

    Optional<ActivityType> findByIdAndIsActiveTrue(UUID id);
}
