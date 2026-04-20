package com.onnyth.onnythserver.repository;

import com.onnyth.onnythserver.models.SportMedal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SportMedalRepository extends JpaRepository<SportMedal, UUID> {

    List<SportMedal> findAllByUserId(UUID userId);

    long countByUserId(UUID userId);
}
