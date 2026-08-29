package com.onnyth.onnythserver.bookmark.domain.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Getter
@Builder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Bookmark {

    private UUID id;
    private String url;
    private String title;

    @Builder.Default
    private Set<String> tags = Set.of();

    private Instant createdAt;
    private Instant updatedAt;
}

