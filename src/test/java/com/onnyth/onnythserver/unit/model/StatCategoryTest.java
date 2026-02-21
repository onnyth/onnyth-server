package com.onnyth.onnythserver.unit.model;

import com.onnyth.onnythserver.models.StatCategory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for StatCategory enum.
 */
class StatCategoryTest {

    @Nested
    @DisplayName("isValidValue()")
    class IsValidValue {

        @ParameterizedTest
        @ValueSource(ints = { 1, 50, 100 })
        @DisplayName("returns true for valid values within range")
        void returnsTrue_forValidValues(int value) {
            for (StatCategory category : StatCategory.values()) {
                assertThat(category.isValidValue(value)).isTrue();
            }
        }

        @ParameterizedTest
        @ValueSource(ints = { 0, -1, -100, 101, 200 })
        @DisplayName("returns false for values outside range")
        void returnsFalse_forOutOfRangeValues(int value) {
            for (StatCategory category : StatCategory.values()) {
                assertThat(category.isValidValue(value)).isFalse();
            }
        }

        @Test
        @DisplayName("boundary: 1 is valid")
        void boundary_minIsValid() {
            assertThat(StatCategory.CAREER.isValidValue(1)).isTrue();
        }

        @Test
        @DisplayName("boundary: 100 is valid")
        void boundary_maxIsValid() {
            assertThat(StatCategory.CAREER.isValidValue(100)).isTrue();
        }

        @Test
        @DisplayName("boundary: 0 is invalid")
        void boundary_belowMinIsInvalid() {
            assertThat(StatCategory.CAREER.isValidValue(0)).isFalse();
        }

        @Test
        @DisplayName("boundary: 101 is invalid")
        void boundary_aboveMaxIsInvalid() {
            assertThat(StatCategory.CAREER.isValidValue(101)).isFalse();
        }
    }

    @Nested
    @DisplayName("enum properties")
    class EnumProperties {

        @Test
        @DisplayName("all 5 categories exist")
        void allCategoriesExist() {
            assertThat(StatCategory.values()).hasSize(5);
        }

        @Test
        @DisplayName("CAREER has correct display name")
        void careerDisplayName() {
            assertThat(StatCategory.CAREER.getDisplayName()).isEqualTo("Career");
        }

        @Test
        @DisplayName("SOCIAL_INFLUENCE has correct display name")
        void socialInfluenceDisplayName() {
            assertThat(StatCategory.SOCIAL_INFLUENCE.getDisplayName()).isEqualTo("Social Influence");
        }

        @Test
        @DisplayName("all categories have range 1-100")
        void allCategoriesRange() {
            for (StatCategory category : StatCategory.values()) {
                assertThat(category.getMinValue()).isEqualTo(1);
                assertThat(category.getMaxValue()).isEqualTo(100);
            }
        }
    }
}
