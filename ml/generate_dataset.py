"""
Generates ml/data/raw_dataset.csv – 600 synthetic PR records for training.
Run once: python ml/generate_dataset.py
"""
import os
import numpy as np
import pandas as pd

rng = np.random.default_rng(42)
N   = 600

lines_added      = rng.integers(0, 2000, N)
lines_deleted    = rng.integers(0, 800, N)
files_changed    = rng.integers(1, 60, N)
commits          = rng.integers(1, 40, N)
comments         = rng.integers(0, 80, N)
author_experience= rng.integers(0, 500, N)
days_open        = rng.exponential(scale=5, size=N).clip(0, 60)

# Deterministic risk_level based on a weighted score
raw_risk = (
    (lines_added / 2000) * 0.25 +
    (lines_deleted / 800) * 0.15 +
    (files_changed / 60)  * 0.25 +
    (1 / (author_experience + 1)) * 100 * 0.20 +
    (days_open / 60) * 0.15 +
    rng.uniform(0, 0.1, N)          # noise
)

def label(s):
    if s < 0.33:  return "Low"
    if s < 0.66:  return "Medium"
    return "High"

risk_level = [label(s) for s in raw_risk]
merged     = (raw_risk < 0.55).astype(int)
review_time = days_open * 24 * rng.uniform(0.3, 0.9, N)

df = pd.DataFrame({
    "lines_added":       lines_added,
    "lines_deleted":     lines_deleted,
    "files_changed":     files_changed,
    "commits":           commits,
    "comments":          comments,
    "author_experience": author_experience,
    "days_open":         np.round(days_open, 2),
    "review_time":       np.round(review_time, 2),
    "merged":            merged,
    "risk_level":        risk_level,
})

os.makedirs("ml/data", exist_ok=True)
df.to_csv("ml/data/raw_dataset.csv", index=False)
print(f"✅  Generated {N} rows → ml/data/raw_dataset.csv")
print(df["risk_level"].value_counts())
