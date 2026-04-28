from fastapi import APIRouter

from app.core.config import get_settings
from app.domain.policy_design.schemas import SuggestRequest, SuggestResponse, ValidateRequest, ValidateResponse
from app.domain.policy_design.suggest_service import SuggestService
from app.domain.policy_design.validate_service import ValidateService

router = APIRouter(tags=["ML Design"])


@router.post("/ml/design/suggest-next", response_model=SuggestResponse)
async def suggest_next(body: SuggestRequest) -> SuggestResponse:
    settings = get_settings()
    service = SuggestService(mongo_uri=settings.mongodb_uri, mongo_db=settings.mongodb_db)
    return await service.suggest(body)


@router.post("/ml/design/validate", response_model=ValidateResponse)
async def validate_policy(body: ValidateRequest) -> ValidateResponse:
    service = ValidateService()
    return service.validate(body)
