package com.onnyth.onnythserver.unit.dto;

import com.onnyth.onnythserver.dto.StatUpdateResponse;
import com.onnyth.onnythserver.models.StatCategory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for StatUpdateResponse DTO.
 */
class StatUpdateResponseTest {

    @Test
    @DisplayName("builder maps all fields correctly")
    void builderMapsAllFields() {
        StatUpdateResponse response = StatUpdateResponse.builder()
                .category(StatCategory.CAREER)
                .displayName("Career")
                .previousValue(50)
                .newValue(75)
                .totalScore(300)
                .scoreChange(25)
                .build();

        assertThat(response.category()).isEqualTo(StatCategory.CAREER);
        assertThat(response.displayName()).isEqualTo("Career");
        assertThat(response.previousValue()).isEqualTo(50);
        assertThat(response.newValue()).isEqualTo(75);
        assertThat(response.totalScore()).isEqualTo(300);
        assertThat(response.scoreChange()).isEqualTo(25);
    }

    @Test
    @DisplayName("scoreChange can be negative")
    void scoreChangeCanBeNegative() {
        StatUpdateResponse response = StatUpdateResponse.builder()
                .category(StatCategory.FITNESS)
                .displayName("Fitness")
                .previousValue(80)
                .newValue(60)
                .totalScore(250)
                .scoreChange(-20)
                .build();

        assertThat(response.scoreChange()).isEqualTo(-20);
        assertThat(response.previousValue()).isGreaterThan(response.newValue());
    }
}
