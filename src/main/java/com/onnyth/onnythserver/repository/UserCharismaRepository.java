package com.onnyth.onnythserver.repository;

import com.onnyth.onnythserver.models.UserCharisma;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserCharismaRepository extends JpaRepository<UserCharisma, UUID> {

    Optional<UserCharisma> findByUserId(UUID userId);

    boolean existsByUserId(UUID userId);
}
