package com.onnyth.onnythserver.unit.dto;

import com.onnyth.onnythserver.dto.LifeStatResponse;
import com.onnyth.onnythserver.models.LifeStat;
import com.onnyth.onnythserver.models.StatCategory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for LifeStatResponse DTO.
 */
class LifeStatResponseTest {

    @Test
    @DisplayName("fromEntity() maps all fields correctly")
    void fromEntity_mapsAllFields() {
        Instant now = Instant.now();
        LifeStat entity = LifeStat.builder()
                .id(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .category(StatCategory.CAREER)
                .value(75)
                .lastUpdated(now)
                .metadata("{\"role\": \"engineer\"}")
                .build();

        LifeStatResponse response = LifeStatResponse.fromEntity(entity);

        assertThat(response.category()).isEqualTo(StatCategory.CAREER);
        assertThat(response.displayName()).isEqualTo("Career");
        assertThat(response.value()).isEqualTo(75);
        assertThat(response.pointsContributed()).isEqualTo(75); // defaults to value
        assertThat(response.lastUpdated()).isEqualTo(now);
        assertThat(response.metadata()).isEqualTo("{\"role\": \"engineer\"}");
    }

    @Test
    @DisplayName("fromEntity() handles null metadata")
    void fromEntity_handlesNullMetadata() {
        LifeStat entity = LifeStat.builder()
                .id(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .category(StatCategory.FITNESS)
                .value(50)
                .lastUpdated(Instant.now())
                .build();

        LifeStatResponse response = LifeStatResponse.fromEntity(entity);

        assertThat(response.category()).isEqualTo(StatCategory.FITNESS);
        assertThat(response.displayName()).isEqualTo("Fitness");
        assertThat(response.metadata()).isNull();
    }

    @Test
    @DisplayName("fromEntity() works for all categories")
    void fromEntity_worksForAllCategories() {
        for (StatCategory category : StatCategory.values()) {
            LifeStat entity = LifeStat.builder()
                    .id(UUID.randomUUID())
                    .userId(UUID.randomUUID())
                    .category(category)
                    .value(42)
                    .lastUpdated(Instant.now())
                    .build();

            LifeStatResponse response = LifeStatResponse.fromEntity(entity);

            assertThat(response.category()).isEqualTo(category);
            assertThat(response.displayName()).isEqualTo(category.getDisplayName());
        }
    }
}
