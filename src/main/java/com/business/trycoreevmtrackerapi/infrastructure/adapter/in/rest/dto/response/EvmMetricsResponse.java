package com.business.trycoreevmtrackerapi.infrastructure.adapter.in.rest.dto.response;

import com.business.trycoreevmtrackerapi.domain.model.CostStatus;
import com.business.trycoreevmtrackerapi.domain.model.ScheduleStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(description = "Indicadores de Valor Ganado (EVM) calculados")
public record EvmMetricsResponse(
        @Schema(description = "Planned Value (PV)") BigDecimal plannedValue,
        @Schema(description = "Earned Value (EV)") BigDecimal earnedValue,
        @Schema(description = "Cost Variance (CV = EV - AC)") BigDecimal costVariance,
        @Schema(description = "Schedule Variance (SV = EV - PV)") BigDecimal scheduleVariance,
        @Schema(description = "Cost Performance Index (CPI = EV / AC)") BigDecimal cpi,
        @Schema(description = "Schedule Performance Index (SPI = EV / PV)") BigDecimal spi,
        @Schema(description = "Estimate at Completion (EAC = BAC / CPI)") BigDecimal eac,
        @Schema(description = "Variance at Completion (VAC = BAC - EAC)") BigDecimal vac,
        @Schema(description = "Interpretación del estado de costos") CostStatus costStatus,
        @Schema(description = "Interpretación del estado de cronograma") ScheduleStatus scheduleStatus
) {}
