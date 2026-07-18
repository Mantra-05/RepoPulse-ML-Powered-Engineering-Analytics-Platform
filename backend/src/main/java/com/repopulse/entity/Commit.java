package com.repopulse.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Represents a single Git commit belonging to a {@link Repository}.
 */
@Entity
@Table(name = "commits")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Commit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Full SHA-1 hash of the commit */
    @Column(name = "sha", nullable = false, unique = true, length = 40)
    private String sha;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Column(name = "author_login")
    private String authorLogin;

    @Column(name = "author_name")
    private String authorName;

    @Column(name = "author_email")
    private String authorEmail;

    @Column(name = "committed_at")
    private LocalDateTime committedAt;

    /** Lines added in this commit */
    private Integer additions;

    /** Lines deleted in this commit */
    private Integer deletions;

    /** Total files changed */
    @Column(name = "changed_files")
    private Integer changedFiles;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repository_id", nullable = false)
    private Repository repository;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
