from __future__ import annotations

import os
from typing import Optional

import pandas as pd
from motor.motor_asyncio import AsyncIOMotorClient


class DatasetService:
    """Dataset builder for policy recommendation. Reads from MongoDB."""

    def __init__(
        self,
        mongo_uri: str | None = None,
        mongo_db: str | None = None,
    ):
        self.mongo_uri = mongo_uri or os.environ.get("MONGODB_URI", "mongodb://localhost:27017")
        self.mongo_db = mongo_db or os.environ.get("MONGODB_DB", "tramitesdb")

    async def load_data(self) -> pd.DataFrame:
        """Lee tramites y recorridos de Mongo, construye features + label."""
        client: AsyncIOMotorClient = AsyncIOMotorClient(self.mongo_uri)
        try:
            db = client[self.mongo_db]

            tramites_cursor = db["tramites"].find(
                {"politicaId": {"$exists": True, "$ne": None}},
                {"_id": 1, "politicaId": 1, "prioridad": 1, "estado": 1, "clienteId": 1}
            )
            tramites_raw = await tramites_cursor.to_list(length=5000)
            if not tramites_raw:
                return pd.DataFrame()

            rows = []
            for t in tramites_raw:
                tramite_id = str(t["_id"])
                politica_id = str(t["politicaId"])
                prioridad = t.get("prioridad", "MEDIA")
                estado = t.get("estado", "")

                recorridos_cursor = db["recorridos_tramite"].find({"tramiteId": t["_id"]})
                recorridos = await recorridos_cursor.to_list(length=100)

                num_nodos = len(recorridos)
                tiempos = []
                for r in recorridos:
                    entrada = r.get("fechaEntrada")
                    salida = r.get("fechaSalida")
                    if entrada and salida:
                        dt = (salida - entrada).total_seconds() / 3600.0
                        tiempos.append(dt)

                tiempo_promedio = sum(tiempos) / len(tiempos) if tiempos else 0.0
                tiempo_max = max(tiempos) if tiempos else 0.0

                rows.append({
                    "tramite_id": tramite_id,
                    "politica_id": politica_id,
                    "prioridad": prioridad,
                    "estado": estado,
                    "num_nodos": num_nodos,
                    "tiempo_promedio_h": round(tiempo_promedio, 2),
                    "tiempo_max_h": round(tiempo_max, 2),
                })

            return pd.DataFrame(rows)
        finally:
            client.close()

    def build_features(self, df: pd.DataFrame) -> tuple[pd.DataFrame, pd.Series]:
        """Transforma raw data → X (features num), y (label politica_id)."""
        if df is None or df.empty:
            return pd.DataFrame(), pd.Series(dtype=str)

        df = df.copy()
        # Prioridad → numeric
        prioridad_map = {"BAJA": 1, "MEDIA": 2, "ALTA": 3}
        df["prioridad_num"] = df["prioridad"].map(prioridad_map).fillna(2)

        # Estado → flag completado
        df["completado"] = df["estado"].apply(lambda e: 1 if e in ("APROBADO", "CERRADO") else 0)

        # Feature columns
        feature_cols = ["prioridad_num", "num_nodos", "tiempo_promedio_h", "tiempo_max_h", "completado"]
        X = df[feature_cols].fillna(0)
        y = df["politica_id"].astype(str)

        return X, y
