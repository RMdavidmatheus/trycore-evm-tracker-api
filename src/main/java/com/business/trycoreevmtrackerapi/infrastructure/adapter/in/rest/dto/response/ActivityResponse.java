package com.business.trycoreevmtrackerapi.infrastructure.adapter.in.rest.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Detalle de una actividad con sus indicadores EVM")
public record ActivityResponse(
        UUID id,
        UUID projectId,
        String name,
        BigDecimal plannedBudget,
        BigDecimal plannedPercentage,
        BigDecimal completedPercentage,
        BigDecimal actualCost,
        EvmMetricsResponse metrics,
        boolean isActive,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
