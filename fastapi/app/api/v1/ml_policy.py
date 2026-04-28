from __future__ import annotations

from fastapi import APIRouter

from pydantic import BaseModel

from app.core.config import get_settings
from app.domain.policy_recommendation.schemas import TrainResponse, PredictResponse
from app.domain.policy_recommendation.train_service import TrainService
from app.domain.policy_recommendation.predict_service import PredictService
from app.domain.policy_recommendation.dataset_service import DatasetService

router = APIRouter(tags=["ML Policy"])


class TrainRequest(BaseModel):
    dataset_source: str | None = None
    training_params: dict | None = None


@router.post("/ml/policies/train", response_model=TrainResponse)
async def train_policies(body: TrainRequest) -> TrainResponse:
    settings = get_settings()
    ds = DatasetService(mongo_uri=settings.mongodb_uri, mongo_db=settings.mongodb_db)
    df = await ds.load_data()
    X, y = ds.build_features(df)
    trainer = TrainService(model_dir=settings.model_dir)
    result = trainer.train(X, y)
    return TrainResponse(
        status=result.get("status", "unknown"),
        model_id=result.get("model_id"),
        message=result.get("note") or result.get("status", "Training completed"),
        metrics=result.get("metrics"),
    )


class PredictRequest(BaseModel):
    tramite_id: str
    context: dict | None = None


@router.post("/ml/policies/predict", response_model=PredictResponse)
async def predict_policies(body: PredictRequest) -> PredictResponse:
    settings = get_settings()
    predictor = PredictService(model_dir=settings.model_dir)
    prediction = predictor.predict({"tramite_id": body.tramite_id, **(body.context or {})})
    return PredictResponse(**prediction)


@router.get("/ml/policies/model-info")
async def model_info() -> dict:
    settings = get_settings()
    predictor = PredictService(model_dir=settings.model_dir)
    has_model = predictor.model is not None
    return {
        "model_id": "policy_model_latest",
        "version": "0.1.0",
        "has_trained_model": has_model,
        "feature_names": predictor.feature_names if has_model else [],
        "created_at": "2026-04-27T00:00:00Z",
        "metrics": {"status": "ready" if has_model else "no_model_trained"},
    }
