package com.onnyth.onnythserver.unit.model;

import com.onnyth.onnythserver.models.StatDomain;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for the StatDomain enum.
 */
@DisplayName("StatDomain")
class StatDomainTest {

    @Nested
    @DisplayName("enum properties")
    class EnumProperties {

        @Test
        @DisplayName("all 5 domains exist")
        void allDomainsExist() {
            assertThat(StatDomain.values()).hasSize(5);
        }

        @Test
        @DisplayName("OCCUPATION has correct display name and weight")
        void occupationProperties() {
            assertThat(StatDomain.OCCUPATION.getDisplayName()).isEqualTo("Occupation");
            assertThat(StatDomain.OCCUPATION.getWeight()).isEqualTo(1.2);
        }

        @Test
        @DisplayName("WEALTH has correct display name and weight")
        void wealthProperties() {
            assertThat(StatDomain.WEALTH.getDisplayName()).isEqualTo("Wealth");
            assertThat(StatDomain.WEALTH.getWeight()).isEqualTo(1.0);
        }

        @Test
        @DisplayName("PHYSIQUE has correct display name and weight")
        void physiqueProperties() {
            assertThat(StatDomain.PHYSIQUE.getDisplayName()).isEqualTo("Physique");
            assertThat(StatDomain.PHYSIQUE.getWeight()).isEqualTo(1.1);
        }

        @Test
        @DisplayName("WISDOM has highest weight at 1.3")
        void wisdomHasHighestWeight() {
            assertThat(StatDomain.WISDOM.getWeight()).isEqualTo(1.3);
        }

        @Test
        @DisplayName("CHARISMA has lowest weight at 0.9")
        void charismaHasLowestWeight() {
            assertThat(StatDomain.CHARISMA.getWeight()).isEqualTo(0.9);
        }

        @Test
        @DisplayName("all weights sum to approximately 5.5")
        void weightsSum() {
            double sum = 0;
            for (StatDomain d : StatDomain.values()) {
                sum += d.getWeight();
            }
            assertThat(sum).isEqualTo(5.5);
        }
    }
}
