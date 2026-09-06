package com.onnyth.onnythserver.user.adapter.out.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserJpaRepository extends JpaRepository<UserEntity, UUID> {
    Optional<UserEntity> findByUsername(String username);

    Optional<UserEntity> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByUsernameIgnoreCase(String username);

    boolean existsByEmail(String email);

    Page<UserEntity> findAllByOrderByTotalScoreDesc(Pageable pageable);

    long countByTotalScoreGreaterThan(long score);

    @Query("SELECT u FROM UserEntity u WHERE u.id <> :excludeUserId " +
            "AND (LOWER(u.username) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<UserEntity> searchByUsernameOrFullName(
            @Param("query") String query,
            @Param("excludeUserId") UUID excludeUserId,
            Pageable pageable);

    /** All users ordered by score descending — used by world rank computation. */
    @Query("SELECT u FROM UserEntity u ORDER BY u.totalScore DESC")
    List<UserEntity> findAllOrderedByScoreDesc();

    /** All users in a specific country ordered by score descending. */
    @Query("SELECT u FROM UserEntity u WHERE u.country = :country ORDER BY u.totalScore DESC")
    List<UserEntity> findByCountryOrderByScoreDesc(@Param("country") String country);

    /** Distinct country codes with at least one user. */
    @Query("SELECT DISTINCT u.country FROM UserEntity u WHERE u.country IS NOT NULL")
    List<String> findDistinctCountries();
}
