from datetime import datetime, timezone

from fastapi import APIRouter

from app.core.config import get_settings
from app.schemas.health import HealthResponse

router = APIRouter()


def build_health_response() -> HealthResponse:
    settings = get_settings()
    return HealthResponse(
        status="UP",
        service=settings.app_name,
        environment=settings.app_env,
        timestamp=datetime.now(timezone.utc).isoformat(),
    )


@router.get("/health", response_model=HealthResponse, summary="Health check (prefijo /api)")
async def health_api() -> HealthResponse:
    """Alias bajo el prefijo /api; el chequeo canónico para operaciones simples es GET /health."""
    return build_health_response()
