from pydantic import BaseModel, Field


class SlaPredictRequest(BaseModel):
    tramite_id: str


class NodoEta(BaseModel):
    nodo_id: str
    nodo_nombre: str
    eta_horas: float
    riesgo: str = "BAJO"  # BAJO, MEDIO, ALTO

class SlaPredictResponse(BaseModel):
    tramite_id: str
    eta_total_horas: float
    eta_total_dias: float
    riesgo_global: str = "BAJO"  # BAJO, MEDIO, ALTO, CRITICO
    nodos_restantes: int = 0
    detalle_nodos: list[NodoEta] = Field(default_factory=list)
    cuello_probable: str = ""
    mensaje: str = ""
