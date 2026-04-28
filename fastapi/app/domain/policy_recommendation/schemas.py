from __future__ import annotations

from typing import Optional, List

from pydantic import BaseModel, ConfigDict


class TrainRequest(BaseModel):
    model_config = ConfigDict(protected_namespaces=())
    dataset_source: Optional[str] = None
    training_params: Optional[dict[str, float | str | int]] = None


class TrainResponse(BaseModel):
    model_config = ConfigDict(protected_namespaces=())
    status: str
    model_id: Optional[str] = None
    message: str = "Training initiated (stub)"
    metrics: Optional[dict] = None


class PredictRequest(BaseModel):
    tramite_id: str
    context: Optional[dict] = None


class PredictResponse(BaseModel):
    policy_id: str
    score_confianza: float
    explanation: Optional[str] = None
    human_review_required: bool = True


class ModelInfo(BaseModel):
    model_config = ConfigDict(protected_namespaces=())
    model_id: str
    version: str
    created_at: str
    metrics: Optional[dict[str, float]] = None
