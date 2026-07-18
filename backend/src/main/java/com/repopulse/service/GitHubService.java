package com.repopulse.service;

import com.repopulse.dto.github.*;

import java.util.List;

/**
 * Abstraction over the GitHub REST API v3.
 * All methods are synchronous and handle pagination internally.
 */
public interface GitHubService {

    /** Fetch basic repository metadata. */
    GitHubRepoDto fetchRepository(String owner, String repo);

    /** Fetch all pull requests (open + closed + merged), paginated. */
    List<GitHubPullRequestDto> fetchPullRequests(String owner, String repo);

    /** Fetch full detail for a single PR (includes additions/deletions/changedFiles). */
    GitHubPullRequestDto fetchPullRequestDetail(String owner, String repo, Integer prNumber);

    /** Fetch all commits, paginated. */
    List<GitHubCommitDto> fetchCommits(String owner, String repo);

    /** Fetch diff stats for a single commit SHA. */
    GitHubCommitDetailDto fetchCommitDetail(String owner, String repo, String sha);

    /** Fetch all contributors, paginated. */
    List<GitHubContributorDto> fetchContributors(String owner, String repo);

    /** How many commits the given login has in this repo (for author_experience feature). */
    int fetchContributorCommitCount(String owner, String repo, String login);
}
