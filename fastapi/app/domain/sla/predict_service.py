import os

import numpy as np
from motor.motor_asyncio import AsyncIOMotorClient

from app.domain.sla.schemas import SlaPredictRequest, SlaPredictResponse, NodoEta


class SlaPredictService:
    """Predice tiempo estimado de finalización de un trámite basado en históricos."""

    def __init__(self, mongo_uri: str | None = None, mongo_db: str | None = None):
        self.mongo_uri = mongo_uri or os.environ.get("MONGODB_URI", "mongodb://localhost:27017")
        self.mongo_db = mongo_db or os.environ.get("MONGODB_DB", "tramitesdb")

    async def predict(self, body: SlaPredictRequest) -> SlaPredictResponse:
        client = AsyncIOMotorClient(self.mongo_uri)
        try:
            db = client[self.mongo_db]

            # 1. Buscar el trámite
            from bson import ObjectId
            tramite = None
            if ObjectId.is_valid(body.tramite_id):
                tramite = await db["tramites"].find_one({"_id": ObjectId(body.tramite_id)})
            if not tramite:
                return SlaPredictResponse(
                    tramite_id=body.tramite_id,
                    eta_total_horas=0,
                    eta_total_dias=0,
                    riesgo_global="BAJO",
                    mensaje="Trámite no encontrado",
                )

            tramite_id_obj = tramite["_id"]
            politica_id = tramite.get("politicaId")
            nodo_actual_id = tramite.get("nodoActualId")

            if not politica_id or not nodo_actual_id:
                return SlaPredictResponse(
                    tramite_id=body.tramite_id,
                    eta_total_horas=0,
                    eta_total_dias=0,
                    riesgo_global="BAJO",
                    mensaje="Trámite sin política asignada o sin nodo actual",
                )

            # 2. Obtener todos los nodos de la política (ordenados)
            politica = await db["politicas_negocio"].find_one(
                {"_id": politica_id if isinstance(politica_id, ObjectId) else ObjectId(str(politica_id))}
            )
            if not politica:
                politica = await db["politicas_negocio"].find_one({"_id": ObjectId()})  # dummy

            if not politica:
                return SlaPredictResponse(
                    tramite_id=body.tramite_id,
                    eta_total_horas=0,
                    eta_total_dias=0,
                    riesgo_global="BAJO",
                    mensaje="Política no encontrada",
                )

            nodos = sorted(politica.get("nodos", []), key=lambda n: n.get("orden", 0))
            nodos_restantes = []
            encontrado = False
            for nodo in nodos:
                nid = nodo.get("idNodo", "")
                if nid == str(nodo_actual_id):
                    encontrado = True
                    continue
                if encontrado:
                    nodos_restantes.append(nodo)

            if not nodos_restantes:
                return SlaPredictResponse(
                    tramite_id=body.tramite_id,
                    eta_total_horas=0,
                    eta_total_dias=0,
                    riesgo_global="BAJO",
                    nodos_restantes=0,
                    mensaje="Trámite en nodo final o sin nodos restantes",
                )

            # 3. Buscar tiempos históricos para los nodos restantes
            historicos = await db["recorridos_tramite"].find(
                {"nodoId": {"$in": [n.get("idNodo", "") for n in nodos_restantes]}},
                {"nodoId": 1, "fechaEntrada": 1, "fechaSalida": 1},
            ).to_list(length=10000)

            tiempos_por_nodo: dict[str, list[float]] = {}
            for r in historicos:
                entrada = r.get("fechaEntrada")
                salida = r.get("fechaSalida")
                if entrada and salida:
                    h = (salida - entrada).total_seconds() / 3600.0
                    nodo_id = str(r.get("nodoId", ""))
                    tiempos_por_nodo.setdefault(nodo_id, []).append(h)

            # 4. Calcular ETA por nodo
            detalle: list[NodoEta] = []
            eta_total = 0.0
            cuello_nodo = ""
            cuello_eta = 0.0

            for nodo in nodos_restantes:
                nid = nodo.get("idNodo", "")
                nombre = nodo.get("nombre", nid)
                tiempos = tiempos_por_nodo.get(nid, [])

                if tiempos:
                    arr = np.array(tiempos)
                    eta = float(np.median(arr))
                    std = float(np.std(arr)) if len(arr) > 1 else 0.0
                    cv = std / eta if eta > 0 else 0
                    riesgo = "ALTO" if cv > 1.0 else ("MEDIO" if cv > 0.5 else "BAJO")
                else:
                    eta = 4.0  # default 4h si no hay datos
                    riesgo = "MEDIO"

                eta_total += eta
                detalle.append(NodoEta(
                    nodo_id=nid,
                    nodo_nombre=nombre,
                    eta_horas=round(eta, 1),
                    riesgo=riesgo,
                ))

                if eta > cuello_eta:
                    cuello_eta = eta
                    cuello_nodo = nombre

            # 5. Riesgo global
            if eta_total > 168:  # > 7 días
                riesgo_global = "CRITICO"
            elif eta_total > 72:  # > 3 días
                riesgo_global = "ALTO"
            elif eta_total > 24:  # > 1 día
                riesgo_global = "MEDIO"
            else:
                riesgo_global = "BAJO"

            return SlaPredictResponse(
                tramite_id=body.tramite_id,
                eta_total_horas=round(eta_total, 1),
                eta_total_dias=round(eta_total / 24, 1),
                riesgo_global=riesgo_global,
                nodos_restantes=len(nodos_restantes),
                detalle_nodos=detalle,
                cuello_probable=cuello_nodo,
                mensaje=f"ETA basado en {len(historicos)} registros históricos",
            )
        finally:
            client.close()
