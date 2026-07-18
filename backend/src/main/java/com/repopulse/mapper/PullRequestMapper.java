package com.repopulse.mapper;

import com.repopulse.dto.response.PullRequestResponse;
import com.repopulse.entity.PullRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PullRequestMapper {

    @Mapping(target = "state", expression = "java(pullRequest.getState().name())")
    PullRequestResponse toResponse(PullRequest pullRequest);
}
