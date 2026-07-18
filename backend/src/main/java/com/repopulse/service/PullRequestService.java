package com.repopulse.service;

import com.repopulse.dto.response.PullRequestResponse;

import java.util.List;

public interface PullRequestService {

    List<PullRequestResponse> getPullRequestsByRepository(Long repositoryId);

    PullRequestResponse getPullRequestById(Long pullRequestId);

    List<PullRequestResponse> getPullRequestsByState(Long repositoryId, String state);
}
