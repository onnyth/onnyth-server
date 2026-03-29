package com.onnyth.onnythserver.service;

import com.onnyth.onnythserver.dto.ActivityTypeResponse;
import com.onnyth.onnythserver.exceptions.ActivityTypeNotFoundException;
import com.onnyth.onnythserver.models.ActivityType;
import com.onnyth.onnythserver.models.StatCategory;
import com.onnyth.onnythserver.repository.ActivityTypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ActivityTypeService {

    private final ActivityTypeRepository activityTypeRepository;

    /**
     * Get all active activity types, optionally filtered by category.
     */
    @Transactional(readOnly = true)
    public List<ActivityTypeResponse> getActivityTypes(StatCategory category) {
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
