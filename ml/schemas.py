"""Pydantic models (schemas) for the RepoPulse ML prediction service."""

from pydantic import BaseModel, Field
from typing import Dict, Optional


class PredictionRequest(BaseModel):
    """Seven engineered features fed to the XGBoost model."""

    lines_added: int       = Field(ge=0, description="Lines added in the PR")
    lines_deleted: int     = Field(ge=0, description="Lines deleted in the PR")
    files_changed: int     = Field(ge=0, description="Number of files changed")
    commits: int           = Field(ge=1, description="Number of commits in the PR")
    comments: int          = Field(ge=0, description="Total review + issue comments")
    author_experience: int = Field(ge=0, description="Author commit count to this repo")
    days_open: float       = Field(ge=0.0, description="Days the PR has been open")

    class Config:
        json_schema_extra = {
            "example": {
                "lines_added": 120,
                "lines_deleted": 45,
                "files_changed": 8,
                "commits": 4,
                "comments": 6,
                "author_experience": 35,
                "days_open": 2.5,
            }
        }


class PredictionResponse(BaseModel):
    """Output from the XGBoost model + heuristic post-processing."""

    risk_score: float              = Field(description="0.0–1.0, higher = riskier")
    risk_level: str                = Field(description="LOW | MEDIUM | HIGH")
    priority: str                  = Field(description="P1_CRITICAL | P2_HIGH | P3_MEDIUM | P4_LOW")
    estimated_review_time: float   = Field(description="Estimated review time in hours")
    repository_health: float       = Field(description="0.0–1.0 health score")
    probabilities: Optional[Dict[str, float]] = Field(
        default=None,
        description="Class probabilities {low, medium, high}"
    )
