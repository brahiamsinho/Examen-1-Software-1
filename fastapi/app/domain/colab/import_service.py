import os
import pickle
import shutil
from datetime import datetime, timezone
from pathlib import Path


class ImportService:
    """Recibe modelo .pkl entrenado en Colab y lo registra como activo."""

    def __init__(self, model_dir: str | None = None):
        self.model_dir = Path(model_dir or os.environ.get("MODEL_DIR", "/data/models"))
        self.model_dir.mkdir(parents=True, exist_ok=True)

    def import_model(self, file_content: bytes, original_filename: str) -> dict:
        # Validar que sea un pickle válido
        try:
            payload = pickle.loads(file_content)
        except Exception as e:
            return {
                "status": "error",
                "model_id": "",
                "mensaje": f"Archivo invalido: no es un pickle valido ({e})",
            }

        # Validar estructura esperada: (modelo, encoder, feature_names)
        if not isinstance(payload, tuple) or len(payload) < 2:
            return {
                "status": "error",
                "model_id": "",
                "mensaje": "Formato incorrecto. Esperado: tupla (modelo, encoder, [feature_names])",
            }

        model_id = f"colab_model_{datetime.now(timezone.utc).strftime('%Y%m%d%H%M%S')}"
        path = self.model_dir / f"{model_id}.pkl"

        with open(path, "wb") as f:
            pickle.dump(payload, f)

        # Eliminar modelos anteriores de Colab (solo los colab_*)
        for old in self.model_dir.glob("colab_model_*.pkl"):
            if old != path:
                old.unlink()

        return {
            "status": "ok",
            "model_id": model_id,
            "mensaje": f"Modelo importado: {model_id}. Feature names: {list(payload[2]) if len(payload) > 2 else 'N/A'}",
        }
