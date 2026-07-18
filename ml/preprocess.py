"""
Phase 6 – ML Dataset Preprocessing Pipeline
============================================
Input  : ml/data/raw_dataset.csv
Output : ml/data/clean_dataset.csv
         ml/data/X_train.csv, X_test.csv, y_train.csv, y_test.csv

Steps:
  1. Load raw CSV
  2. Drop rows with critical nulls
  3. Clip outliers (IQR method)
  4. Normalise numeric columns (MinMaxScaler)
  5. Encode target label (risk_level: Low→0, Medium→1, High→2)
  6. 80/20 stratified train/test split
  7. Save artefacts
"""

import os
import pandas as pd
import numpy as np
from sklearn.model_selection import train_test_split
from sklearn.preprocessing import MinMaxScaler
import joblib

# ── Paths ─────────────────────────────────────────────────────────────────────
BASE_DIR  = os.path.dirname(os.path.abspath(__file__))
DATA_DIR  = os.path.join(BASE_DIR, "data")
RAW_PATH  = os.path.join(DATA_DIR, "raw_dataset.csv")
CLEAN_PATH = os.path.join(DATA_DIR, "clean_dataset.csv")
SCALER_PATH = os.path.join(BASE_DIR, "scaler.pkl")

FEATURE_COLS = [
    "lines_added",
    "lines_deleted",
    "files_changed",
    "commits",
    "comments",
    "author_experience",
    "days_open",
]
TARGET_COL = "risk_level"
LABEL_MAP  = {"Low": 0, "Medium": 1, "High": 2}

os.makedirs(DATA_DIR, exist_ok=True)


def load_data(path: str) -> pd.DataFrame:
    print(f"[1/6] Loading data from {path} …")
    df = pd.read_csv(path)
    print(f"      Shape: {df.shape}")
    return df


def clean_data(df: pd.DataFrame) -> pd.DataFrame:
    print("[2/6] Cleaning data …")
    initial = len(df)

    # Drop rows missing target or any feature
    df = df.dropna(subset=[TARGET_COL] + FEATURE_COLS)
    print(f"      Dropped {initial - len(df)} rows with nulls → {len(df)} remaining")

    # Remove invalid values
    df = df[df["lines_added"] >= 0]
    df = df[df["lines_deleted"] >= 0]
    df = df[df["files_changed"] >= 0]
    df = df[df["commits"] >= 1]
    df = df[df["days_open"] >= 0]
    df = df[df[TARGET_COL].isin(LABEL_MAP.keys())]

    print(f"      After value validation: {len(df)} rows")
    return df


def clip_outliers(df: pd.DataFrame) -> pd.DataFrame:
    """Cap values at Q1 - 1.5*IQR and Q3 + 1.5*IQR for each numeric feature."""
    print("[3/6] Clipping outliers (IQR method) …")
    df = df.copy()
    for col in FEATURE_COLS:
        q1 = df[col].quantile(0.25)
        q3 = df[col].quantile(0.75)
        iqr = q3 - q1
        lower, upper = q1 - 1.5 * iqr, q3 + 1.5 * iqr
        clipped = ((df[col] < lower) | (df[col] > upper)).sum()
        df[col] = df[col].clip(lower, upper)
        if clipped:
            print(f"      {col}: clipped {clipped} outliers")
    return df


def normalise(df: pd.DataFrame):
    """MinMax-scale numeric features. Returns scaled DataFrame + fitted scaler."""
    print("[4/6] Normalising numeric columns (MinMaxScaler) …")
    scaler = MinMaxScaler()
    df = df.copy()
    df[FEATURE_COLS] = scaler.fit_transform(df[FEATURE_COLS])
    return df, scaler


def encode_target(df: pd.DataFrame) -> pd.DataFrame:
    """Map Low/Medium/High → 0/1/2."""
    print("[5/6] Encoding target label …")
    df = df.copy()
    df[TARGET_COL] = df[TARGET_COL].map(LABEL_MAP)
    counts = df[TARGET_COL].value_counts().sort_index()
    for k, v in {"Low (0)": 0, "Medium (1)": 1, "High (2)": 2}.items():
        print(f"      {k}: {counts.get(v, 0)} rows")
    return df


def split_and_save(df: pd.DataFrame):
    """Stratified 80/20 split, save to CSV."""
    print("[6/6] Splitting 80/20 (stratified) and saving …")
    X = df[FEATURE_COLS]
    y = df[TARGET_COL]

    X_train, X_test, y_train, y_test = train_test_split(
        X, y, test_size=0.20, random_state=42, stratify=y
    )

    X_train.to_csv(os.path.join(DATA_DIR, "X_train.csv"), index=False)
    X_test.to_csv(os.path.join(DATA_DIR,  "X_test.csv"),  index=False)
    y_train.to_csv(os.path.join(DATA_DIR, "y_train.csv"), index=False)
    y_test.to_csv(os.path.join(DATA_DIR,  "y_test.csv"),  index=False)

    print(f"      Train: {len(X_train)}, Test: {len(X_test)}")
    return X_train, X_test, y_train, y_test


# ── Main ──────────────────────────────────────────────────────────────────────

def main():
    df = load_data(RAW_PATH)
    df = clean_data(df)
    df = clip_outliers(df)
    df, scaler = normalise(df)
    df = encode_target(df)

    df.to_csv(CLEAN_PATH, index=False)
    print(f"\n✅  Clean dataset saved → {CLEAN_PATH}")

    joblib.dump(scaler, SCALER_PATH)
    print(f"✅  Scaler saved        → {SCALER_PATH}")

    split_and_save(df)
    print("\n✅  Preprocessing complete.")


if __name__ == "__main__":
    main()
