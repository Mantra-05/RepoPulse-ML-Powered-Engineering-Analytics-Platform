package com.repopulse.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Response received from the FastAPI ML service {@code POST /predict}.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MlPredictionResponse {

    @JsonProperty("risk_score")
    private Double riskScore;

    /** LOW / MEDIUM / HIGH */
    @JsonProperty("risk_level")
    private String riskLevel;

    /** P1_CRITICAL / P2_HIGH / P3_MEDIUM / P4_LOW */
    private String priority;

    @JsonProperty("estimated_review_time")
    private Double estimatedReviewTime;

    @JsonProperty("repository_health")
    private Double repositoryHealth;
}
