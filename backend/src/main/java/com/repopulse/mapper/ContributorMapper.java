package com.repopulse.mapper;

import com.repopulse.dto.response.ContributorResponse;
import com.repopulse.entity.Contributor;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ContributorMapper {

    ContributorResponse toResponse(Contributor contributor);
}
