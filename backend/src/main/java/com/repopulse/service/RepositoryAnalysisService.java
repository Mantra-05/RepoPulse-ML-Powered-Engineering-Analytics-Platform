package com.repopulse.service;

import com.repopulse.dto.response.RepositoryAnalysisResponse;

public interface RepositoryAnalysisService {

    /**
     * Compute all engineering metrics for the given repository,
     * persist the result, and return the response DTO.
     *
     * @param repositoryId internal database ID of the repository
     * @return computed metrics
     */
    RepositoryAnalysisResponse analyzeRepository(Long repositoryId);

    /**
     * Return the most-recently stored analysis for a repository.
     * Triggers a fresh analysis if none exists yet.
     *
     * @param repositoryId internal database ID
     * @return latest analysis result
     */
    RepositoryAnalysisResponse getAnalysis(Long repositoryId);
}
