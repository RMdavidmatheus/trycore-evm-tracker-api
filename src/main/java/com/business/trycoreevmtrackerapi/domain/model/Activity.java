package com.business.trycoreevmtrackerapi.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
        boolean  isActive,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
