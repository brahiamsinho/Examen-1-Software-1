from pydantic import BaseModel, Field


class BottleneckSignal(BaseModel):
    """Señal genérica que luego podrá mapearse a métricas reales o reglas."""

    code: str = Field(description="Identificador estable del tipo de señal")
    severity: str = Field(description="info | warning | critical (convención interna)")
    detail: str = Field(default="")


class BottleneckAnalysisResult(BaseModel):
    status: str = Field(description="stub | ok | error en futuras versiones")
    signals: list[BottleneckSignal] = Field(default_factory=list)
    summary: str = Field(default="")
