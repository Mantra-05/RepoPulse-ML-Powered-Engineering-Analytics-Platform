package com.repopulse.service.impl;

import com.repopulse.dto.response.RepositoryAnalysisResponse;
import com.repopulse.entity.PullRequest;
import com.repopulse.entity.Repository;
import com.repopulse.entity.RepositoryAnalysis;
import com.repopulse.exception.ResourceNotFoundException;
import com.repopulse.repository.*;
import com.repopulse.service.RepositoryAnalysisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.OptionalDouble;

@Service
@RequiredArgsConstructor
@Slf4j
public class RepositoryAnalysisServiceImpl implements RepositoryAnalysisService {

    private final RepositoryRepository         repositoryRepository;
    private final PullRequestRepository        pullRequestRepository;
    private final CommitRepository             commitRepository;
    private final ContributorRepository        contributorRepository;
    private final RepositoryAnalysisRepository analysisRepository;

    // ── Analyze ───────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public RepositoryAnalysisResponse analyzeRepository(Long repositoryId) {
        Repository repo = repositoryRepository.findById(repositoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Repository", "id", repositoryId));

        // ── Count PRs by state ────────────────────────────────────────────────
        int openCount   = (int) pullRequestRepository
                .countByRepositoryIdAndState(repositoryId, PullRequest.PrState.OPEN);
        int closedCount = (int) pullRequestRepository
                .countByRepositoryIdAndState(repositoryId, PullRequest.PrState.CLOSED);
        int mergedCount = (int) pullRequestRepository
                .countByRepositoryIdAndState(repositoryId, PullRequest.PrState.MERGED);

        // ── Merge rate ────────────────────────────────────────────────────────
        double mergeRate = 0.0;
        int closedOrMerged = closedCount + mergedCount;
        if (closedOrMerged > 0) {
            mergeRate = (double) mergedCount / closedOrMerged;
        }

        // ── Average PR size (JPQL) ────────────────────────────────────────────
        Double avgPrSize         = nullSafe(pullRequestRepository.avgPrSize(repositoryId));
        Double avgCommitsPerPr   = nullSafe(pullRequestRepository.avgCommitsPerPr(repositoryId));
        Double avgFilesChanged   = nullSafe(pullRequestRepository.avgFilesChanged(repositoryId));

        // ── Average review time (Java-side: needs timestamp arithmetic) ───────
        List<PullRequest> donePrs = pullRequestRepository.findClosedOrMergedByRepository(repositoryId);
        OptionalDouble avgReviewTime = donePrs.stream()
                .mapToDouble(pr -> {
                    LocalDateTime end = pr.getMergedAt() != null ? pr.getMergedAt() : pr.getClosedAt();
                    return ChronoUnit.MINUTES.between(pr.getOpenedAt(), end) / 60.0;
                })
                .filter(h -> h >= 0)
                .average();
        Double avgReviewTimeHours = avgReviewTime.isPresent() ? avgReviewTime.getAsDouble() : 0.0;

        // ── Contributor count ─────────────────────────────────────────────────
        int contributorCount = (int) contributorRepository.countByRepositoryId(repositoryId);

        // ── Total commits ─────────────────────────────────────────────────────
        int totalCommits = (int) commitRepository.countByRepositoryId(repositoryId);

        // ── Health score (composite) ──────────────────────────────────────────
        double healthScore = computeHealthScore(mergeRate, avgReviewTimeHours, contributorCount, totalCommits);

        // ── Persist / update ──────────────────────────────────────────────────
        RepositoryAnalysis analysis = analysisRepository.findByRepositoryId(repositoryId)
                .orElse(RepositoryAnalysis.builder().repository(repo).build());

        analysis.setAvgPrSize(avgPrSize);
        analysis.setAvgReviewTimeHours(avgReviewTimeHours);
        analysis.setAvgCommitsPerPr(avgCommitsPerPr);
        analysis.setAvgFilesChanged(avgFilesChanged);
        analysis.setMergeRate(mergeRate);
        analysis.setOpenPrCount(openCount);
        analysis.setClosedPrCount(closedCount);
        analysis.setMergedPrCount(mergedCount);
        analysis.setContributorCount(contributorCount);
        analysis.setTotalCommits(totalCommits);
        analysis.setHealthScore(healthScore);
        analysis.setAnalysedAt(LocalDateTime.now());

        analysisRepository.save(analysis);
        log.info("Analysis complete for repository={} healthScore={}", repositoryId, healthScore);

        return toResponse(repo, analysis);
    }

    // ── Get ───────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public RepositoryAnalysisResponse getAnalysis(Long repositoryId) {
        return analysisRepository.findByRepositoryId(repositoryId)
                .map(a -> {
                    Repository repo = repositoryRepository.findById(repositoryId).orElseThrow();
                    return toResponse(repo, a);
                })
                .orElseGet(() -> analyzeRepository(repositoryId));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /**
     * Composite health score formula:
     * <ul>
     *   <li>Merge rate contributes 40 %</li>
     *   <li>Review time penalty (long reviews = unhealthy) contributes 30 %</li>
     *   <li>Activity (commits + contributors) contributes 30 %</li>
     * </ul>
     */
    private double computeHealthScore(double mergeRate, double avgReviewTimeHours,
                                      int contributorCount, int totalCommits) {
        // Merge rate: 0.0–1.0, already normalised
        double mergeScore = mergeRate;

        // Review time: penalise > 72 hours; cap at 0.0 if > 168 hours
        double reviewScore = avgReviewTimeHours == 0 ? 0.8
                : Math.max(0.0, 1.0 - (avgReviewTimeHours / 168.0));

        // Activity score: saturates at 100 commits and 10 contributors
        double activityScore = Math.min(1.0, totalCommits / 100.0) * 0.5
                + Math.min(1.0, contributorCount / 10.0) * 0.5;

        double score = (mergeScore * 0.40) + (reviewScore * 0.30) + (activityScore * 0.30);
        return Math.min(1.0, Math.max(0.0, score));
    }

    private RepositoryAnalysisResponse toResponse(Repository repo, RepositoryAnalysis a) {
        return RepositoryAnalysisResponse.builder()
                .repositoryId(repo.getId())
                .repositoryFullName(repo.getFullName())
                .avgPrSize(a.getAvgPrSize())
                .avgReviewTimeHours(a.getAvgReviewTimeHours())
                .avgCommitsPerPr(a.getAvgCommitsPerPr())
                .avgFilesChanged(a.getAvgFilesChanged())
                .mergeRate(a.getMergeRate())
                .openPrCount(a.getOpenPrCount())
                .closedPrCount(a.getClosedPrCount())
                .mergedPrCount(a.getMergedPrCount())
                .contributorCount(a.getContributorCount())
                .totalCommits(a.getTotalCommits())
                .healthScore(a.getHealthScore())
                .analysedAt(a.getAnalysedAt())
                .build();
    }

    private double nullSafe(Double val) {
        return val != null ? val : 0.0;
    }
}
