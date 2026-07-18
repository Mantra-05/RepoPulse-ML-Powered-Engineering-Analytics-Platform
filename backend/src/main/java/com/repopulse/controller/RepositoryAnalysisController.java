package com.repopulse.controller;

import com.repopulse.dto.response.ApiResponse;
import com.repopulse.dto.response.RepositoryAnalysisResponse;
import com.repopulse.service.RepositoryAnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Exposes repository engineering-metric analysis.
 *
 * <pre>
 * GET  /api/v1/repositories/{id}/analysis   – return latest (or trigger fresh) analysis
 * POST /api/v1/repositories/{id}/analysis   – force fresh analysis
 * </pre>
 */
@RestController
@RequestMapping("/api/v1/repositories/{repositoryId}/analysis")
@RequiredArgsConstructor
public class RepositoryAnalysisController {

    private final RepositoryAnalysisService analysisService;

    /**
     * Returns the latest cached analysis, or triggers a fresh one if none exists.
     * {@code GET /api/v1/repositories/{repositoryId}/analysis}
     */
    @GetMapping
    public ResponseEntity<ApiResponse<RepositoryAnalysisResponse>> getAnalysis(
            @PathVariable Long repositoryId) {

        RepositoryAnalysisResponse response = analysisService.getAnalysis(repositoryId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Forces a fresh metric recomputation regardless of cached state.
     * {@code POST /api/v1/repositories/{repositoryId}/analysis}
     */
    @PostMapping
    public ResponseEntity<ApiResponse<RepositoryAnalysisResponse>> runAnalysis(
            @PathVariable Long repositoryId) {

        RepositoryAnalysisResponse response = analysisService.analyzeRepository(repositoryId);
        return ResponseEntity.ok(ApiResponse.success("Analysis completed", response));
    }
}
