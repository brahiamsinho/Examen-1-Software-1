from __future__ import annotations

import os
import pickle
from datetime import datetime, timezone
from pathlib import Path
from typing import Optional

import pandas as pd
import numpy as np
from sklearn.ensemble import RandomForestClassifier
from sklearn.preprocessing import LabelEncoder
from sklearn.model_selection import train_test_split
from sklearn.metrics import accuracy_score, f1_score

DEFAULT_POLICY_ID = os.environ.get("DEFAULT_POLICY_ID", "default_policy")
DEFAULT_MODEL_DIR = os.environ.get("MODEL_DIR", "/tmp/ml_models")


class TrainService:
    """Train a RandomForest policy recommendation model."""

    def __init__(self, model_dir: str | None = None):
        self.model_dir = Path(model_dir or DEFAULT_MODEL_DIR)
        self.model_dir.mkdir(parents=True, exist_ok=True)

    def train(self, X: pd.DataFrame, y: pd.Series) -> dict:
        """Entrena modelo con features X y label y (politicaId)."""
        if X is None or X.empty or len(X) < 3:
            model_id = f"policy_model_{datetime.now(timezone.utc).strftime('%Y%m%d%H%M%S')}"
            self._save_model(model_id, None)
            return {
                "model_id": model_id,
                "status": "fallback",
                "note": f"Data too small ({len(X) if X is not None else 0} rows). Dummy model saved.",
            }

        le = LabelEncoder()
        y_encoded = le.fit_transform(y.astype(str))

        X_train, X_val, y_train, y_val = train_test_split(
            X.values, y_encoded, test_size=0.2, random_state=42, stratify=y_encoded
        )

        model = RandomForestClassifier(
            n_estimators=100, max_depth=8, random_state=42, n_jobs=-1
        )
        model.fit(X_train, y_train)

        y_pred = model.predict(X_val)
        acc = float(accuracy_score(y_val, y_pred))
        f1 = float(f1_score(y_val, y_pred, average="weighted", zero_division=0))

        model_id = f"policy_model_{datetime.now(timezone.utc).strftime('%Y%m%d%H%M%S')}"
        self._save_model(model_id, (model, le, X.columns.tolist()))

        return {
            "model_id": model_id,
            "status": "trained",
            "samples": len(X),
            "features": X.columns.tolist(),
            "metrics": {"accuracy": round(acc, 4), "f1_weighted": round(f1, 4)},
        }

    def _save_model(self, model_id: str, payload: Optional[object]):
        path = self.model_dir / f"{model_id}.pkl"
        with open(path, "wb") as f:
            pickle.dump(payload, f)
