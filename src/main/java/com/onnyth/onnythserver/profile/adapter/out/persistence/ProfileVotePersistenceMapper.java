package com.onnyth.onnythserver.profile.adapter.out.persistence;

import com.onnyth.onnythserver.profile.domain.model.ProfileVote;

public final class ProfileVotePersistenceMapper {

    private ProfileVotePersistenceMapper() {
    }

    public static ProfileVote toDomain(ProfileVoteEntity entity) {
        if (entity == null) {
            return null;
        }
        return ProfileVote.builder()
                .voterId(entity.getVoterId())
                .targetId(entity.getTargetId())
                .isUpvote(entity.getIsUpvote())
                .votedAt(entity.getVotedAt())
                .build();
    }

    public static ProfileVoteEntity toEntity(ProfileVote domain) {
        if (domain == null) {
            return null;
        }
        return ProfileVoteEntity.builder()
                .voterId(domain.getVoterId())
                .targetId(domain.getTargetId())
                .isUpvote(domain.getIsUpvote())
                .votedAt(domain.getVotedAt())
                .build();
    }
}
