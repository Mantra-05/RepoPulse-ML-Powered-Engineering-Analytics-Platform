package com.repopulse.mapper;

import com.repopulse.dto.response.RepositoryResponse;
import com.repopulse.entity.Repository;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RepositoryMapper {

    @Mapping(target = "privateRepo", source = "privateRepo")
    RepositoryResponse toResponse(Repository repository);
}
