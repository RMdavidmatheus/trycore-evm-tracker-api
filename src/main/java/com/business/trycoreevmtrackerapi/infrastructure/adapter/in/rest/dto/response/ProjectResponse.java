package com.business.trycoreevmtrackerapi.infrastructure.adapter.in.rest.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Schema(description = "Detalle de un proyecto con sus actividades e indicadores EVM consolidados")
public record ProjectResponse(
        UUID id,
        String name,
        String description,
        BigDecimal totalPlannedBudget,
        BigDecimal totalActualCost,
        List<ActivityResponse> activities,
        EvmMetricsResponse consolidatedMetrics,
        boolean isActive,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}