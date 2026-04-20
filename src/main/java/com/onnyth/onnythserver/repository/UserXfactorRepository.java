package com.onnyth.onnythserver.repository;

import com.onnyth.onnythserver.models.UserXfactor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserXfactorRepository extends JpaRepository<UserXfactor, UUID> {

    List<UserXfactor> findAllByUserId(UUID userId);

    long countByUserId(UUID userId);
}
