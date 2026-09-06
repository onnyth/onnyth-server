package com.onnyth.onnythserver.profile.adapter.out.persistence;

import com.onnyth.onnythserver.profile.application.port.ProfileVoteRepository;
import com.onnyth.onnythserver.profile.domain.model.ProfileVote;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class ProfileVoteRepositoryAdapter implements ProfileVoteRepository {

    private final ProfileVoteJpaRepository profileVoteJpaRepository;

    @Override
    public ProfileVote save(ProfileVote vote) {
        ProfileVoteEntity saved = profileVoteJpaRepository.save(ProfileVotePersistenceMapper.toEntity(vote));
        return ProfileVotePersistenceMapper.toDomain(saved);
    }

    @Override
    public Optional<ProfileVote> findByVoterIdAndTargetId(UUID voterId, UUID targetId) {
        return profileVoteJpaRepository.findByVoterIdAndTargetId(voterId, targetId)
                .map(ProfileVotePersistenceMapper::toDomain);
    }

    @Override
    public long countByTargetIdAndIsUpvoteTrue(UUID targetId) {
        return profileVoteJpaRepository.countByTargetIdAndIsUpvoteTrue(targetId);
    }

    @Override
    public long countByTargetIdAndIsUpvoteFalse(UUID targetId) {
        return profileVoteJpaRepository.countByTargetIdAndIsUpvoteFalse(targetId);
    }

    @Override
    public void deleteByVoterIdAndTargetId(UUID voterId, UUID targetId) {
        profileVoteJpaRepository.deleteByVoterIdAndTargetId(voterId, targetId);
    }

    @Override
    public boolean existsByVoterIdAndTargetId(UUID voterId, UUID targetId) {
        return profileVoteJpaRepository.existsByVoterIdAndTargetId(voterId, targetId);
    }

    @Override
    public int computeNetVoteScore(UUID targetId) {
        return profileVoteJpaRepository.computeNetVoteScore(targetId);
    }

    @Override
    public void deleteAllByVoterId(UUID voterId) {
        profileVoteJpaRepository.deleteAllByVoterId(voterId);
    }

    @Override
    public void deleteAllByTargetId(UUID targetId) {
        profileVoteJpaRepository.deleteAllByTargetId(targetId);
    }
}
