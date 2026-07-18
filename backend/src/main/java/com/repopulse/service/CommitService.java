package com.repopulse.service;

import com.repopulse.dto.response.CommitResponse;

import java.util.List;

public interface CommitService {

    List<CommitResponse> getCommitsByRepository(Long repositoryId);

    CommitResponse getCommitBySha(String sha);
}
