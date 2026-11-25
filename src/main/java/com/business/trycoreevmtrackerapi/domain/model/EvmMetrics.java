package com.business.trycoreevmtrackerapi.domain.model;

import java.math.BigDecimal;

public record EvmMetrics(
        BigDecimal plannedValue,
        BigDecimal earnedValue,
        BigDecimal costVariance,
        BigDecimal scheduleVariance,
        BigDecimal cpi,
        BigDecimal spi,
        BigDecimal eac,
        BigDecimal vac,
        CostStatus costStatus,
        ScheduleStatus scheduleStatus
) {}
