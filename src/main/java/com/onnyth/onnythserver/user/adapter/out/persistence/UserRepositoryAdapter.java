package com.onnyth.onnythserver.user.adapter.out.persistence;

import com.onnyth.onnythserver.user.application.port.UserRepository;
import com.onnyth.onnythserver.user.domain.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Repository
@RequiredArgsConstructor
public class UserRepositoryAdapter implements UserRepository {

    private final UserJpaRepository userJpaRepository;

    @Override
    public User save(User user) {
        UserEntity saved = userJpaRepository.save(UserPersistenceMapper.toEntity(user));
        return UserPersistenceMapper.toDomain(saved);
    }

    @Override
    public List<User> saveAll(Iterable<User> users) {
        List<UserEntity> entities = StreamSupport.stream(users.spliterator(), false)
                .map(UserPersistenceMapper::toEntity)
                .collect(Collectors.toList());
        return userJpaRepository.saveAll(entities).stream()
                .map(UserPersistenceMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<User> findById(UUID id) {
        return userJpaRepository.findById(id).map(UserPersistenceMapper::toDomain);
    }

    @Override
    public List<User> findAll() {
        return userJpaRepository.findAll().stream()
                .map(UserPersistenceMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<User> findAllById(Iterable<UUID> ids) {
        return userJpaRepository.findAllById(ids).stream()
                .map(UserPersistenceMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsById(UUID id) {
        return userJpaRepository.existsById(id);
    }

    @Override
    public void deleteById(UUID id) {
        userJpaRepository.deleteById(id);
    }

    @Override
    public void deleteAll() {
        userJpaRepository.deleteAll();
    }

    @Override
    public void flush() {
        userJpaRepository.flush();
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return userJpaRepository.findByUsername(username).map(UserPersistenceMapper::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userJpaRepository.findByEmail(email).map(UserPersistenceMapper::toDomain);
    }

    @Override
    public boolean existsByUsername(String username) {
        return userJpaRepository.existsByUsername(username);
    }

    @Override
    public boolean existsByUsernameIgnoreCase(String username) {
        return userJpaRepository.existsByUsernameIgnoreCase(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userJpaRepository.existsByEmail(email);
    }

    @Override
    public Page<User> findAllByOrderByTotalScoreDesc(Pageable pageable) {
        return userJpaRepository.findAllByOrderByTotalScoreDesc(pageable).map(UserPersistenceMapper::toDomain);
    }

    @Override
    public long countByTotalScoreGreaterThan(long score) {
        return userJpaRepository.countByTotalScoreGreaterThan(score);
    }

    @Override
    public Page<User> searchByUsernameOrFullName(String query, UUID excludeUserId, Pageable pageable) {
        return userJpaRepository.searchByUsernameOrFullName(query, excludeUserId, pageable)
                .map(UserPersistenceMapper::toDomain);
    }

    @Override
    public List<User> findAllOrderedByScoreDesc() {
        return userJpaRepository.findAllOrderedByScoreDesc().stream()
                .map(UserPersistenceMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<User> findByCountryOrderByScoreDesc(String country) {
        return userJpaRepository.findByCountryOrderByScoreDesc(country).stream()
                .map(UserPersistenceMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<String> findDistinctCountries() {
        return userJpaRepository.findDistinctCountries();
    }
}
