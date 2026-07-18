package com.repopulse.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Stores computed engineering metrics for a {@link Repository}.
 * One record per repository, overwritten on each re-analysis.
 */
@Entity
@Table(name = "repository_analyses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RepositoryAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ── Parent ────────────────────────────────────────────────────────────────

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repository_id", nullable = false, unique = true)
    private Repository repository;

    // ── Computed metrics ──────────────────────────────────────────────────────

    /** Average (additions + deletions) across all PRs. */
    @Column(name = "avg_pr_size")
    private Double avgPrSize;

    /**
     * Average time between PR open and close/merge, in hours.
     * Only computed for closed/merged PRs.
     */
    @Column(name = "avg_review_time_hours")
    private Double avgReviewTimeHours;

    /** Average number of commits per PR. */
    @Column(name = "avg_commits_per_pr")
    private Double avgCommitsPerPr;

    /** Average number of files changed per PR. */
    @Column(name = "avg_files_changed")
    private Double avgFilesChanged;

    /**
     * Fraction of PRs that were merged (merged / (merged + closed)).
     * Range: 0.0 – 1.0.
     */
    @Column(name = "merge_rate")
    private Double mergeRate;

    /** Number of currently open PRs. */
    @Column(name = "open_pr_count")
    private Integer openPrCount;

    /** Number of closed (non-merged) PRs. */
    @Column(name = "closed_pr_count")
    private Integer closedPrCount;

    /** Number of merged PRs. */
    @Column(name = "merged_pr_count")
    private Integer mergedPrCount;

    /** Total number of distinct contributors. */
    @Column(name = "contributor_count")
    private Integer contributorCount;

    /** Total number of commits in the repository. */
    @Column(name = "total_commits")
    private Integer totalCommits;

    /**
     * Repository health score derived from merge rate, review time, and activity.
     * Range: 0.0 – 1.0.
     */
    @Column(name = "health_score")
    private Double healthScore;

    // ── Audit ─────────────────────────────────────────────────────────────────

    /** Timestamp of the last analysis run. */
    @Column(name = "analysed_at", nullable = false)
    private LocalDateTime analysedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        analysedAt = LocalDateTime.now();
    }
}
