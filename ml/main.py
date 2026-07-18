"""
Phase 8 – FastAPI Prediction Service
======================================
Loads the trained XGBoost model and exposes:
  POST /predict  – returns risk_score, risk_level, priority, estimated_review_time, repository_health

Usage:
  uvicorn ml.main:app --host 0.0.0.0 --port 8001 --reload
"""

import os
import math
from contextlib import asynccontextmanager

import joblib
import numpy as np
from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware

from ml.schemas import PredictionRequest, PredictionResponse

# ── Model loading ─────────────────────────────────────────────────────────────
BASE_DIR    = os.path.dirname(os.path.abspath(__file__))
MODEL_PATH  = os.path.join(BASE_DIR, "model.pkl")
SCALER_PATH = os.path.join(BASE_DIR, "scaler.pkl")

model  = None
scaler = None


@asynccontextmanager
async def lifespan(app: FastAPI):
    """Load model and scaler at startup."""
    global model, scaler
    if not os.path.exists(MODEL_PATH):
        raise RuntimeError(
            f"model.pkl not found at {MODEL_PATH}. "
            "Run: python ml/generate_dataset.py && python ml/preprocess.py && python ml/train.py"
        )
    model  = joblib.load(MODEL_PATH)
    scaler = joblib.load(SCALER_PATH) if os.path.exists(SCALER_PATH) else None
    print(f"✅  Model loaded from {MODEL_PATH}")
    yield
    # Shutdown
    model  = None
    scaler = None


# ── App ───────────────────────────────────────────────────────────────────────
app = FastAPI(
    title="RepoPulse ML Prediction Service",
    description="XGBoost-based PR risk classification API",
    version="1.0.0",
    lifespan=lifespan,
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["http://localhost:8080", "http://localhost:3000", "http://localhost:5173"],
    allow_methods=["*"],
    allow_headers=["*"],
)

LABEL_MAP    = {0: "LOW", 1: "MEDIUM", 2: "HIGH"}
PRIORITY_MAP = {0: "P4_LOW", 1: "P3_MEDIUM", 2: "P1_CRITICAL"}


def _priority(risk_level: str, risk_score: float) -> str:
    if risk_score >= 0.80:  return "P1_CRITICAL"
    if risk_score >= 0.60:  return "P2_HIGH"
    if risk_score >= 0.35:  return "P3_MEDIUM"
    return "P4_LOW"


def _estimate_review_hours(req: PredictionRequest, risk_score: float) -> float:
    """Heuristic review-time estimate (hours) based on features and risk score."""
    base = req.days_open * 24 * 0.5
    size_factor = (req.lines_added + req.lines_deleted) / 200
    review_hours = base + size_factor + (risk_score * 8)
    return round(max(1.0, review_hours), 2)


def _repository_health(risk_score: float, author_experience: int) -> float:
    """Heuristic health proxy (0–1) – higher is healthier."""
    experience_score = math.log1p(author_experience) / math.log1p(500)
    health = (1.0 - risk_score) * 0.6 + experience_score * 0.4
    return round(min(1.0, max(0.0, health)), 4)


# ── Endpoints ─────────────────────────────────────────────────────────────────

@app.get("/health")
async def health():
    return {"status": "ok", "model_loaded": model is not None}


@app.post("/predict", response_model=PredictionResponse)
async def predict(request: PredictionRequest):
    if model is None:
        raise HTTPException(status_code=503, detail="Model not loaded")

    # Build feature vector
    features = np.array([[
        request.lines_added,
        request.lines_deleted,
        request.files_changed,
        request.commits,
        request.comments,
        request.author_experience,
        request.days_open,
    ]], dtype=float)

    # Apply same scaler used during training
    if scaler is not None:
        features = scaler.transform(features)

    # Predict class probabilities
    proba    = model.predict_proba(features)[0]   # [P(Low), P(Med), P(High)]
    class_id = int(np.argmax(proba))
    risk_score  = float(proba[2])                 # probability of HIGH class → risk score
    risk_level  = LABEL_MAP[class_id]
    priority    = _priority(risk_level, risk_score)

    review_time   = _estimate_review_hours(request, risk_score)
    repo_health   = _repository_health(risk_score, request.author_experience)

    return PredictionResponse(
        risk_score=round(risk_score, 4),
        risk_level=risk_level,
        priority=priority,
        estimated_review_time=review_time,
        repository_health=repo_health,
        probabilities={
            "low":    round(float(proba[0]), 4),
            "medium": round(float(proba[1]), 4),
            "high":   round(float(proba[2]), 4),
        },
    )
