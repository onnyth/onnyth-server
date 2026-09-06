package com.onnyth.onnythserver.lifestats.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * Tracks a user's financial information for the Wealth domain.
 * Income bracket is cross-checked against occupation for verification.
 * Onnyth Coins balance lives on the users table but feeds into wealth score.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserWealth {

    private UUID id;
    private UUID userId;
    private String incomeBracket;

    @Builder.Default
    private Boolean incomeVerified = false;

    private String netWorthBracket;
    private String monthlySpendingBracket;
    private Integer monthlySavingPct;

    @Builder.Default
    private String incomeCurrency = "USD";

    @Builder.Default
    private Integer score = 0;

    @Builder.Default
    private Instant createdAt = Instant.now();

    @Builder.Default
    private Instant updatedAt = Instant.now();

    public IncomeBracket getIncomeBracketEnum() {
        return IncomeBracket.fromDbValue(this.incomeBracket);
    }

    public void setIncomeBracketEnum(IncomeBracket bracket) {
        this.incomeBracket = bracket != null ? bracket.toDbValue() : null;
    }
}
