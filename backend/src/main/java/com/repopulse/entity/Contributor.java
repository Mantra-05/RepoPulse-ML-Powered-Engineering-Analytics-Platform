package com.repopulse.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Represents a contributor to a tracked {@link Repository}.
 */
@Entity
@Table(name = "contributors")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Contributor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** GitHub login of the contributor */
    @Column(name = "login", nullable = false)
    private String login;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Column(name = "github_url")
    private String githubUrl;

    /** Total commits to this repository */
    @Column(name = "contributions_count")
    private Integer contributionsCount;

    /** Total lines added across all commits */
    @Column(name = "total_additions")
    private Integer totalAdditions;

    /** Total lines deleted across all commits */
    @Column(name = "total_deletions")
    private Integer totalDeletions;

    /** Total pull requests opened */
    @Column(name = "pull_requests_opened")
    private Integer pullRequestsOpened;

    /** Total pull requests merged */
    @Column(name = "pull_requests_merged")
    private Integer pullRequestsMerged;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repository_id", nullable = false)
    private Repository repository;

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
}
