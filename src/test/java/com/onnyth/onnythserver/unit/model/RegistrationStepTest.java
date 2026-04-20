package com.onnyth.onnythserver.unit.model;

import com.onnyth.onnythserver.models.RegistrationStep;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for the RegistrationStep enum.
 */
@DisplayName("RegistrationStep")
class RegistrationStepTest {

    @Nested
    @DisplayName("enum properties")
    class EnumProperties {

        @Test
        @DisplayName("has 8 steps in correct order")
        void hasCorrectStepCount() {
            assertThat(RegistrationStep.values()).hasSize(8);
        }

        @Test
        @DisplayName("allInOrder returns steps sorted by order")
        void allInOrder() {
            List<RegistrationStep> steps = RegistrationStep.allInOrder();

            assertThat(steps.get(0)).isEqualTo(RegistrationStep.PHONE);
            assertThat(steps.get(1)).isEqualTo(RegistrationStep.NAME);
            assertThat(steps.get(2)).isEqualTo(RegistrationStep.IMAGE);
            assertThat(steps.get(7)).isEqualTo(RegistrationStep.CHARISMA);
        }

        @Test
        @DisplayName("PHONE and NAME are required, others are optional")
        void requiredVsOptional() {
            assertThat(RegistrationStep.PHONE.isRequired()).isTrue();
            assertThat(RegistrationStep.NAME.isRequired()).isTrue();
            assertThat(RegistrationStep.IMAGE.isOptional()).isTrue();
            assertThat(RegistrationStep.OCCUPATION.isOptional()).isTrue();
            assertThat(RegistrationStep.WEALTH.isOptional()).isTrue();
            assertThat(RegistrationStep.PHYSIQUE.isOptional()).isTrue();
            assertThat(RegistrationStep.WISDOM.isOptional()).isTrue();
            assertThat(RegistrationStep.CHARISMA.isOptional()).isTrue();
        }

        @Test
        @DisplayName("requiredSteps returns only PHONE and NAME")
        void requiredSteps() {
            List<RegistrationStep> required = RegistrationStep.requiredSteps();

            assertThat(required).containsExactly(RegistrationStep.PHONE, RegistrationStep.NAME);
        }
    }

    @Nested
    @DisplayName("next()")
    class Next {

        @Test
        @DisplayName("PHONE → NAME")
        void phoneNext() {
            assertThat(RegistrationStep.PHONE.next()).isEqualTo(RegistrationStep.NAME);
        }

        @Test
        @DisplayName("WISDOM → CHARISMA")
        void wisdomNext() {
            assertThat(RegistrationStep.WISDOM.next()).isEqualTo(RegistrationStep.CHARISMA);
        }

        @Test
        @DisplayName("CHARISMA → null (last step)")
        void charismaNextIsNull() {
            assertThat(RegistrationStep.CHARISMA.next()).isNull();
        }
    }

    @Nested
    @DisplayName("fromKey()")
    class FromKey {

        @Test
        @DisplayName("parses valid key")
        void parsesValidKey() {
            assertThat(RegistrationStep.fromKey("PHONE")).isEqualTo(RegistrationStep.PHONE);
            assertThat(RegistrationStep.fromKey("phone")).isEqualTo(RegistrationStep.PHONE);
        }

        @Test
        @DisplayName("returns null for invalid key")
        void returnsNullForInvalidKey() {
            assertThat(RegistrationStep.fromKey("INVALID")).isNull();
        }
    }
}
