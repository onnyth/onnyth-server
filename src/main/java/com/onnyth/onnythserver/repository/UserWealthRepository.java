package com.onnyth.onnythserver.repository;

import com.onnyth.onnythserver.models.UserWealth;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserWealthRepository extends JpaRepository<UserWealth, UUID> {

    Optional<UserWealth> findByUserId(UUID userId);

    boolean existsByUserId(UUID userId);
}
