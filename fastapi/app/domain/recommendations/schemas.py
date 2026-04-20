from pydantic import BaseModel, Field


class RecommendationItem(BaseModel):
    id: str
    title: str
    rationale: str = ""


class RecommendationBatch(BaseModel):
    status: str = Field(description="stub | ok en futuras versiones")
    items: list[RecommendationItem] = Field(default_factory=list)
