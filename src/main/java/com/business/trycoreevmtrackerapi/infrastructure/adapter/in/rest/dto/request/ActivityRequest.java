package com.business.trycoreevmtrackerapi.infrastructure.adapter.in.rest.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

@Schema(description = "Datos para registrar o actualizar una actividad del proyecto")
public record ActivityRequest(
        @Schema(description = "Nombre de la actividad", example = "Diseño de Arquitectura Hexagonal")
        @NotBlank(message = "El nombre de la actividad es obligatorio")
        @Size(max = 150, message = "El nombre no puede exceder los 150 caracteres")
        String name,

        @Schema(description = "Presupuesto total planificado (BAC)", example = "5000.00")
        @NotNull(message = "El presupuesto planificado es obligatorio")
        @DecimalMin(value = "0.0", inclusive = true, message = "El presupuesto no puede ser negativo")
        BigDecimal plannedBudget,

        @Schema(description = "Porcentaje de avance planificado a la fecha de corte (0 a 100)", example = "50.00")
        @NotNull(message = "El porcentaje planificado es obligatorio")
        @DecimalMin(value = "0.0", message = "El porcentaje mínimo es 0")
        @DecimalMax(value = "100.0", message = "El porcentaje máximo es 100")
        BigDecimal plannedPercentage,

        @Schema(description = "Porcentaje de avance real completado (0 a 100)", example = "40.00")
        @NotNull(message = "El porcentaje completado es obligatorio")
        @DecimalMin(value = "0.0", message = "El porcentaje mínimo es 0")
        @DecimalMax(value = "100.0", message = "El porcentaje máximo es 100")
        BigDecimal completedPercentage,

        @Schema(description = "Costo real incurrido hasta la fecha (AC)", example = "3000.00")
        @NotNull(message = "El costo real es obligatorio")
        @DecimalMin(value = "0.0", inclusive = true, message = "El costo real no puede ser negativo")
        BigDecimal actualCost
) {}
