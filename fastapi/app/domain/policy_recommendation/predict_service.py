from __future__ import annotations

import os
import pickle
from pathlib import Path
from typing import Any, Optional

import numpy as np

DEFAULT_POLICY_ID = os.environ.get("DEFAULT_POLICY_ID", "default_policy")
DEFAULT_MODEL_DIR = os.environ.get("MODEL_DIR", "/tmp/ml_models")


class PredictService:
    """Infer policy recommendations using trained RandomForest model."""

    def __init__(self, model_dir: str | None = None):
        self.model_dir = Path(model_dir or DEFAULT_MODEL_DIR)
        self.model_dir.mkdir(parents=True, exist_ok=True)
        self.model, self.encoder, self.feature_names = self._load_model()

    def _load_model(self) -> tuple[Optional[object], Optional[object], list[str]]:
        if not self.model_dir.exists():
            return None, None, []
        files = sorted(
            self.model_dir.glob("*.pkl"),
            key=lambda p: p.stat().st_mtime,
            reverse=True,
        )
        for f in files:
            with open(f, "rb") as fh:
                payload = pickle.load(fh)
            if payload is not None and isinstance(payload, tuple) and len(payload) == 3:
                return payload
        return None, None, []

    def predict(self, input_features: dict[str, Any]) -> dict[str, Any]:
        """Predice politicaId mas probable."""
        if self.model is None or self.encoder is None:
            return self._fallback("No trained model available")

        try:
            vec = np.array([[0.0] * len(self.feature_names)])
            for i, name in enumerate(self.feature_names):
                vec[0, i] = float(input_features.get(name, 0.0))

            proba = self.model.predict_proba(vec)[0]
            top_idx = int(np.argmax(proba))
            policy_id = str(self.encoder.inverse_transform([top_idx])[0])
            score = float(proba[top_idx])

            return {
                "policy_id": policy_id,
                "score_confianza": round(score, 4),
                "explanation": f"RandomForest top-1 (score={score:.3f})",
                "human_review_required": True,
            }
        except Exception as e:
            return self._fallback(f"Prediction error: {e}")

    def _fallback(self, reason: str) -> dict[str, Any]:
        return {
            "policy_id": DEFAULT_POLICY_ID,
            "score_confianza": 0.0,
            "explanation": reason,
            "human_review_required": True,
        }
