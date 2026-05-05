package com.onnyth.onnythserver.dto;

/**
 * Compact domain score snapshot embedded in ProfileCardResponse.
 * Carries the raw 0-100 score plus a human-readable label for display.
 *
 * @param domain    the StatDomain key (e.g. "OCCUPATION")
 * @param score     domain score 0–100
 * @param label     human-readable tier label (e.g. "Senior Engineer", "Fit")
 * @param rankBadge emoji or short string representing the domain rank tier
 */
public record DomainScoreDto(
        String domain,
        int score,
        String label,
        String rankBadge
) {
    /** Convenience for a domain with no data yet. */
    public static DomainScoreDto empty(String domain) {
        return new DomainScoreDto(domain, 0, "Not set", "—");
    }
}
