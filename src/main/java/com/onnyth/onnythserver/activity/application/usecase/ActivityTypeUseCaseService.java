package com.onnyth.onnythserver.activity.application.usecase;

import com.onnyth.onnythserver.activity.adapter.in.rest.dto.ActivityTypeResponse;
import com.onnyth.onnythserver.activity.application.exception.ActivityTypeNotFoundException;
import com.onnyth.onnythserver.activity.application.port.ActivityTypeRepository;
import com.onnyth.onnythserver.activity.domain.model.ActivityType;
import com.onnyth.onnythserver.shared.domain.model.StatDomain;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ActivityTypeUseCaseService {

    private final ActivityTypeRepository activityTypeRepository;

    /**
     * Get all active activity types, optionally filtered by category.
     */
    @Transactional(readOnly = true)
    public List<ActivityTypeResponse> getActivityTypes(StatDomain category) {
        List<ActivityType> types;
        if (category != null) {
            types = activityTypeRepository.findAllByCategoryAndIsActiveTrue(category);
        } else {
            types = activityTypeRepository.findAllByIsActiveTrue();
        }
        return types.stream()
                .map(ActivityTypeResponse::fromEntity)
                .toList();
    }

    /**
     * Get a single active activity type by ID.
     */
    @Transactional(readOnly = true)
    public ActivityType getActiveActivityType(UUID id) {
        return activityTypeRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new ActivityTypeNotFoundException(id.toString()));
    }
}
