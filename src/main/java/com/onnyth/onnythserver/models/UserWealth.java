package com.onnyth.onnythserver.models;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;
import java.util.UUID;

/**
 * Tracks a user's financial information for the Wealth domain.
 * Income bracket is cross-checked against occupation for verification.
 * Onnyth Coins balance lives on the users table but feeds into wealth score.
 */
@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user_wealth", uniqueConstraints = @UniqueConstraint(columnNames = "user_id"))
public class UserWealth {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false, unique = true)
    private UUID userId;

    @Column(name = "income_bracket", length = 30)
    private String incomeBracket;

    @Column(name = "income_verified", nullable = false)
    @Builder.Default
    private Boolean incomeVerified = false;

    @Column(name = "net_worth_bracket", length = 30)
    private String netWorthBracket;

    /** Monthly spending bracket using the same IncomeBracket taxonomy. */
    @Column(name = "monthly_spending_bracket", length = 30)
    private String monthlySpendingBracket;

    @Column(name = "monthly_saving_pct")
    private Integer monthlySavingPct;

    @Column(name = "income_currency", nullable = false, length = 3)
    @Builder.Default
    private String incomeCurrency = "USD";

    @Column(name = "score", nullable = false)
    @Builder.Default
    private Integer score = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    @ColumnDefault("now()")
    @Builder.Default
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    @ColumnDefault("now()")
    @Builder.Default
    private Instant updatedAt = Instant.now();

    /**
     * Convenience method to get the typed IncomeBracket enum.
     */
    public IncomeBracket getIncomeBracketEnum() {
        return IncomeBracket.fromDbValue(this.incomeBracket);
    }

    public void setIncomeBracketEnum(IncomeBracket bracket) {
        this.incomeBracket = bracket != null ? bracket.toDbValue() : null;
    }
}
