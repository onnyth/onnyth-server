package com.onnyth.onnythserver.repository;

import com.onnyth.onnythserver.models.UserWisdom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserWisdomRepository extends JpaRepository<UserWisdom, UUID> {

    Optional<UserWisdom> findByUserId(UUID userId);

    boolean existsByUserId(UUID userId);
}
