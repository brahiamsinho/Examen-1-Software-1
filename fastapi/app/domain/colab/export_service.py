import csv
import io
import os
from datetime import datetime, timezone

import pandas as pd
from motor.motor_asyncio import AsyncIOMotorClient


class ExportService:
    """Exporta dataset de entrenamiento desde Mongo a CSV."""

    def __init__(self, mongo_uri: str | None = None, mongo_db: str | None = None):
        self.mongo_uri = mongo_uri or os.environ.get("MONGODB_URI", "mongodb://localhost:27017")
        self.mongo_db = mongo_db or os.environ.get("MONGODB_DB", "tramitesdb")

    async def export_csv(self, limit: int = 5000, solo_completados: bool = True) -> str:
        client = AsyncIOMotorClient(self.mongo_uri)
        try:
            db = client[self.mongo_db]

            query: dict = {"politicaId": {"$exists": True, "$ne": None}}
            if solo_completados:
                query["estado"] = {"$in": ["APROBADO", "CERRADO"]}

            tramites_cursor = db["tramites"].find(query).limit(limit)
            tramites = await tramites_cursor.to_list(length=limit)

            rows = []
            for t in tramites:
                tramite_id = str(t["_id"])
                politica_id = str(t.get("politicaId", ""))
                prioridad = t.get("prioridad", "MEDIA")
                estado = t.get("estado", "")
                fecha_registro = t.get("fechaRegistro")

                # Tiempo total del trámite
                recorridos_cursor = db["recorridos_tramite"].find({"tramiteId": t["_id"]})
                recorridos = await recorridos_cursor.to_list(length=200)

                num_nodos = len(recorridos)
                tiempos = []
                for r in recorridos:
                    entrada = r.get("fechaEntrada")
                    salida = r.get("fechaSalida")
                    if entrada and salida:
                        tiempos.append((salida - entrada).total_seconds() / 3600.0)

                tiempo_total_h = round(sum(tiempos), 2) if tiempos else 0.0
                tiempo_promedio_h = round(sum(tiempos) / len(tiempos), 2) if tiempos else 0.0
                tiempo_max_h = round(max(tiempos), 2) if tiempos else 0.0

                prioridad_map = {"BAJA": 1, "MEDIA": 2, "ALTA": 3}
                completado = 1 if estado in ("APROBADO", "CERRADO") else 0

                rows.append({
                    "tramite_id": tramite_id,
                    "politica_id": politica_id,
                    "prioridad": prioridad,
                    "prioridad_num": prioridad_map.get(prioridad, 2),
                    "estado": estado,
                    "completado": completado,
                    "num_nodos": num_nodos,
                    "tiempo_total_h": tiempo_total_h,
                    "tiempo_promedio_h": tiempo_promedio_h,
                    "tiempo_max_h": tiempo_max_h,
                })

            df = pd.DataFrame(rows)
            output = io.StringIO()
            df.to_csv(output, index=False)
            return output.getvalue()
        finally:
            client.close()
