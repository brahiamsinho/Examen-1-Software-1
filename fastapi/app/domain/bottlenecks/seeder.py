from __future__ import annotations

import os
from datetime import datetime, timezone, timedelta
import random

from motor.motor_asyncio import AsyncIOMotorClient
from bson import ObjectId


class BottleneckSeeder:
    """Genera datos de prueba con cuellos de botella simulados."""

    def __init__(self, mongo_uri: str | None = None, mongo_db: str | None = None):
        self.mongo_uri = mongo_uri or os.environ.get("MONGODB_URI", "mongodb://localhost:27017")
        self.mongo_db = mongo_db or os.environ.get("MONGODB_DB", "tramitesdb")

    async def seed(self, politica_id: str, num_tramites: int = 10) -> dict:
        client: AsyncIOMotorClient = AsyncIOMotorClient(self.mongo_uri)
        try:
            db = client[self.mongo_db]

            nodos = [
                {"id": "nodo-ingreso", "nombre": "Ingreso", "area": "atencion"},
                {"id": "nodo-revision", "nombre": "Revision inicial", "area": "legal"},
                {"id": "nodo-evaluacion", "nombre": "Evaluacion tecnica", "area": "tecnologia"},
                {"id": "nodo-aprobacion", "nombre": "Aprobacion final", "area": "direccion"},
                {"id": "nodo-salida", "nombre": "Notificacion", "area": "atencion"},
            ]

            tiempo_base = {
                "nodo-ingreso": 1.0,
                "nodo-revision": 8.0,
                "nodo-evaluacion": 3.0,
                "nodo-aprobacion": 24.0,
                "nodo-salida": 0.5,
            }

            # Use same ObjectId for all tramites so analyzer can match them
            politicas_oid = ObjectId() if not ObjectId.is_valid(politica_id) else ObjectId(politica_id)
            cliente_oid = ObjectId()

            for i in range(num_tramites):
                tramite_id = ObjectId()
                t = datetime.now(timezone.utc) - timedelta(days=random.randint(1, 30))

                await db["tramites"].insert_one({
                    "_id": tramite_id,
                    "codigo": f"TEST-BN-{i:04d}",
                    "asunto": f"Tramite prueba cuello botella #{i}",
                    "descripcion": "Seeder para deteccion de cuellos de botella",
                    "fechaRegistro": t,
                    "prioridad": random.choice(["BAJA", "MEDIA", "ALTA"]),
                    "estado": random.choice(["EN_PROCESO", "APROBADO", "CERRADO"]),
                    "numeroTurno": i + 1,
                    "politicaId": politicas_oid,
                    "clienteId": cliente_oid,
                })

                for nodo in nodos:
                    base_h = tiempo_base[nodo["id"]]
                    variacion = base_h * random.uniform(-0.5, 0.5)
                    if random.random() < 0.3:
                        variacion += base_h * random.uniform(3.0, 8.0)
                    horas = max(0.1, base_h + variacion)
                    fecha_entrada = t
                    fecha_salida = t + timedelta(hours=horas)

                    await db["recorridos_tramite"].insert_one({
                        "tramiteId": tramite_id,
                        "nodoId": nodo["id"],
                        "areaId": ObjectId(),
                        "fechaEntrada": fecha_entrada,
                        "fechaSalida": fecha_salida,
                        "estado": "COMPLETADO",
                    })
                    t = fecha_salida

            return {
                "status": "ok",
                "politica_id": str(politicas_oid),
                "tramites_creados": num_tramites,
                "recorridos_creados": num_tramites * len(nodos),
                "instruccion": f"POST /api/ml/bottlenecks/analyze with body: {{\"politica_id\": \"{politicas_oid}\"}}",
            }
        finally:
            client.close()
