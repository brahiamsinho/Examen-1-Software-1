from fastapi import APIRouter

from app.api.v1 import health
from app.api.v1.ml_policy import router as ml_policy_router
from app.api.v1.ml_bottlenecks import router as ml_bottlenecks_router
from app.api.v1.ml_design import router as ml_design_router
from app.api.v1.ml_sla import router as ml_sla_router
from app.api.v1.ml_colab import router as ml_colab_router

api_router = APIRouter()
api_router.include_router(health.router, tags=["Health"])
api_router.include_router(ml_policy_router)
api_router.include_router(ml_bottlenecks_router)
api_router.include_router(ml_design_router)
api_router.include_router(ml_sla_router)
api_router.include_router(ml_colab_router)
