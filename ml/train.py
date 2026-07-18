"""
Phase 7 – XGBoost Model Training
=================================
Input  : ml/data/X_train.csv, X_test.csv, y_train.csv, y_test.csv
Output : ml/model.pkl
         ml/artifacts/classification_report.txt
         ml/artifacts/confusion_matrix.png
         ml/artifacts/feature_importance.png

Usage:
  1. python ml/generate_dataset.py      # create raw data
  2. python ml/preprocess.py            # clean + split
  3. python ml/train.py                 # train + evaluate
"""

import os
import joblib
import numpy as np
import pandas as pd
import matplotlib
matplotlib.use("Agg")          # headless backend
import matplotlib.pyplot as plt
import seaborn as sns

from xgboost import XGBClassifier
from sklearn.metrics import classification_report, confusion_matrix
from sklearn.model_selection import cross_val_score

# ── Paths ─────────────────────────────────────────────────────────────────────
BASE_DIR       = os.path.dirname(os.path.abspath(__file__))
DATA_DIR       = os.path.join(BASE_DIR, "data")
ARTIFACTS_DIR  = os.path.join(BASE_DIR, "artifacts")
MODEL_PATH     = os.path.join(BASE_DIR, "model.pkl")

os.makedirs(ARTIFACTS_DIR, exist_ok=True)

FEATURE_COLS = [
    "lines_added",
    "lines_deleted",
    "files_changed",
    "commits",
    "comments",
    "author_experience",
    "days_open",
]
LABEL_NAMES = ["Low", "Medium", "High"]


def load_splits():
    print("[1/5] Loading train/test splits …")
    X_train = pd.read_csv(os.path.join(DATA_DIR, "X_train.csv"))
    X_test  = pd.read_csv(os.path.join(DATA_DIR, "X_test.csv"))
    y_train = pd.read_csv(os.path.join(DATA_DIR, "y_train.csv")).squeeze()
    y_test  = pd.read_csv(os.path.join(DATA_DIR, "y_test.csv")).squeeze()
    print(f"      Train: {X_train.shape}, Test: {X_test.shape}")
    return X_train, X_test, y_train, y_test


def train_model(X_train, y_train):
    print("[2/5] Training XGBoost classifier …")
    model = XGBClassifier(
        n_estimators=200,
        max_depth=6,
        learning_rate=0.1,
        subsample=0.8,
        colsample_bytree=0.8,
        use_label_encoder=False,
        eval_metric="mlogloss",
        random_state=42,
        n_jobs=-1,
    )
    model.fit(X_train, y_train, eval_set=[(X_train, y_train)], verbose=False)

    # 5-fold cross-validation accuracy
    cv_scores = cross_val_score(model, X_train, y_train, cv=5, scoring="accuracy")
    print(f"      CV accuracy: {cv_scores.mean():.4f} ± {cv_scores.std():.4f}")
    return model


def evaluate_model(model, X_test, y_test):
    print("[3/5] Evaluating on test set …")
    y_pred = model.predict(X_test)

    report = classification_report(y_test, y_pred, target_names=LABEL_NAMES)
    print(report)

    report_path = os.path.join(ARTIFACTS_DIR, "classification_report.txt")
    with open(report_path, "w") as f:
        f.write(report)
    print(f"      Report saved → {report_path}")
    return y_pred


def save_confusion_matrix(y_test, y_pred):
    print("[4/5] Saving confusion matrix …")
    cm = confusion_matrix(y_test, y_pred)

    fig, ax = plt.subplots(figsize=(6, 5))
    sns.heatmap(cm, annot=True, fmt="d", cmap="Blues",
                xticklabels=LABEL_NAMES, yticklabels=LABEL_NAMES, ax=ax)
    ax.set_xlabel("Predicted")
    ax.set_ylabel("Actual")
    ax.set_title("RepoPulse – XGBoost Risk Classification\nConfusion Matrix")
    plt.tight_layout()

    path = os.path.join(ARTIFACTS_DIR, "confusion_matrix.png")
    plt.savefig(path, dpi=150)
    plt.close()
    print(f"      Saved → {path}")


def save_feature_importance(model):
    print("[4/5] Saving feature importance …")
    importance = model.feature_importances_
    df = pd.DataFrame({
        "feature": FEATURE_COLS,
        "importance": importance,
    }).sort_values("importance", ascending=True)

    fig, ax = plt.subplots(figsize=(7, 5))
    colors = sns.color_palette("viridis", len(df))
    ax.barh(df["feature"], df["importance"], color=colors)
    ax.set_xlabel("F-score (Gain)")
    ax.set_title("RepoPulse – Feature Importance")
    plt.tight_layout()

    path = os.path.join(ARTIFACTS_DIR, "feature_importance.png")
    plt.savefig(path, dpi=150)
    plt.close()
    print(f"      Saved → {path}")


def save_model(model):
    print("[5/5] Saving model …")
    joblib.dump(model, MODEL_PATH)
    print(f"      Model saved → {MODEL_PATH}")


# ── Main ──────────────────────────────────────────────────────────────────────

def main():
    X_train, X_test, y_train, y_test = load_splits()
    model  = train_model(X_train, y_train)
    y_pred = evaluate_model(model, X_test, y_test)
    save_confusion_matrix(y_test, y_pred)
    save_feature_importance(model)
    save_model(model)
    print("\n✅  Training complete.")


if __name__ == "__main__":
    main()
