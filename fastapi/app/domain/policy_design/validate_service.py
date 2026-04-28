from app.domain.policy_design.schemas import (
    NodoInfo, ConexionInfo, ValidateRequest, ValidateResponse, ValidationError,
)


class ValidateService:
    """Valida estructura de una política de negocio."""

    def validate(self, body: ValidateRequest) -> ValidateResponse:
        nodos = body.nodos
        conexiones = body.conexiones
        errores: list[ValidationError] = []
        warnings: list[ValidationError] = []

        tipos = [n.tipo for n in nodos]
        ids_nodo = {n.id_nodo for n in nodos}

        # 1. Debe tener al menos 1 INICIO
        if "INICIO" not in tipos:
            errores.append(ValidationError(
                codigo="SIN_INICIO", mensaje="La política no tiene ningún nodo INICIO", severidad="error"))

        # 2. Debe tener al menos 1 FIN
        if "FIN" not in tipos:
            errores.append(ValidationError(
                codigo="SIN_FIN", mensaje="La política no tiene ningún nodo FIN", severidad="error"))

        # 3. Nodos huérfanos (sin conexiones)
        if conexiones:
            conectados = set()
            for c in conexiones:
                conectados.add(c.origen)
                conectados.add(c.destino)
            huerfanos = ids_nodo - conectados
            if huerfanos:
                warnings.append(ValidationError(
                    codigo="NODOS_HUERFANOS",
                    mensaje=f"Nodos sin conexiones: {', '.join(sorted(huerfanos)[:5])}",
                    severidad="warning"))

        # 4. DECISION debe tener ≥ 2 salidas
        if conexiones:
            salidas_por_nodo: dict[str, int] = {}
            for c in conexiones:
                salidas_por_nodo[c.origen] = salidas_por_nodo.get(c.origen, 0) + 1
            for n in nodos:
                if n.tipo == "DECISION" and salidas_por_nodo.get(n.id_nodo, 0) < 2:
                    errores.append(ValidationError(
                        codigo="DECISION_SIN_RAMAS",
                        mensaje=f"Nodo DECISION '{n.nombre}' ({n.id_nodo}) necesita al menos 2 salidas",
                        severidad="error"))

        # 5. Nodos INICIO no deben tener entradas
        if conexiones:
            entradas_por_nodo: dict[str, int] = {}
            for c in conexiones:
                entradas_por_nodo[c.destino] = entradas_por_nodo.get(c.destino, 0) + 1
            for n in nodos:
                if n.tipo == "INICIO" and entradas_por_nodo.get(n.id_nodo, 0) > 0:
                    warnings.append(ValidationError(
                        codigo="INICIO_CON_ENTRADA",
                        mensaje=f"Nodo INICIO '{n.nombre}' no debería tener conexiones de entrada",
                        severidad="warning"))

        # 6. Ciclos simples (detección básica)
        if conexiones and len(conexiones) <= 100:
            ciclos = self._detectar_ciclos(ids_nodo, conexiones)
            if ciclos:
                warnings.append(ValidationError(
                    codigo="CICLOS_DETECTADOS",
                    mensaje=f"Posibles ciclos involucrando: {', '.join(list(ciclos)[:5])}",
                    severidad="warning"))

        valido = len(errores) == 0
        return ValidateResponse(valido=valido, errores=errores, warnings=warnings)

    def _detectar_ciclos(self, ids_nodo: set[str], conexiones: list[ConexionInfo]) -> set[str]:
        ady: dict[str, list[str]] = {n: [] for n in ids_nodo}
        for c in conexiones:
            if c.origen in ady:
                ady[c.origen].append(c.destino)

        visitado: set[str] = set()
        en_pila: set[str] = set()
        ciclos: set[str] = set()

        def dfs(nodo: str):
            visitado.add(nodo)
            en_pila.add(nodo)
            for vecino in ady.get(nodo, []):
                if vecino not in visitado:
                    dfs(vecino)
                elif vecino in en_pila:
                    ciclos.add(nodo)
            en_pila.discard(nodo)

        for n in ids_nodo:
            if n not in visitado:
                dfs(n)
        return ciclos
