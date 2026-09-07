package com.onnyth.onnythserver.scoring.application.port;

import com.onnyth.onnythserver.scoring.domain.model.SportMedal;

import java.util.List;
import java.util.UUID;

public interface SportMedalRepository {

    SportMedal save(SportMedal sportMedal);

    List<SportMedal> findAllByUserId(UUID userId);

    long countByUserId(UUID userId);
}
