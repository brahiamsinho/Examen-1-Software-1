from __future__ import annotations

import os
from typing import Any

import numpy as np
from motor.motor_asyncio import AsyncIOMotorClient

from app.domain.bottlenecks.schemas import BottleneckAnalysisResult, BottleneckSignal


class BottleneckAnalysisService:
    """Detección de cuellos de botella usando datos reales de recorridos_tramite."""

    def __init__(self, mongo_uri: str | None = None, mongo_db: str | None = None):
        self.mongo_uri = mongo_uri or os.environ.get("MONGODB_URI", "mongodb://localhost:27017")
        self.mongo_db = mongo_db or os.environ.get("MONGODB_DB", "tramitesdb")

    async def analyze_policy(self, politica_id: str) -> BottleneckAnalysisResult:
        """Analiza cuellos de botella para una política específica."""
        client: AsyncIOMotorClient = AsyncIOMotorClient(self.mongo_uri)
        try:
            db = client[self.mongo_db]

            # 1. Obtener tramites de esta política
            tramites_cursor = db["tramites"].find(
                {"politicaId": {"$exists": True}},
                {"_id": 1, "politicaId": 1},
            )
            tramites = await tramites_cursor.to_list(length=5000)

            target_tramite_ids = [
                t["_id"] for t in tramites
                if str(t.get("politicaId", "")) == politica_id
            ]

            if not target_tramite_ids:
                return BottleneckAnalysisResult(
                    status="ok",
                    signals=[],
                    summary=f"No hay trámites para la política {politica_id}",
                )

            # 2. Leer recorridos de esos trámites
            recorridos_cursor = db["recorridos_tramite"].find(
                {"tramiteId": {"$in": target_tramite_ids}},
                {"nodoId": 1, "areaId": 1, "fechaEntrada": 1, "fechaSalida": 1},
            )
            recorridos = await recorridos_cursor.to_list(length=50000)

            if not recorridos:
                return BottleneckAnalysisResult(
                    status="ok",
                    signals=[],
                    summary=f"Sin recorridos para política {politica_id}",
                )

            # 3. Agrupar por nodoId, calcular tiempos
            groups: dict[str, list[float]] = {}
            for r in recorridos:
                entrada = r.get("fechaEntrada")
                salida = r.get("fechaSalida")
                if not entrada or not salida:
                    continue
                hours = (salida - entrada).total_seconds() / 3600.0
                nodo_id = r.get("nodoId", "?")
                groups.setdefault(nodo_id, []).append(hours)

            # 4. Detectar anomalías usando median + max/median ratio
            signals: list[BottleneckSignal] = []
            for nodo_id, tiempos in groups.items():
                arr = np.array(tiempos)
                n = len(arr)
                if n < 3:
                    continue

                med = float(np.median(arr))
                avg = float(np.mean(arr))
                max_time = float(np.max(arr))
                ratio = max_time / med if med > 0 else 0

                if ratio > 4.0:
                    severity = "critical"
                elif ratio > 2.5:
                    severity = "warning"
                else:
                    continue

                signals.append(BottleneckSignal(
                    code="BOTTLENECK_TIME",
                    severity=severity,
                    detail=(
                        f"nodo={nodo_id} | "
                        f"n={n} | med={med:.1f}h | avg={avg:.1f}h | max={max_time:.1f}h | "
                        f"max/med={ratio:.1f}x"
                    ),
                ))

            signals.sort(key=lambda s: 0 if s.severity == "critical" else 1)

            summary = (
                f"Política {politica_id}: {len(tramites)} trámites, "
                f"{len(recorridos)} recorridos, "
                f"{len(signals)} señales detectadas "
                f"({sum(1 for s in signals if s.severity == 'critical')} críticas)"
            )

            return BottleneckAnalysisResult(
                status="ok",
                signals=signals,
                summary=summary,
            )
        finally:
            client.close()
