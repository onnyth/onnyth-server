package com.onnyth.onnythserver.repository;

import com.onnyth.onnythserver.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByUsernameIgnoreCase(String username);

    boolean existsByEmail(String email);

    Page<User> findAllByOrderByTotalScoreDesc(Pageable pageable);

    long countByTotalScoreGreaterThan(long score);

    @Query("SELECT u FROM User u WHERE u.id <> :excludeUserId " +
            "AND (LOWER(u.username) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<User> searchByUsernameOrFullName(
            @Param("query") String query,
            @Param("excludeUserId") UUID excludeUserId,
            Pageable pageable);
}
