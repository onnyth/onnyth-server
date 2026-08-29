package com.onnyth.onnythserver.bookmark.adapter.out.persistence;

import com.onnyth.onnythserver.validation.ValidUri;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Table(name = "bookmark")
public class BookmarkEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @NotBlank
    @ValidUri(message = "URL must be a valid http/https URL")
    @Column(name = "url", nullable = false)
    private String url;

    @NotBlank
    @Size(max = 255)
    @Column(name = "title", nullable = false)
    private String title;

    @ElementCollection
    @CollectionTable(
            name = "bookmark_tags",
            joinColumns = @JoinColumn(name = "bookmark_id")
    )
    @Column(name = "tag", length = 50)
    private Set<@Size(max = 50, message = "Each tag must be at most 50 characters") String> tags;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }
}

