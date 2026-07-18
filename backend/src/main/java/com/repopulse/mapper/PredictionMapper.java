package com.repopulse.mapper;

import com.repopulse.dto.response.PredictionResponse;
import com.repopulse.entity.Prediction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PredictionMapper {

    @Mapping(target = "predictionType", expression = "java(prediction.getPredictionType().name())")
    PredictionResponse toResponse(Prediction prediction);
}
