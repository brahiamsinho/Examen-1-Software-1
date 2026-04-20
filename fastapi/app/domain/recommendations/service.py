from typing import Any

from app.domain.recommendations.schemas import RecommendationBatch


class RecommendationService:
    """
    Punto de extensión: ranking de políticas, sugerencias de optimización,
    integración con LLM u orquestadores, sin acoplar al backend Spring.
    """

    async def suggest(self, *, context: dict[str, Any] | None = None) -> RecommendationBatch:
        _ = context
        return RecommendationBatch(status="stub", items=[])


recommendation_service = RecommendationService()
