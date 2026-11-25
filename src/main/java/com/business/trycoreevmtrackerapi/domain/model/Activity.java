package com.business.trycoreevmtrackerapi.domain.model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record Activity(
        UUID id,
        UUID projectId,
        String name,
        BigDecimal plannedBudget,
        BigDecimal plannedPercentage,
        BigDecimal completedPercentage,
        BigDecimal actualCost,
        EvmMetrics metrics,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {}
