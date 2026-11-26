package com.business.trycoreevmtrackerapi.infrastructure.adapter.in.rest.exception;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.Map;

@Schema(description = "Estructura estándar de respuesta ante errores de la API")
public record ApiErrorResponse(
        @Schema(description = "Código de estado HTTP", example = "404")
        int status,

        @Schema(description = "Título descriptivo del error", example = "Recurso no encontrado")
        String error,

        @Schema(description = "Mensaje detallado", example = "Proyecto no encontrado con ID: 123e4567-e89b-12d3-a456-426614174000")
        String message,

        @Schema(description = "Ruta de la petición", example = "/api/v1/projects/123e4567-e89b-12d3-a456-426614174000")
        String path,

        @Schema(description = "Marca de tiempo del error")
        LocalDateTime timestamp,

        @Schema(description = "Detalle de errores de validación por campo (si aplica)")
        Map<String, String> validationErrors
) {}
