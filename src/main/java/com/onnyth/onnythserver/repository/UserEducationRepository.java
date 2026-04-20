package com.onnyth.onnythserver.repository;

import com.onnyth.onnythserver.models.UserEducation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserEducationRepository extends JpaRepository<UserEducation, UUID> {

    List<UserEducation> findAllByUserId(UUID userId);

    Optional<UserEducation> findByUserIdAndIsHighestTrue(UUID userId);
}
