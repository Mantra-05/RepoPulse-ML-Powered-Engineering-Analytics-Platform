package com.repopulse.controller;

import com.repopulse.dto.request.AddRepositoryRequest;
import com.repopulse.dto.response.ApiResponse;
import com.repopulse.dto.response.RepositoryResponse;
import com.repopulse.entity.User;
import com.repopulse.service.RepositoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * CRUD operations for GitHub repositories tracked by the authenticated user.
 */
@RestController
@RequestMapping("/api/v1/repositories")
@RequiredArgsConstructor
public class RepositoryController {

    private final RepositoryService repositoryService;

    /** POST /api/v1/repositories */
    @PostMapping
    public ResponseEntity<ApiResponse<RepositoryResponse>> addRepository(
            @Valid @RequestBody AddRepositoryRequest request,
            @AuthenticationPrincipal User currentUser) {

        RepositoryResponse response = repositoryService.addRepository(
                request.getFullName(), currentUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Repository added successfully", response));
    }

    /** GET /api/v1/repositories */
    @GetMapping
    public ResponseEntity<ApiResponse<List<RepositoryResponse>>> getMyRepositories(
            @AuthenticationPrincipal User currentUser) {

        List<RepositoryResponse> repos = repositoryService.getUserRepositories(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(repos));
    }

    /** GET /api/v1/repositories/{id} */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RepositoryResponse>> getRepository(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {

        RepositoryResponse response = repositoryService.getRepositoryById(id, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /** DELETE /api/v1/repositories/{id} */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteRepository(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {

        repositoryService.deleteRepository(id, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("Repository deleted", null));
    }

    /** POST /api/v1/repositories/{id}/sync */
    @PostMapping("/{id}/sync")
    public ResponseEntity<ApiResponse<RepositoryResponse>> syncRepository(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {

        RepositoryResponse response = repositoryService.syncRepository(id, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("Repository sync triggered", response));
    }
}
