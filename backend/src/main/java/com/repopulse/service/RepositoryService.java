package com.repopulse.service;

import com.repopulse.dto.response.RepositoryResponse;

import java.util.List;

public interface RepositoryService {

    RepositoryResponse addRepository(String fullName, Long userId);

    List<RepositoryResponse> getUserRepositories(Long userId);

    RepositoryResponse getRepositoryById(Long repositoryId, Long userId);

    void deleteRepository(Long repositoryId, Long userId);

    RepositoryResponse syncRepository(Long repositoryId, Long userId);
}
