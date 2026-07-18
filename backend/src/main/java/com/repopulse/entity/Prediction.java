package com.repopulse.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Stores the latest ML prediction result for a {@link PullRequest}.
 *
 * <p>Relationship: {@code PullRequest} ←→ {@code Prediction} is {@code @OneToOne}.
 * Each PR has at most one active prediction record; re-running prediction
 * overwrites the existing row.
 */
@Entity
@Table(name = "predictions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Prediction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ── Relationships ─────────────────────────────────────────────────────────

    /**
     * The pull request this prediction belongs to.
     * Foreign key: {@code prediction.pull_request_id → pull_requests.id}.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pull_request_id", nullable = false, unique = true)
    private PullRequest pullRequest;

    /**
     * Convenience denormalised FK; always equals {@code pullRequest.repository}.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repository_id", nullable = false)
    private Repository repository;

    // ── ML output fields ──────────────────────────────────────────────────────

    /**
     * Composite risk score in the range 0.0 – 1.0 produced by the XGBoost model.
     * Higher = riskier.
     */
    @Column(name = "risk_score", nullable = false)
    private Double riskScore;

    /**
     * Three-class label: {@code LOW}, {@code MEDIUM}, or {@code HIGH}.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "risk_level", nullable = false)
    private RiskLevel riskLevel;

    /**
     * Human-readable merge urgency: {@code P1_CRITICAL}, {@code P2_HIGH},
     * {@code P3_MEDIUM}, {@code P4_LOW}.
     */
    @Column(name = "priority", nullable = false)
    private String priority;

    /** Estimated time to complete review, in hours. */
    @Column(name = "estimated_review_time_hours")
    private Double estimatedReviewTimeHours;

    /**
     * Overall repository health score (0.0 – 1.0) at the time of prediction.
     */
    @Column(name = "repository_health_score")
    private Double repositoryHealthScore;

    /** Raw JSON payload returned by the FastAPI ML service, for debugging. */
    @Column(name = "raw_payload", columnDefinition = "TEXT")
    private String rawPayload;

    /** ML model version tag (e.g. {@code "xgb-v1.0"}). */
    @Column(name = "model_version", length = 32)
    private String modelVersion;

    // ── Audit ─────────────────────────────────────────────────────────────────

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // ── Enums ─────────────────────────────────────────────────────────────────

    public enum RiskLevel {
        LOW,
        MEDIUM,
        HIGH
    }
}
