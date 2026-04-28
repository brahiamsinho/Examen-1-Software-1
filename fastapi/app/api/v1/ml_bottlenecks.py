from __future__ import annotations

from fastapi import APIRouter

from pydantic import BaseModel

from app.core.config import get_settings
from app.domain.bottlenecks.schemas import BottleneckAnalysisResult
from app.domain.bottlenecks.service import BottleneckAnalysisService
from app.domain.bottlenecks.seeder import BottleneckSeeder

router = APIRouter(tags=["ML Bottlenecks"])


class AnalyzeRequest(BaseModel):
    politica_id: str


class SeedRequest(BaseModel):
    politica_id: str = "test-politica-bottleneck"
    num_tramites: int = 10


@router.post("/ml/bottlenecks/seed")
async def seed_test_data(body: SeedRequest) -> dict:
    settings = get_settings()
    seeder = BottleneckSeeder(
        mongo_uri=settings.mongodb_uri,
        mongo_db=settings.mongodb_db,
    )
    return await seeder.seed(body.politica_id, body.num_tramites)


@router.post("/ml/bottlenecks/analyze", response_model=BottleneckAnalysisResult)
async def analyze_bottlenecks(body: AnalyzeRequest) -> BottleneckAnalysisResult:
    settings = get_settings()
    service = BottleneckAnalysisService(
        mongo_uri=settings.mongodb_uri,
        mongo_db=settings.mongodb_db,
    )
    return await service.analyze_policy(body.politica_id)
