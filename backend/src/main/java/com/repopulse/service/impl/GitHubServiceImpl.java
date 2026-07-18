package com.repopulse.service.impl;

import com.repopulse.dto.github.*;
import com.repopulse.exception.ResourceNotFoundException;
import com.repopulse.service.GitHubService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * GitHub REST API v3 client.
 *
 * <p>Uses {@code RestTemplate} with the {@code gitHubRestTemplate} bean
 * (which injects the optional Bearer token). All paginated endpoints are
 * drained automatically using the {@code page} query parameter.
 */
@Service
@Slf4j
public class GitHubServiceImpl implements GitHubService {

    private static final String BASE = "https://api.github.com";
    private static final int PAGE_SIZE = 100;

    private final RestTemplate gitHubRestTemplate;

    @Value("${github.api.max-pages:10}")
    private int maxPages;

    public GitHubServiceImpl(@Qualifier("gitHubRestTemplate") RestTemplate gitHubRestTemplate) {
        this.gitHubRestTemplate = gitHubRestTemplate;
    }

    // ── Repository ────────────────────────────────────────────────────────────

    @Override
    public GitHubRepoDto fetchRepository(String owner, String repo) {
        String url = BASE + "/repos/{owner}/{repo}";
        try {
            ResponseEntity<GitHubRepoDto> resp =
                    gitHubRestTemplate.getForEntity(url, GitHubRepoDto.class, owner, repo);
            return resp.getBody();
        } catch (HttpClientErrorException.NotFound ex) {
            throw new ResourceNotFoundException("GitHub repository not found: " + owner + "/" + repo);
        } catch (HttpClientErrorException ex) {
            log.error("GitHub API error for {}/{}: {} {}", owner, repo,
                    ex.getStatusCode(), ex.getMessage());
            throw new RuntimeException("GitHub API error: " + ex.getMessage(), ex);
        }
    }

    // ── Pull Requests ─────────────────────────────────────────────────────────

    @Override
    public List<GitHubPullRequestDto> fetchPullRequests(String owner, String repo) {
        List<GitHubPullRequestDto> all = new ArrayList<>();
        for (String state : List.of("open", "closed")) {
            all.addAll(fetchPaginated(
                    BASE + "/repos/{owner}/{repo}/pulls",
                    new ParameterizedTypeReference<>() {},
                    owner, repo,
                    "state", state));
        }
        log.info("Fetched {} PRs for {}/{}", all.size(), owner, repo);
        return all;
    }

    @Override
    public GitHubPullRequestDto fetchPullRequestDetail(String owner, String repo, Integer prNumber) {
        String url = BASE + "/repos/{owner}/{repo}/pulls/{pull_number}";
        try {
            ResponseEntity<GitHubPullRequestDto> resp =
                    gitHubRestTemplate.getForEntity(url, GitHubPullRequestDto.class,
                            owner, repo, prNumber);
            return resp.getBody();
        } catch (HttpClientErrorException.NotFound ex) {
            throw new ResourceNotFoundException("PR #" + prNumber + " not found in " + owner + "/" + repo);
        }
    }

    // ── Commits ───────────────────────────────────────────────────────────────

    @Override
    public List<GitHubCommitDto> fetchCommits(String owner, String repo) {
        List<GitHubCommitDto> commits = fetchPaginated(
                BASE + "/repos/{owner}/{repo}/commits",
                new ParameterizedTypeReference<>() {},
                owner, repo);
        log.info("Fetched {} commits for {}/{}", commits.size(), owner, repo);
        return commits;
    }

    @Override
    public GitHubCommitDetailDto fetchCommitDetail(String owner, String repo, String sha) {
        String url = BASE + "/repos/{owner}/{repo}/commits/{sha}";
        try {
            return gitHubRestTemplate
                    .getForEntity(url, GitHubCommitDetailDto.class, owner, repo, sha)
                    .getBody();
        } catch (HttpClientErrorException ex) {
            log.warn("Could not fetch commit detail for sha={}: {}", sha, ex.getMessage());
            return null;
        }
    }

    // ── Contributors ──────────────────────────────────────────────────────────

    @Override
    public List<GitHubContributorDto> fetchContributors(String owner, String repo) {
        List<GitHubContributorDto> contributors = fetchPaginated(
                BASE + "/repos/{owner}/{repo}/contributors",
                new ParameterizedTypeReference<>() {},
                owner, repo);
        log.info("Fetched {} contributors for {}/{}", contributors.size(), owner, repo);
        return contributors;
    }

    @Override
    public int fetchContributorCommitCount(String owner, String repo, String login) {
        // Use the contributor stats endpoint – returns commit count directly
        List<GitHubContributorDto> contributors = fetchContributors(owner, repo);
        return contributors.stream()
                .filter(c -> login.equalsIgnoreCase(c.getLogin()))
                .mapToInt(GitHubContributorDto::getContributions)
                .findFirst()
                .orElse(0);
    }

    // ── Pagination helper ─────────────────────────────────────────────────────

    /**
     * Drains a paginated GitHub endpoint by incrementing the {@code page} parameter
     * until an empty page is returned or {@code maxPages} is reached.
     *
     * @param urlTemplate URL with path variable placeholders ({owner}, {repo})
     * @param type        {@link ParameterizedTypeReference} for {@code List<T>}
     * @param pathVars    values for path variable placeholders
     * @param extraParams additional key-value query params (e.g. "state","open")
     */
    private <T> List<T> fetchPaginated(
            String urlTemplate,
            ParameterizedTypeReference<List<T>> type,
            String owner, String repo,
            String... extraParams) {

        List<T> result = new ArrayList<>();

        for (int page = 1; page <= maxPages; page++) {
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(
                    urlTemplate.replace("{owner}", owner).replace("{repo}", repo))
                    .queryParam("per_page", PAGE_SIZE)
                    .queryParam("page", page);

            // Inject any extra query params (key-value pairs)
            for (int i = 0; i < extraParams.length - 1; i += 2) {
                builder.queryParam(extraParams[i], extraParams[i + 1]);
            }

            try {
                ResponseEntity<List<T>> resp = gitHubRestTemplate.exchange(
                        builder.toUriString(),
                        HttpMethod.GET,
                        null,
                        type);

                List<T> page_data = resp.getBody();
                if (page_data == null || page_data.isEmpty()) break;

                result.addAll(page_data);
                if (page_data.size() < PAGE_SIZE) break; // last page

            } catch (HttpClientErrorException.Forbidden ex) {
                log.warn("GitHub rate limit hit at page {}: {}", page, ex.getMessage());
                break;
            } catch (HttpClientErrorException ex) {
                log.error("GitHub API error on page {}: {}", page, ex.getMessage());
                break;
            }
        }
        return result;
    }
}
