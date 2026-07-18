package com.repopulse.service;

import com.repopulse.dto.response.PredictionResponse;

import java.util.List;

public interface PredictionService {

    /**
     * Call the FastAPI ML service for the given PR,
     * persist the result, and return the response DTO.
     *
     * @param pullRequestId internal DB id of the pull request
     * @return stored prediction
     */
    PredictionResponse requestPrediction(Long pullRequestId);

    /**
     * Return all predictions associated with a repository.
     *
     * @param repositoryId internal DB id of the repository
     */
    List<PredictionResponse> getPredictionsByRepository(Long repositoryId);

    /**
     * Return the prediction for a specific pull request.
     *
     * @param pullRequestId internal DB id
     */
    PredictionResponse getPredictionByPullRequest(Long pullRequestId);
}
