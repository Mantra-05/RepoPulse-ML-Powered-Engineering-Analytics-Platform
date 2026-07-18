package com.repopulse.controller;

import com.repopulse.dto.response.ApiResponse;
import com.repopulse.dto.response.PredictionResponse;
import com.repopulse.service.PredictionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST endpoints for ML predictions.
 *
 * <pre>
 * GET  /api/v1/predictions/{repositoryId}             – all predictions for a repo
 * GET  /api/v1/predictions/pull-requests/{prId}       – prediction for a specific PR
 * POST /api/v1/predictions/pull-requests/{prId}       – trigger fresh prediction for a PR
 * </pre>
 */
@RestController
@RequestMapping("/api/v1/predictions")
@RequiredArgsConstructor
public class PredictionController {

    private final PredictionService predictionService;

    /**
     * Return all stored predictions for a repository.
     * {@code GET /api/v1/predictions/{repositoryId}}
     */
    @GetMapping("/{repositoryId}")
    public ResponseEntity<ApiResponse<List<PredictionResponse>>> getByRepository(
            @PathVariable Long repositoryId) {

        return ResponseEntity.ok(ApiResponse.success(
                predictionService.getPredictionsByRepository(repositoryId)));
    }

    /**
     * Return the stored prediction for a specific PR.
     * {@code GET /api/v1/predictions/pull-requests/{pullRequestId}}
     */
    @GetMapping("/pull-requests/{pullRequestId}")
    public ResponseEntity<ApiResponse<PredictionResponse>> getByPullRequest(
            @PathVariable Long pullRequestId) {

        return ResponseEntity.ok(ApiResponse.success(
                predictionService.getPredictionByPullRequest(pullRequestId)));
    }

    /**
     * Trigger (or re-trigger) an ML prediction for a pull request.
     * Calls the FastAPI service and persists the result.
     * {@code POST /api/v1/predictions/pull-requests/{pullRequestId}}
     */
    @PostMapping("/pull-requests/{pullRequestId}")
    public ResponseEntity<ApiResponse<PredictionResponse>> requestPrediction(
            @PathVariable Long pullRequestId) {

        PredictionResponse response = predictionService.requestPrediction(pullRequestId);
        return ResponseEntity.ok(ApiResponse.success("Prediction generated", response));
    }
}
