package com.onnyth.onnythserver.lifestats.adapter.out.persistence;

import com.onnyth.onnythserver.lifestats.domain.model.EducationLevel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user_education")
public class UserEducationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "level", nullable = false, length = 30)
    private EducationLevel level;

    @Column(name = "institution", nullable = false, length = 200)
    private String institution;

    @Column(name = "field_of_study", length = 100)
    private String fieldOfStudy;

    @Column(name = "graduation_year")
    private Integer graduationYear;

    @Column(name = "is_highest", nullable = false)
    @Builder.Default
    private Boolean isHighest = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    @ColumnDefault("now()")
    @Builder.Default
    private Instant createdAt = Instant.now();
}
