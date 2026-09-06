package com.onnyth.onnythserver.lifestats.adapter.out.persistence;

import com.onnyth.onnythserver.lifestats.domain.model.UserWealth;

public final class UserWealthPersistenceMapper {

    private UserWealthPersistenceMapper() {
    }

    public static UserWealth toDomain(UserWealthEntity entity) {
        if (entity == null) {
            return null;
        }
        return UserWealth.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .incomeBracket(entity.getIncomeBracket())
                .incomeVerified(entity.getIncomeVerified())
                .netWorthBracket(entity.getNetWorthBracket())
                .monthlySpendingBracket(entity.getMonthlySpendingBracket())
                .monthlySavingPct(entity.getMonthlySavingPct())
                .incomeCurrency(entity.getIncomeCurrency())
                .score(entity.getScore())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public static UserWealthEntity toEntity(UserWealth domain) {
        if (domain == null) {
            return null;
        }
        return UserWealthEntity.builder()
                .id(domain.getId())
                .userId(domain.getUserId())
                .incomeBracket(domain.getIncomeBracket())
                .incomeVerified(domain.getIncomeVerified())
                .netWorthBracket(domain.getNetWorthBracket())
                .monthlySpendingBracket(domain.getMonthlySpendingBracket())
                .monthlySavingPct(domain.getMonthlySavingPct())
                .incomeCurrency(domain.getIncomeCurrency())
                .score(domain.getScore())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }
}
