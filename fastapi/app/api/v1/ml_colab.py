from fastapi import APIRouter, UploadFile, File
from fastapi.responses import StreamingResponse
import io

from pydantic import BaseModel

from app.core.config import get_settings
from app.domain.colab.schemas import ExportRequest, ImportResponse
from app.domain.colab.export_service import ExportService
from app.domain.colab.import_service import ImportService

router = APIRouter(tags=["ML Colab"])


@router.post("/ml/export/dataset")
async def export_dataset(body: ExportRequest):
    """Descarga dataset CSV para entrenar en Google Colab."""
    settings = get_settings()
    service = ExportService(mongo_uri=settings.mongodb_uri, mongo_db=settings.mongodb_db)
    csv_content = await service.export_csv(limit=body.limit, solo_completados=body.incluir_solo_completados)

    return StreamingResponse(
        io.StringIO(csv_content),
        media_type="text/csv",
        headers={"Content-Disposition": "attachment; filename=dataset_tramites.csv"},
    )


@router.post("/ml/import/model", response_model=ImportResponse)
async def import_model(file: UploadFile = File(...)):
    """Importa modelo .pkl entrenado en Google Colab."""
    settings = get_settings()
    content = await file.read()
    service = ImportService(model_dir=settings.model_dir)
    return service.import_model(content, file.filename or "modelo.pkl")
