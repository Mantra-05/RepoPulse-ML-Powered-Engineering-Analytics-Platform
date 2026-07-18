package com.repopulse.repository;

import com.repopulse.entity.PullRequest;
import com.repopulse.entity.PullRequest.PrState;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PullRequestRepository extends JpaRepository<PullRequest, Long> {

    List<PullRequest> findAllByRepositoryId(Long repositoryId);

    Page<PullRequest> findAllByRepositoryId(Long repositoryId, Pageable pageable);

    List<PullRequest> findAllByRepositoryIdAndState(Long repositoryId, PrState state);

    Page<PullRequest> findAllByRepositoryIdAndState(Long repositoryId, PrState state, Pageable pageable);

    Optional<PullRequest> findByRepositoryIdAndGithubPrNumber(Long repositoryId, Integer githubPrNumber);

    long countByRepositoryIdAndState(Long repositoryId, PrState state);

    // ── Metrics JPQL queries ──────────────────────────────────────────────────

    @Query("SELECT COALESCE(AVG(p.additions + p.deletions), 0.0) FROM PullRequest p " +
           "WHERE p.repository.id = :repoId")
    Double avgPrSize(@Param("repoId") Long repositoryId);

    @Query("SELECT COALESCE(AVG(p.commitsCount), 0.0) FROM PullRequest p " +
           "WHERE p.repository.id = :repoId AND p.commitsCount IS NOT NULL")
    Double avgCommitsPerPr(@Param("repoId") Long repositoryId);

    @Query("SELECT COALESCE(AVG(p.changedFiles), 0.0) FROM PullRequest p " +
           "WHERE p.repository.id = :repoId AND p.changedFiles IS NOT NULL")
    Double avgFilesChanged(@Param("repoId") Long repositoryId);

    /**
     * Average review time in hours for closed/merged PRs.
     * Uses TIMESTAMPDIFF not available in JPQL — relies on Java-side computation
     * via {@link #findClosedOrMergedByRepository}.
     */
    @Query("SELECT p FROM PullRequest p " +
           "WHERE p.repository.id = :repoId " +
           "  AND p.state IN ('CLOSED', 'MERGED') " +
           "  AND p.openedAt IS NOT NULL " +
           "  AND (p.closedAt IS NOT NULL OR p.mergedAt IS NOT NULL)")
    List<PullRequest> findClosedOrMergedByRepository(@Param("repoId") Long repositoryId);

    /** Search PRs by title (case-insensitive). */
    @Query("SELECT p FROM PullRequest p " +
           "WHERE p.repository.id = :repoId " +
           "  AND LOWER(p.title) LIKE LOWER(CONCAT('%', :q, '%'))")
    Page<PullRequest> searchByTitle(@Param("repoId") Long repositoryId,
                                    @Param("q") String query,
                                    Pageable pageable);
}
