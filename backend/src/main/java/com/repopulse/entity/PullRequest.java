package com.repopulse.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Represents a GitHub Pull Request belonging to a {@link Repository}.
 */
@Entity
@Table(name = "pull_requests",
       uniqueConstraints = @UniqueConstraint(
               name = "uq_pr_repo_number",
               columnNames = {"repository_id", "github_pr_number"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PullRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ── GitHub metadata ───────────────────────────────────────────────────────

    /** GitHub PR number within the repository (1-based). */
    @Column(name = "github_pr_number", nullable = false)
    private Integer githubPrNumber;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String body;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PrState state;

    /** GitHub login of the PR author. */
    @Column(name = "author_login", length = 64)
    private String authorLogin;

    @Column(name = "base_branch", length = 128)
    private String baseBranch;

    @Column(name = "head_branch", length = 128)
    private String headBranch;

    /** Number of files changed in this PR. */
    @Column(name = "changed_files")
    private Integer changedFiles;

    /** Lines added. */
    private Integer additions;

    /** Lines deleted. */
    private Integer deletions;

    /** Number of review (inline) comments. */
    @Column(name = "review_comments")
    private Integer reviewComments;

    /** Number of general issue-style comments. */
    private Integer comments;

    /** Number of commits in this PR. */
    @Column(name = "commits_count")
    private Integer commitsCount;

    /** Number of commits the author has made to this repo (proxy for experience). */
    @Column(name = "author_contributions")
    private Integer authorContributions;

    @Column(name = "opened_at")
    private LocalDateTime openedAt;

    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    @Column(name = "merged_at")
    private LocalDateTime mergedAt;

    // ── Relationships ─────────────────────────────────────────────────────────

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repository_id", nullable = false)
    private Repository repository;

    /**
     * Latest ML prediction for this PR.
     * {@code mappedBy = "pullRequest"} means Prediction owns the FK column.
     */
    @OneToOne(mappedBy = "pullRequest",
              cascade = CascadeType.ALL,
              fetch = FetchType.LAZY,
              orphanRemoval = true)
    private Prediction prediction;

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

    // ── Enum ─────────────────────────────────────────────────────────────────

    public enum PrState {
        OPEN,
        CLOSED,
        MERGED
    }
}
