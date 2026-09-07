package com.onnyth.onnythserver.quest.domain.model;

import com.onnyth.onnythserver.shared.domain.model.StatDomain;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * A quest is a system-created challenge that users can complete to earn XP.
 * Quests have a category (maps to StatDomain), an XP reward, and an optional
 * deadline.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Quest {

    private UUID id;
    private String title;
    private String description;
    private Integer xpReward;
    private StatDomain category;

    @Builder.Default
    private QuestStatus status = QuestStatus.ACTIVE;

    private Instant deadline;

    @Builder.Default
    private Instant createdAt = Instant.now();
}
