package com.onnyth.onnythserver.user.application.port;

import com.onnyth.onnythserver.user.domain.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository {

    User save(User user);

    List<User> saveAll(Iterable<User> users);

    Optional<User> findById(UUID id);

    List<User> findAll();

    List<User> findAllById(Iterable<UUID> ids);

    boolean existsById(UUID id);

    void deleteById(UUID id);

    void deleteAll();

    void flush();

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByUsernameIgnoreCase(String username);

    boolean existsByEmail(String email);

    Page<User> findAllByOrderByTotalScoreDesc(Pageable pageable);

    long countByTotalScoreGreaterThan(long score);

    Page<User> searchByUsernameOrFullName(String query, UUID excludeUserId, Pageable pageable);

    /** All users ordered by score descending — used by world rank computation. */
    List<User> findAllOrderedByScoreDesc();

    /** All users in a specific country ordered by score descending. */
    List<User> findByCountryOrderByScoreDesc(String country);

    /** Distinct country codes with at least one user. */
    List<String> findDistinctCountries();
}
