import os
from collections import Counter

from motor.motor_asyncio import AsyncIOMotorClient

from app.domain.policy_design.schemas import (
    NodoInfo, NodoSugerido, SuggestRequest, SuggestResponse,
)


class SuggestService:
    """Sugiere siguiente nodo basado en patrones de politicas_negocio."""

    def __init__(self, mongo_uri: str | None = None, mongo_db: str | None = None):
        self.mongo_uri = mongo_uri or os.environ.get("MONGODB_URI", "mongodb://localhost:27017")
        self.mongo_db = mongo_db or os.environ.get("MONGODB_DB", "tramitesdb")

    async def suggest(self, body: SuggestRequest) -> SuggestResponse:
        client = AsyncIOMotorClient(self.mongo_uri)
        try:
            db = client[self.mongo_db]
            politicas = await db["politicas_negocio"].find({}).to_list(length=500)

            if not politicas:
                return SuggestResponse(sugerencias=[
                    NodoSugerido(tipo="ACTIVIDAD", nombre="Nueva actividad", confianza=0.5,
                                 razon="Sin datos históricos. Sugerencia genérica.")
                ])

            ultimo_tipo = "INICIO"
            if body.nodo_actual_id:
                for n in body.nodos_actuales:
                    if n.id_nodo == body.nodo_actual_id:
                        ultimo_tipo = n.tipo
                        break

            # 1. Analizar TODAS las políticas existentes: ¿qué tipo sigue a qué tipo?
            transiciones: list[tuple[str, str]] = []
            nombres_por_tipo: dict[str, list[str]] = {}
            tipos_usados: Counter = Counter()

            for p in politicas:
                nodos = p.get("nodos", [])
                conexiones = p.get("conexiones", [])

                id_a_tipo = {}
                id_a_nombre = {}
                for nodo in nodos:
                    nid = str(nodo.get("idNodo", ""))
                    tipo = str(nodo.get("tipoNodo", ""))
                    nombre = str(nodo.get("nombre", ""))
                    id_a_tipo[nid] = tipo
                    id_a_nombre[nid] = nombre
                    tipos_usados[tipo] += 1
                    nombres_por_tipo.setdefault(tipo, []).append(nombre)

                for c in conexiones:
                    origen = str(c.get("origenNodoId", ""))
                    destino = str(c.get("destinoNodoId", ""))
                    tipo_origen = id_a_tipo.get(origen, "")
                    tipo_destino = id_a_tipo.get(destino, "")
                    if tipo_origen and tipo_destino:
                        transiciones.append((tipo_origen, tipo_destino))

            # 2. Frecuencia de transiciones desde ultimo_tipo
            candidatos = [dest for (orig, dest) in transiciones if orig == ultimo_tipo]
            if not candidatos:
                # fallback: usar todas las transiciones
                candidatos = [dest for (_, dest) in transiciones]

            conteo = Counter(candidatos)
            total = sum(conteo.values()) or 1

            # 3. Construir sugerencias top-3
            sugerencias: list[NodoSugerido] = []
            for tipo, count in conteo.most_common(3):
                confianza = round(count / total, 2)
                nombre = self._mejor_nombre(nombres_por_tipo.get(tipo, []))
                razon = f"Después de {ultimo_tipo}, {count}/{total} políticas usan {tipo}"
                sugerencias.append(NodoSugerido(
                    tipo=tipo, nombre=nombre, confianza=confianza, razon=razon,
                ))

            if not sugerencias and tipos_usados:
                top_tipo = tipos_usados.most_common(1)[0][0]
                sugerencias.append(NodoSugerido(
                    tipo=top_tipo,
                    nombre=self._mejor_nombre(nombres_por_tipo.get(top_tipo, [])),
                    confianza=0.3,
                    razon="Tipo más frecuente en todas las políticas",
                ))

            return SuggestResponse(sugerencias=sugerencias)
        finally:
            client.close()

    @staticmethod
    def _mejor_nombre(nombres: list[str]) -> str:
        if not nombres:
            return "Nuevo nodo"
        return Counter(nombres).most_common(1)[0][0]
