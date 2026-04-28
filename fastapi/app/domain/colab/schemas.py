from pydantic import BaseModel


class ExportRequest(BaseModel):
    """Filtros opcionales para exportar dataset."""
    limit: int = 5000
    incluir_solo_completados: bool = True


class ExportResponse(BaseModel):
    status: str
    rows: int
    columns: list[str]
    mensaje: str


class ImportResponse(BaseModel):
    status: str
    model_id: str
    mensaje: str
