package com.onnyth.onnythserver.lifestats.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user_wealth", uniqueConstraints = @UniqueConstraint(columnNames = "user_id"))
public class UserWealthEntity {

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
}
