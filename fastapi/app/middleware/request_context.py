import logging
import time
import uuid
from collections.abc import Callable

from starlette.middleware.base import BaseHTTPMiddleware
from starlette.requests import Request
from starlette.responses import Response

log = logging.getLogger("app.request")


class RequestContextMiddleware(BaseHTTPMiddleware):
    """
    Base para observabilidad y futura detección de cuellos de botella:
    - request_id correlacionable con Spring Boot / Nginx (header o generado)
    - tiempo de proceso en header (útil para dashboards y alertas)
    """

    async def dispatch(self, request: Request, call_next: Callable[[Request], Response]) -> Response:
        request_id = request.headers.get("X-Request-ID") or str(uuid.uuid4())
        request.state.request_id = request_id
        start = time.perf_counter()
        response = await call_next(request)
        duration_ms = (time.perf_counter() - start) * 1000
        response.headers["X-Request-ID"] = request_id
        response.headers["X-Process-Time-Ms"] = f"{duration_ms:.2f}"
        log.info(
            "%s %s %s %.2fms",
            request_id,
            request.method,
            request.url.path,
            duration_ms,
        )
        return response
