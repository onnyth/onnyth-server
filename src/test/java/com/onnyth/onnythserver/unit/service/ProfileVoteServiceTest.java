package com.onnyth.onnythserver.unit.service;

import com.onnyth.onnythserver.dto.VoteResponse;
import com.onnyth.onnythserver.models.ProfileVote;
import com.onnyth.onnythserver.models.User;
import com.onnyth.onnythserver.repository.ProfileVoteRepository;
import com.onnyth.onnythserver.repository.UserRepository;
import com.onnyth.onnythserver.service.ProfileVoteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProfileVoteService")
class ProfileVoteServiceTest {

    @Mock ProfileVoteRepository voteRepository;
    @Mock UserRepository userRepository;

    @InjectMocks ProfileVoteService voteService;

    private final UUID voterId  = UUID.randomUUID();
    private final UUID targetId = UUID.randomUUID();
    private User targetUser;

    @BeforeEach
    void setUp() {
        targetUser = User.builder().id(targetId).voteScore(0).build();
    }

    // ─── castVote ─────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("castVote()")
    class CastVote {

        @Test
        @DisplayName("new upvote is saved and voteScore recomputed")
        void newUpvote_savesAndRecomputes() {
            when(userRepository.findById(targetId)).thenReturn(Optional.of(targetUser));
            when(voteRepository.findByVoterIdAndTargetId(voterId, targetId))
                    .thenReturn(Optional.empty())  // first call: no existing vote
                    .thenReturn(Optional.empty()); // second call in buildResponse: still empty
            when(voteRepository.computeNetVoteScore(targetId)).thenReturn(1);
            when(userRepository.save(any())).thenReturn(targetUser);

            VoteResponse resp = voteService.castVote(voterId, targetId, true);

            verify(voteRepository).save(argThat(v -> Boolean.TRUE.equals(v.getIsUpvote())));
            assertThat(resp.targetUserId()).isEqualTo(targetId);
        }

        @Test
        @DisplayName("existing same-direction vote is idempotent — no extra save")
        void sameDirVote_isIdempotent() {
            ProfileVote existingVote = ProfileVote.builder()
                    .voterId(voterId).targetId(targetId).isUpvote(true).build();
            when(userRepository.findById(targetId)).thenReturn(Optional.of(targetUser));
            when(voteRepository.findByVoterIdAndTargetId(voterId, targetId))
                    .thenReturn(Optional.of(existingVote));
            when(voteRepository.findByVoterIdAndTargetId(voterId, targetId))
                    .thenReturn(Optional.of(existingVote));

            voteService.castVote(voterId, targetId, true);

            // No new record should be saved
            verify(voteRepository, never()).save(any(ProfileVote.class));
        }

        @Test
        @DisplayName("flipping from upvote to downvote updates existing record")
        void flipVote_updatesRecord() {
            ProfileVote existingVote = ProfileVote.builder()
                    .voterId(voterId).targetId(targetId).isUpvote(true).build();
            when(userRepository.findById(targetId)).thenReturn(Optional.of(targetUser));
            when(voteRepository.findByVoterIdAndTargetId(voterId, targetId))
                    .thenReturn(Optional.of(existingVote));
            when(voteRepository.computeNetVoteScore(targetId)).thenReturn(-1);
            when(userRepository.save(any())).thenReturn(targetUser);

            voteService.castVote(voterId, targetId, false);

            assertThat(existingVote.getIsUpvote()).isFalse();
            verify(voteRepository).save(existingVote);
        }

        @Test
        @DisplayName("self-vote throws IllegalArgumentException")
        void selfVote_throws() {
            assertThatThrownBy(() -> voteService.castVote(voterId, voterId, true))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("own profile");
        }

        @Test
        @DisplayName("target user not found → UserNotFoundException")
        void targetNotFound_throws() {
            when(userRepository.findById(targetId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> voteService.castVote(voterId, targetId, true))
                    .hasMessageContaining(targetId.toString());
        }
    }

    // ─── removeVote ───────────────────────────────────────────────────────────

    @Nested
    @DisplayName("removeVote()")
    class RemoveVote {

        @Test
        @DisplayName("existing vote removed and voteScore recomputed")
        void remove_existingVote() {
            when(userRepository.findById(targetId)).thenReturn(Optional.of(targetUser));
            when(voteRepository.existsByVoterIdAndTargetId(voterId, targetId)).thenReturn(true);
            when(voteRepository.computeNetVoteScore(targetId)).thenReturn(2);
            when(userRepository.save(any())).thenReturn(targetUser);

            VoteResponse resp = voteService.removeVote(voterId, targetId);

            verify(voteRepository).deleteByVoterIdAndTargetId(voterId, targetId);
            assertThat(resp.myVote()).isNull();
        }

        @Test
        @DisplayName("no existing vote → no-op, still returns 200 response")
        void remove_noVote_noop() {
            when(userRepository.findById(targetId)).thenReturn(Optional.of(targetUser));
            when(voteRepository.existsByVoterIdAndTargetId(voterId, targetId)).thenReturn(false);

            VoteResponse resp = voteService.removeVote(voterId, targetId);

            verify(voteRepository, never()).deleteByVoterIdAndTargetId(any(), any());
            assertThat(resp.targetUserId()).isEqualTo(targetId);
        }
    }

    // ─── getMyVote ────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("getMyVote()")
    class GetMyVote {

        @Test
        @DisplayName("returns upvote when record exists with isUpvote=true")
        void returnsUpvote() {
            ProfileVote vote = ProfileVote.builder()
                    .voterId(voterId).targetId(targetId).isUpvote(true).build();
            when(userRepository.findById(targetId)).thenReturn(Optional.of(targetUser));
            when(voteRepository.findByVoterIdAndTargetId(voterId, targetId))
                    .thenReturn(Optional.of(vote));

            VoteResponse resp = voteService.getMyVote(voterId, targetId);

            assertThat(resp.myVote()).isTrue();
        }

        @Test
        @DisplayName("returns null myVote when no record exists")
        void returnsNull_whenNoVote() {
            when(userRepository.findById(targetId)).thenReturn(Optional.of(targetUser));
            when(voteRepository.findByVoterIdAndTargetId(voterId, targetId))
                    .thenReturn(Optional.empty());

            VoteResponse resp = voteService.getMyVote(voterId, targetId);

            assertThat(resp.myVote()).isNull();
        }

        @Test
        @DisplayName("returns downvote when record exists with isUpvote=false")
        void returnsDownvote() {
            ProfileVote vote = ProfileVote.builder()
                    .voterId(voterId).targetId(targetId).isUpvote(false).build();
            when(userRepository.findById(targetId)).thenReturn(Optional.of(targetUser));
            when(voteRepository.findByVoterIdAndTargetId(voterId, targetId))
                    .thenReturn(Optional.of(vote));

            VoteResponse resp = voteService.getMyVote(voterId, targetId);

            assertThat(resp.myVote()).isFalse();
        }

        @Test
        @DisplayName("target not found → throws")
        void targetNotFound_throws() {
            when(userRepository.findById(targetId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> voteService.getMyVote(voterId, targetId))
                    .hasMessageContaining(targetId.toString());
        }
    }
}
