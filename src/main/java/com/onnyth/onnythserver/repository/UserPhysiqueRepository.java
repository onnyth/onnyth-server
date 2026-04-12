package com.onnyth.onnythserver.repository;

import com.onnyth.onnythserver.models.UserPhysique;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserPhysiqueRepository extends JpaRepository<UserPhysique, UUID> {

    Optional<UserPhysique> findByUserId(UUID userId);

    boolean existsByUserId(UUID userId);
}
