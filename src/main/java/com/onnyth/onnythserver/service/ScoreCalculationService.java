package com.onnyth.onnythserver.service;

import com.onnyth.onnythserver.exceptions.UserNotFoundException;
import com.onnyth.onnythserver.models.*;
import com.onnyth.onnythserver.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Central service for computing domain-specific and total scores.
 * Each domain is scored 0-100. Total score is a weighted sum of all domain scores.
 *
 * Domain scoring formulas:
 *   OCCUPATION: job_title_tier(0-40) + company_factor(0-25) + experience(0-20) + skills(0-15)
 *   WEALTH:     income_bracket(0-40) + onnyth_coins(0-35) + savings(0-15) + verified(0-10)
 *   PHYSIQUE:   fitness_level(0-30) + body_comp(0-25) + workouts(0-25) + medals(0-20)
 *   WISDOM:     education(0-35) + hobbies(0-15) + xfactors(0-50)
 *   CHARISMA:   social_followers(0-35) + onnyth_followers(0-30) + profile_likes(0-20) + verified(0-15)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ScoreCalculationService {

    private final UserRepository userRepository;
    private final UserOccupationRepository occupationRepository;
    private final UserWealthRepository wealthRepository;
    private final UserPhysiqueRepository physiqueRepository;
    private final SportMedalRepository sportMedalRepository;
    private final UserWisdomRepository wisdomRepository;
    private final UserEducationRepository educationRepository;
    private final UserXfactorRepository xfactorRepository;
    private final UserCharismaRepository charismaRepository;
    private final UserSocialAccountRepository socialAccountRepository;
    private final ProfileLikeRepository profileLikeRepository;
    private final FollowRepository followRepository;
    private final ScoreHistoryRepository scoreHistoryRepository;

    // ─── Domain Score Calculators ─────────────────────────────────────────────

    /**
     * Recalculate the Occupation domain score for a user.
     */
    @Transactional
    public int recalculateOccupation(UUID userId) {
        UserOccupation occ = occupationRepository.findByUserIdAndIsCurrentTrue(userId)
                .orElse(null);
        if (occ == null) return 0;

        int titleTier = classifyJobTitleTier(occ.getJobTitle());             // 0-40
        int companyFactor = 15;                                                // flat 15 for MVP (no company DB yet)
        int experiencePts = Math.min((occ.getYearsExperience() != null ? occ.getYearsExperience() : 0) * 2, 20); // 0-20
        int skillsBonus = Math.min((occ.getSkills() != null ? occ.getSkills().size() : 0) * 3, 15);               // 0-15

        int newScore = Math.min(titleTier + companyFactor + experiencePts + skillsBonus, 100);
        return persistDomainScore(userId, occ, occ.getScore(), newScore, StatDomain.OCCUPATION);
    }

    /**
     * Recalculate the Wealth domain score. Reads onnyth_coins from User table.
     */
    @Transactional
    public int recalculateWealth(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId.toString()));
        UserWealth wealth = wealthRepository.findByUserId(userId).orElse(null);

        int incomePts = 0;
        int savingsPts = 0;
        int verifiedPts = 0;

        if (wealth != null) {
            IncomeBracket bracket = wealth.getIncomeBracketEnum();
            incomePts = bracket != null ? bracket.getScoreContribution() : 0;           // 0-40
            savingsPts = wealth.getMonthlySavingPct() != null
                    ? (int) (wealth.getMonthlySavingPct() / 100.0 * 15) : 0;            // 0-15
            verifiedPts = Boolean.TRUE.equals(wealth.getIncomeVerified()) ? 10 : 0;     // 0-10
        }

        int coinsPts = Math.min(user.getOnnythCoins() / 100, 35);                       // 0-35

        int newScore = Math.min(incomePts + coinsPts + savingsPts + verifiedPts, 100);

        if (wealth != null) {
            return persistDomainScore(userId, wealth, wealth.getScore(), newScore, StatDomain.WEALTH);
        }
        return newScore;
    }

    /**
     * Recalculate the Physique domain score.
     */
    @Transactional
    public int recalculatePhysique(UUID userId) {
        UserPhysique physique = physiqueRepository.findByUserId(userId).orElse(null);
        if (physique == null) return 0;

        int fitnessLevelPts = physique.getFitnessLevel() != null
                ? physique.getFitnessLevel().getScoreContribution() : 0;                 // 0-30
        int bodyCompPts = calculateBodyCompScore(physique);                              // 0-25
        int workoutPts = Math.min(
                (physique.getWeeklyWorkouts() != null ? physique.getWeeklyWorkouts() : 0) * 5, 25); // 0-25

        // Medal score
        List<SportMedal> medals = sportMedalRepository.findAllByUserId(userId);
        int medalPts = calculateMedalScore(medals);                                      // 0-20

        int newScore = Math.min(fitnessLevelPts + bodyCompPts + workoutPts + medalPts, 100);
        return persistDomainScore(userId, physique, physique.getScore(), newScore, StatDomain.PHYSIQUE);
    }

    /**
     * Recalculate the Wisdom domain score.
     */
    @Transactional
    public int recalculateWisdom(UUID userId) {
        UserWisdom wisdom = wisdomRepository.findByUserId(userId).orElse(null);

        // Education score
        UserEducation highest = educationRepository.findByUserIdAndIsHighestTrue(userId).orElse(null);
        int educationPts = highest != null ? highest.getLevel().getScoreContribution() : 0; // 0-35

        // Hobbies score
        int hobbiesPts = 0;
        if (wisdom != null && wisdom.getHobbies() != null) {
            hobbiesPts = Math.min(wisdom.getHobbies().size() * 5, 15);                  // 0-15
        }

        // XFactor score
        List<UserXfactor> xfactors = xfactorRepository.findAllByUserId(userId);
        int xfactorPts = calculateXfactorScore(xfactors);                               // 0-50

        int newScore = Math.min(educationPts + hobbiesPts + xfactorPts, 100);

        if (wisdom != null) {
            return persistDomainScore(userId, wisdom, wisdom.getScore(), newScore, StatDomain.WISDOM);
        }
        return newScore;
    }

    /**
     * Recalculate the Charisma domain score.
     */
    @Transactional
    public int recalculateCharisma(UUID userId) {
        UserCharisma charisma = charismaRepository.findByUserId(userId).orElse(null);

        // External social followers (log-scaled)
        int totalFollowers = socialAccountRepository.getTotalFollowerCount(userId);
        int socialFollowersPts = Math.min((int) (Math.log10(totalFollowers + 1) * 10), 35); // 0-35

        // Onnyth followers (from follows table)
        long onnythFollowers = followRepository.countByFollowingId(userId);
        int onnythFollowersPts = Math.min((int) (onnythFollowers * 2), 30);              // 0-30

        // Profile likes
        long profileLikes = profileLikeRepository.countByLikedId(userId);
        int profileLikesPts = Math.min((int) profileLikes, 20);                         // 0-20

        // Verified platforms bonus
        List<UserSocialAccount> accounts = socialAccountRepository.findAllByUserId(userId);
        long verifiedCount = accounts.stream().filter(a -> Boolean.TRUE.equals(a.getIsVerified())).count();
        int verifiedPts = Math.min((int) (verifiedCount * 5), 15);                       // 0-15

        int newScore = Math.min(socialFollowersPts + onnythFollowersPts + profileLikesPts + verifiedPts, 100);

        if (charisma != null) {
            charisma.setOnnythProfileLikes((int) profileLikes); // sync denormalized counter
            return persistDomainScore(userId, charisma, charisma.getScore(), newScore, StatDomain.CHARISMA);
        }
        return newScore;
    }

    // ─── Total Score ──────────────────────────────────────────────────────────

    /**
     * Recalculate ALL domain scores and the weighted total score.
     * Persists the total score on the users table.
     */
    @Transactional
    public long recalculateAll(UUID userId) {
        int occupation = recalculateOccupation(userId);
        int wealth = recalculateWealth(userId);
        int physique = recalculatePhysique(userId);
        int wisdom = recalculateWisdom(userId);
        int charisma = recalculateCharisma(userId);

        double weightedTotal =
                occupation * StatDomain.OCCUPATION.getWeight() +
                wealth * StatDomain.WEALTH.getWeight() +
                physique * StatDomain.PHYSIQUE.getWeight() +
                wisdom * StatDomain.WISDOM.getWeight() +
                charisma * StatDomain.CHARISMA.getWeight();

        long totalScore = Math.round(weightedTotal);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId.toString()));
        long oldTotal = user.getTotalScore();
        user.setTotalScore(totalScore);
        userRepository.save(user);

        if (oldTotal != totalScore) {
            scoreHistoryRepository.save(ScoreHistory.builder()
                    .userId(userId)
                    .domain(null) // null domain means TOTAL in history (domain col allows TOTAL)
                    .oldScore((int) oldTotal)
                    .newScore((int) totalScore)
                    .reason("Full recalculation")
                    .build());
        }

        log.info("Score recalculated: userId={}, occ={}, wealth={}, physique={}, wisdom={}, charisma={}, total={}",
                userId, occupation, wealth, physique, wisdom, charisma, totalScore);

        return totalScore;
    }

    // ─── Helper Methods ───────────────────────────────────────────────────────

    private int classifyJobTitleTier(String jobTitle) {
        if (jobTitle == null || jobTitle.isBlank()) return 0;
        String lower = jobTitle.toLowerCase();

        if (lower.contains("ceo") || lower.contains("cto") || lower.contains("cfo") ||
            lower.contains("chief") || lower.contains("founder") || lower.contains("co-founder"))
            return 40;
        if (lower.contains("vp") || lower.contains("vice president"))
            return 35;
        if (lower.contains("director"))
            return 30;
        if (lower.contains("head") || lower.contains("principal"))
            return 28;
        if (lower.contains("lead") || lower.contains("staff"))
            return 25;
        if (lower.contains("senior") || lower.contains("sr"))
            return 20;
        if (lower.contains("mid") || lower.contains("engineer") || lower.contains("developer") ||
            lower.contains("analyst") || lower.contains("designer") || lower.contains("manager"))
            return 15;
        if (lower.contains("junior") || lower.contains("jr") || lower.contains("associate"))
            return 10;
        if (lower.contains("intern"))
            return 5;

        return 10; // default for unclassified titles
    }

    private int calculateBodyCompScore(UserPhysique physique) {
        if (physique.getBodyFatPct() == null) return 10; // partial credit for filling data

        double bodyFat = physique.getBodyFatPct().doubleValue();
        // Healthy ranges score higher: 8-20% male / 15-28% female (using unisex midpoint)
        if (bodyFat >= 10 && bodyFat <= 22) return 25;
        if (bodyFat >= 6 && bodyFat <= 28) return 20;
        if (bodyFat >= 3 && bodyFat <= 35) return 15;
        return 10;
    }

    private int calculateMedalScore(List<SportMedal> medals) {
        if (medals == null || medals.isEmpty()) return 0;
        int total = 0;
        for (SportMedal medal : medals) {
            total += medal.getMedalType().getScoreContribution();
        }
        return Math.min(total, 20);
    }

    private int calculateXfactorScore(List<UserXfactor> xfactors) {
        if (xfactors == null || xfactors.isEmpty()) return 0;
        int total = 0;
        for (UserXfactor xf : xfactors) {
            int base = 8; // base per x-factor entry
            if (xf.getMetricValue() != null && xf.getMetricValue() > 0) {
                base += Math.min((int) (Math.log10(xf.getMetricValue()) * 5), 15);
            }
            if (Boolean.TRUE.equals(xf.getIsVerified())) {
                base *= 2;
            }
            total += base;
        }
        return Math.min(total, 50);
    }

    /**
     * Persist a domain score change and record history.
     * Works generically for any entity that has setScore/getScore.
     */
    private <T> int persistDomainScore(UUID userId, Object entity, int oldScore, int newScore, StatDomain domain) {
        if (oldScore != newScore) {
            // Set score via reflection-like approach — each entity has setScore
            if (entity instanceof UserOccupation e) e.setScore(newScore);
            else if (entity instanceof UserWealth e) e.setScore(newScore);
            else if (entity instanceof UserPhysique e) e.setScore(newScore);
            else if (entity instanceof UserWisdom e) e.setScore(newScore);
            else if (entity instanceof UserCharisma e) e.setScore(newScore);

            scoreHistoryRepository.save(ScoreHistory.builder()
                    .userId(userId)
                    .domain(domain)
                    .oldScore(oldScore)
                    .newScore(newScore)
                    .reason("Domain recalculation")
                    .build());
        }
        return newScore;
    }
}
