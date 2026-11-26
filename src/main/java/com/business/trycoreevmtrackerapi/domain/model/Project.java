package com.business.trycoreevmtrackerapi.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record Project(
        UUID id,
        String name,
        String description,
        boolean isActive,
        BigDecimal totalPlannedBudget,
        BigDecimal totalActualCost,
        List<Activity> activities,
        EvmMetrics consolidatedMetrics,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
