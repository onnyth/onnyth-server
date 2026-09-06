package com.onnyth.onnythserver.lifestats.adapter.out.persistence;

import com.onnyth.onnythserver.lifestats.domain.model.EmploymentType;
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
@Table(name = "user_occupation")
public class UserOccupationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "job_title", length = 100)
    private String jobTitle;

    @Column(name = "raw_job_title", length = 150)
    private String rawJobTitle;

    @Column(name = "company_name", length = 150)
    private String companyName;

    @Column(name = "raw_company_name", length = 200)
    private String rawCompanyName;

    @Column(name = "is_verified", nullable = false)
    @Builder.Default
    private Boolean isVerified = false;

    @Column(name = "industry", length = 50)
    private String industry;

    @Enumerated(EnumType.STRING)
    @Column(name = "employment_type", length = 20)
    private EmploymentType employmentType;

    @Column(name = "years_experience")
    private Integer yearsExperience;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "skills", nullable = false, columnDefinition = "jsonb")
    @Builder.Default
    private List<String> skills = new ArrayList<>();

    @Column(name = "is_current", nullable = false)
    @Builder.Default
    private Boolean isCurrent = true;

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
