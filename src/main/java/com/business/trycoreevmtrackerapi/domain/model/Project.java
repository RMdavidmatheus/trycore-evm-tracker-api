package com.business.trycoreevmtrackerapi.domain.model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record Project(
        UUID id,
        String name,
        String description,
        BigDecimal totalPlannedBudget,
        BigDecimal totalActualCost,
        List<Activity> activities,
        EvmMetrics consolidatedMetrics,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {}
