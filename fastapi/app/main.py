from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from app.api.v1.health import router as health_router

app = FastAPI(
    title="Tramites Microservice - FastAPI",
    description="Microservicio especializado para analítica e IA",
    version="0.1.0",
    docs_url="/docs",
    redoc_url="/redoc",
)

# CORS - adjust origins in production via env
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # Restrict in prod
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Routers
app.include_router(health_router, prefix="/api/v1", tags=["Health"])
