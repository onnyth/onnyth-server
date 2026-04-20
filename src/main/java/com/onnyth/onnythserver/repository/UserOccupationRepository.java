package com.onnyth.onnythserver.repository;

import com.onnyth.onnythserver.models.UserOccupation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserOccupationRepository extends JpaRepository<UserOccupation, UUID> {

    Optional<UserOccupation> findByUserIdAndIsCurrentTrue(UUID userId);

    List<UserOccupation> findAllByUserId(UUID userId);

    boolean existsByUserId(UUID userId);
}
