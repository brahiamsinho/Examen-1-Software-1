from pydantic import BaseModel, Field

# ── Suggest ──

class NodoInfo(BaseModel):
    id_nodo: str
    nombre: str
    tipo: str  # INICIO, ACTIVIDAD, DECISION, PARALELO, FIN
    orden: int = 0

class ConexionInfo(BaseModel):
    id_conexion: str
    origen: str
    destino: str
    tipo_flujo: str = "SECUENCIAL"

class SuggestRequest(BaseModel):
    politica_id: str | None = None
    nodos_actuales: list[NodoInfo] = Field(default_factory=list)
    conexiones_actuales: list[ConexionInfo] = Field(default_factory=list)
    nodo_actual_id: str | None = None  # nodo desde el cual sugerir siguiente

class NodoSugerido(BaseModel):
    tipo: str
    nombre: str
    confianza: float
    razon: str = ""

class SuggestResponse(BaseModel):
    sugerencias: list[NodoSugerido] = Field(default_factory=list)

# ── Validate ──

class ValidateRequest(BaseModel):
    nodos: list[NodoInfo] = Field(default_factory=list)
    conexiones: list[ConexionInfo] = Field(default_factory=list)

class ValidationError(BaseModel):
    codigo: str
    mensaje: str
    severidad: str = "error"  # error | warning

class ValidateResponse(BaseModel):
    valido: bool
    errores: list[ValidationError] = Field(default_factory=list)
    warnings: list[ValidationError] = Field(default_factory=list)
