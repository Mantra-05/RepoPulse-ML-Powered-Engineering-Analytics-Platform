package com.repopulse.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Computed engineering metrics for a repository.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RepositoryAnalysisResponse {

    private Long repositoryId;
    private String repositoryFullName;

    // ── Metrics ───────────────────────────────────────────────────────────────

    /** Average PR size (additions + deletions). */
    private Double avgPrSize;

    /** Average review/close time in hours (closed/merged PRs only). */
    private Double avgReviewTimeHours;

    /** Average number of commits per PR. */
    private Double avgCommitsPerPr;

    /** Average number of files changed per PR. */
    private Double avgFilesChanged;

    /**
     * Fraction of PRs that were merged vs closed without merging.
     * Range: 0.0 – 1.0.
     */
    private Double mergeRate;

    private Integer openPrCount;
    private Integer closedPrCount;
    private Integer mergedPrCount;
    private Integer contributorCount;
    private Integer totalCommits;

    /**
     * Composite repository health score (0.0 – 1.0).
     * Higher is healthier.
     */
    private Double healthScore;

    private LocalDateTime analysedAt;
}
