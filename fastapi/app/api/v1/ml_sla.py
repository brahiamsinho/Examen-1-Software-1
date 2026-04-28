from fastapi import APIRouter

from app.core.config import get_settings
from app.domain.sla.schemas import SlaPredictRequest, SlaPredictResponse
from app.domain.sla.predict_service import SlaPredictService

router = APIRouter(tags=["ML SLA"])


@router.post("/ml/sla/predict", response_model=SlaPredictResponse)
async def predict_sla(body: SlaPredictRequest) -> SlaPredictResponse:
    settings = get_settings()
    service = SlaPredictService(mongo_uri=settings.mongodb_uri, mongo_db=settings.mongodb_db)
    return await service.predict(body)
