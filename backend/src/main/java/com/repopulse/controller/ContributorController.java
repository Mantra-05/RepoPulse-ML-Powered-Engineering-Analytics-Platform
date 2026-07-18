package com.repopulse.controller;

import com.repopulse.dto.response.ApiResponse;
import com.repopulse.dto.response.ContributorResponse;
import com.repopulse.service.ContributorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Endpoints for querying contributors within a repository.
 */
@RestController
@RequestMapping("/api/v1/repositories/{repositoryId}/contributors")
@RequiredArgsConstructor
public class ContributorController {

    private final ContributorService contributorService;

    /** GET /api/v1/repositories/{repositoryId}/contributors */
    @GetMapping
    public ResponseEntity<ApiResponse<List<ContributorResponse>>> getAll(
            @PathVariable Long repositoryId) {
        return ResponseEntity.ok(ApiResponse.success(
                contributorService.getContributorsByRepository(repositoryId)));
    }

    /** GET /api/v1/repositories/{repositoryId}/contributors/{login} */
    @GetMapping("/{login}")
    public ResponseEntity<ApiResponse<ContributorResponse>> getByLogin(
            @PathVariable Long repositoryId,
            @PathVariable String login) {
        return ResponseEntity.ok(ApiResponse.success(
                contributorService.getContributorByLogin(repositoryId, login)));
    }
}
