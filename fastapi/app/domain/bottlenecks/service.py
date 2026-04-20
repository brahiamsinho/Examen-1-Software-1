from typing import Any

from app.domain.bottlenecks.schemas import BottleneckAnalysisResult


class BottleneckAnalysisService:
    """
    Punto de extensión: aquí vivirá la lógica de correlación de métricas,
    umbrales y (más adelante) modelos o reglas de detección.
    """

    async def evaluate(self, *, context: dict[str, Any] | None = None) -> BottleneckAnalysisResult:
        _ = context
        return BottleneckAnalysisResult(status="stub", signals=[])


bottleneck_analysis_service = BottleneckAnalysisService()
