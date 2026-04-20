from fastapi import APIRouter
from datetime import datetime, timezone

router = APIRouter()


@router.get("/health", summary="Health check del microservicio")
async def health():
    return {
        "status": "UP",
        "service": "microservice-fastapi",
        "timestamp": datetime.now(timezone.utc).isoformat(),
    }
