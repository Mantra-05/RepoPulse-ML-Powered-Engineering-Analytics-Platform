package com.repopulse.service.impl;

import com.repopulse.dto.github.*;
import com.repopulse.dto.response.RepositoryResponse;
import com.repopulse.entity.*;
import com.repopulse.entity.PullRequest.PrState;
import com.repopulse.exception.DuplicateResourceException;
import com.repopulse.exception.ResourceNotFoundException;
import com.repopulse.exception.UnauthorizedException;
import com.repopulse.mapper.RepositoryMapper;
import com.repopulse.repository.*;
import com.repopulse.service.GitHubService;
import com.repopulse.service.RepositoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RepositoryServiceImpl implements RepositoryService {

    private final RepositoryRepository   repositoryRepository;
    private final UserRepository         userRepository;
    private final PullRequestRepository  pullRequestRepository;
    private final CommitRepository       commitRepository;
    private final ContributorRepository  contributorRepository;
    private final RepositoryMapper       repositoryMapper;
    private final GitHubService          gitHubService;

    // ── Add ───────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public RepositoryResponse addRepository(String fullName, Long userId) {
        if (repositoryRepository.findByFullName(fullName).isPresent()) {
            throw new DuplicateResourceException("Repository already tracked: " + fullName);
        }

        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // Parse owner/repo from fullName
        String[] parts = fullName.split("/", 2);
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid repository name. Expected format: owner/repo");
        }
        String ghOwner = parts[0];
        String ghRepo  = parts[1];

        // Fetch from GitHub
        GitHubRepoDto ghData = gitHubService.fetchRepository(ghOwner, ghRepo);

        Repository repository = Repository.builder()
                .githubId(ghData.getId())
                .name(ghData.getName())
                .fullName(ghData.getFullName())
                .description(ghData.getDescription())
                .language(ghData.getLanguage())
                .privateRepo(ghData.isPrivateRepo())
                .defaultBranch(ghData.getDefaultBranch())
                .starsCount(ghData.getStargazersCount())
                .forksCount(ghData.getForksCount())
                .openIssuesCount(ghData.getOpenIssuesCount())
                .githubUrl(ghData.getHtmlUrl())
                .owner(owner)
                .lastSyncedAt(LocalDateTime.now())
                .build();

        Repository saved = repositoryRepository.save(repository);
        log.info("Repository added and synced: {} by userId={}", fullName, userId);

        // Async sync in background (fire-and-forget on the same thread for now)
        syncGitHubData(saved, ghOwner, ghRepo);

        return repositoryMapper.toResponse(saved);
    }

    // ── Read ──────────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<RepositoryResponse> getUserRepositories(Long userId) {
        return repositoryRepository.findAllByOwnerId(userId)
                .stream()
                .map(repositoryMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public RepositoryResponse getRepositoryById(Long repositoryId, Long userId) {
        return repositoryMapper.toResponse(findAndVerifyOwner(repositoryId, userId));
    }

    // ── Delete ────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public void deleteRepository(Long repositoryId, Long userId) {
        Repository repository = findAndVerifyOwner(repositoryId, userId);
        repositoryRepository.delete(repository);
        log.info("Repository {} deleted by userId={}", repositoryId, userId);
    }

    // ── Sync ──────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public RepositoryResponse syncRepository(Long repositoryId, Long userId) {
        Repository repository = findAndVerifyOwner(repositoryId, userId);

        String[] parts  = repository.getFullName().split("/", 2);
        String ghOwner  = parts[0];
        String ghRepo   = parts[1];

        // Refresh basic metadata
        GitHubRepoDto ghData = gitHubService.fetchRepository(ghOwner, ghRepo);
        repository.setStarsCount(ghData.getStargazersCount());
        repository.setForksCount(ghData.getForksCount());
        repository.setOpenIssuesCount(ghData.getOpenIssuesCount());
        repository.setDescription(ghData.getDescription());
        repository.setLastSyncedAt(LocalDateTime.now());
        repositoryRepository.save(repository);

        syncGitHubData(repository, ghOwner, ghRepo);

        log.info("Repository {} synced by userId={}", repositoryId, userId);
        return repositoryMapper.toResponse(repository);
    }

    // ── GitHub data sync ──────────────────────────────────────────────────────

    /**
     * Fetches and persists contributors, commits, and pull requests from GitHub.
     * Skips entities that already exist (idempotent).
     */
    private void syncGitHubData(Repository repository, String owner, String repo) {
        syncContributors(repository, owner, repo);
        syncCommits(repository, owner, repo);
        syncPullRequests(repository, owner, repo);
    }

    private void syncContributors(Repository repository, String owner, String repo) {
        List<GitHubContributorDto> ghContributors = gitHubService.fetchContributors(owner, repo);
        for (GitHubContributorDto c : ghContributors) {
            if (!contributorRepository.existsByLoginAndRepositoryId(c.getLogin(), repository.getId())) {
                contributorRepository.save(Contributor.builder()
                        .login(c.getLogin())
                        .avatarUrl(c.getAvatarUrl())
                        .githubUrl(c.getHtmlUrl())
                        .contributionsCount(c.getContributions())
                        .repository(repository)
                        .build());
            }
        }
        log.debug("Synced {} contributors for {}", ghContributors.size(), repository.getFullName());
    }

    private void syncCommits(Repository repository, String owner, String repo) {
        List<GitHubCommitDto> ghCommits = gitHubService.fetchCommits(owner, repo);
        for (GitHubCommitDto c : ghCommits) {
            if (commitRepository.existsBySha(c.getSha())) continue;

            GitHubCommitDto.GitHubCommitDetailDto detail = c.getCommit();
            Commit commit = Commit.builder()
                    .sha(c.getSha())
                    .message(detail != null ? detail.getMessage() : null)
                    .authorLogin(c.getAuthor() != null ? c.getAuthor().getLogin() : null)
                    .authorName(detail != null && detail.getAuthor() != null
                            ? detail.getAuthor().getName() : null)
                    .authorEmail(detail != null && detail.getAuthor() != null
                            ? detail.getAuthor().getEmail() : null)
                    .committedAt(detail != null && detail.getAuthor() != null
                            ? detail.getAuthor().getDate().toLocalDateTime() : null)
                    .repository(repository)
                    .build();
            commitRepository.save(commit);
        }
        log.debug("Synced {} commits for {}", ghCommits.size(), repository.getFullName());
    }

    private void syncPullRequests(Repository repository, String owner, String repo) {
        List<GitHubPullRequestDto> ghPrs = gitHubService.fetchPullRequests(owner, repo);
        for (GitHubPullRequestDto pr : ghPrs) {
            if (pullRequestRepository
                    .findByRepositoryIdAndGithubPrNumber(repository.getId(), pr.getNumber())
                    .isPresent()) {
                continue;  // Already synced
            }

            // Fetch detailed PR (has changedFiles, additions, deletions)
            GitHubPullRequestDto detail = gitHubService
                    .fetchPullRequestDetail(owner, repo, pr.getNumber());

            PrState state = resolvePrState(pr);
            String authorLogin = pr.getUser() != null ? pr.getUser().getLogin() : null;
            int authorContributions = authorLogin != null
                    ? gitHubService.fetchContributorCommitCount(owner, repo, authorLogin) : 0;

            PullRequest entity = PullRequest.builder()
                    .githubPrNumber(pr.getNumber())
                    .title(pr.getTitle())
                    .body(pr.getBody())
                    .state(state)
                    .authorLogin(authorLogin)
                    .authorContributions(authorContributions)
                    .baseBranch(pr.getBase() != null ? pr.getBase().getRef() : null)
                    .headBranch(pr.getHead() != null ? pr.getHead().getRef() : null)
                    .changedFiles(detail != null ? detail.getChangedFiles() : null)
                    .additions(detail != null ? detail.getAdditions() : null)
                    .deletions(detail != null ? detail.getDeletions() : null)
                    .reviewComments(pr.getReviewComments())
                    .comments(pr.getComments())
                    .commitsCount(pr.getCommits())
                    .openedAt(pr.getCreatedAt() != null ? pr.getCreatedAt().toLocalDateTime() : null)
                    .closedAt(pr.getClosedAt() != null ? pr.getClosedAt().toLocalDateTime() : null)
                    .mergedAt(pr.getMergedAt() != null ? pr.getMergedAt().toLocalDateTime() : null)
                    .repository(repository)
                    .build();

            pullRequestRepository.save(entity);
        }
        log.debug("Synced {} PRs for {}", ghPrs.size(), repository.getFullName());
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private PrState resolvePrState(GitHubPullRequestDto pr) {
        if (pr.getMergedAt() != null) return PrState.MERGED;
        if ("closed".equalsIgnoreCase(pr.getState())) return PrState.CLOSED;
        return PrState.OPEN;
    }

    private Repository findAndVerifyOwner(Long repositoryId, Long userId) {
        Repository repository = repositoryRepository.findById(repositoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Repository", "id", repositoryId));

        if (!repository.getOwner().getId().equals(userId)) {
            throw new UnauthorizedException("Access denied to repository: " + repositoryId);
        }
        return repository;
    }
}
