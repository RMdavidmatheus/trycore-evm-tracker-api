package com.business.trycoreevmtrackerapi.infrastructure.adapter.in.rest.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Datos para crear o actualizar un proyecto")
public record ProjectRequest(
        @Schema(description = "Nombre del proyecto", example = "Migración Core Bancario")
        @NotBlank(message = "El nombre del proyecto es obligatorio")
        @Size(max = 150, message = "El nombre no puede exceder los 150 caracteres")
        String name,

        @Schema(description = "Descripción breve del alcance", example = "Actualización de microservicios y base de datos")
        @Size(max = 500, message = "La descripción no puede exceder los 500 caracteres")
        String description
) {}
