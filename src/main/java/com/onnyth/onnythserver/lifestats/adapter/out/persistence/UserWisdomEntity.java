package com.onnyth.onnythserver.lifestats.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user_wisdom", uniqueConstraints = @UniqueConstraint(columnNames = "user_id"))
public class UserWisdomEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false, unique = true)
    private UUID userId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "hobbies", nullable = false, columnDefinition = "jsonb")
    @Builder.Default
    private List<String> habitIds = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "languages", nullable = false, columnDefinition = "jsonb")
    @Builder.Default
    private List<String> languages = new ArrayList<>();

    @Column(name = "education_level", length = 30)
    private String educationLevel;

    @Column(name = "institution_name", length = 200)
    private String institutionName;

    @Column(name = "graduation_year")
    private Integer graduationYear;

    @Column(name = "score", nullable = false)
    @Builder.Default
    private Integer score = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    @ColumnDefault("now()")
    @Builder.Default
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    @ColumnDefault("now()")
    @Builder.Default
    private Instant updatedAt = Instant.now();
}
