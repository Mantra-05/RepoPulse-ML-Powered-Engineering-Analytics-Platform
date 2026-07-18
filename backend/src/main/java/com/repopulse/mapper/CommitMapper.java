package com.repopulse.mapper;

import com.repopulse.dto.response.CommitResponse;
import com.repopulse.entity.Commit;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CommitMapper {

    CommitResponse toResponse(Commit commit);
}
