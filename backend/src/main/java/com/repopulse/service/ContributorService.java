package com.repopulse.service;

import com.repopulse.dto.response.ContributorResponse;

import java.util.List;

public interface ContributorService {

    List<ContributorResponse> getContributorsByRepository(Long repositoryId);

    ContributorResponse getContributorByLogin(Long repositoryId, String login);
}
