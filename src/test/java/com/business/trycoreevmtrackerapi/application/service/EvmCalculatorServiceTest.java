package com.business.trycoreevmtrackerapi.application.service;

import com.business.trycoreevmtrackerapi.domain.model.Activity;
import com.business.trycoreevmtrackerapi.domain.model.CostStatus;
import com.business.trycoreevmtrackerapi.domain.model.EvmMetrics;
import com.business.trycoreevmtrackerapi.domain.model.ScheduleStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class EvmCalculatorServiceTest {

    private final EvmCalculatorService service = new EvmCalculatorService();

    @Test
    void calculatesMetricsForAnActivityUnderBudgetAndAheadOfSchedule() {
        EvmMetrics metrics = service.calculateForActivity(activity("100", "50", "75", "50", null));

        assertEquals(bd("50.0000"), metrics.plannedValue());
        assertEquals(bd("75.0000"), metrics.earnedValue());
        assertEquals(bd("25.0000"), metrics.costVariance());
        assertEquals(bd("25.0000"), metrics.scheduleVariance());
        assertEquals(bd("1.5000"), metrics.cpi());
        assertEquals(bd("1.5000"), metrics.spi());
        assertEquals(bd("66.6667"), metrics.eac());
        assertEquals(bd("33.3333"), metrics.vac());
        assertEquals(CostStatus.UNDER_BUDGET, metrics.costStatus());
        assertEquals(ScheduleStatus.AHEAD_OF_SCHEDULE, metrics.scheduleStatus());
    }

    @Test
    void calculatesOverBudgetAndBehindScheduleStatuses() {
        EvmMetrics metrics = service.calculateForActivity(activity("100", "50", "25", "50", null));

        assertEquals(bd("0.5000"), metrics.cpi());
        assertEquals(bd("0.5000"), metrics.spi());
        assertEquals(CostStatus.OVER_BUDGET, metrics.costStatus());
        assertEquals(ScheduleStatus.BEHIND_SCHEDULE, metrics.scheduleStatus());
    }

    @Test
    void calculatesOnBudgetAndOnScheduleStatuses() {
        EvmMetrics metrics = service.calculateForActivity(activity("100", "50", "50", "50", null));

        assertEquals(bd("1.0000"), metrics.cpi());
        assertEquals(bd("1.0000"), metrics.spi());
        assertEquals(bd("100.0000"), metrics.eac());
        assertEquals(bd("0.0000"), metrics.vac());
        assertEquals(CostStatus.ON_BUDGET, metrics.costStatus());
        assertEquals(ScheduleStatus.ON_SCHEDULE, metrics.scheduleStatus());
    }

    @Test
    void returnsNoCostAndScheduleDataWhenDivisorsAreZero() {
        EvmMetrics metrics = service.calculateForActivity(activity("100", "0", "0", "0", null));

        assertNull(metrics.cpi());
        assertNull(metrics.spi());
        assertNull(metrics.eac());
        assertNull(metrics.vac());
        assertEquals(CostStatus.NO_COST_DATA, metrics.costStatus());
        assertEquals(ScheduleStatus.NO_SCHEDULE_DATA, metrics.scheduleStatus());
    }

    @Test
    void returnsNoEstimateWhenCpiIsZero() {
        EvmMetrics metrics = service.calculateForActivity(activity("100", "50", "0", "50", null));

        assertEquals(bd("0.0000"), metrics.cpi());
        assertNull(metrics.eac());
        assertNull(metrics.vac());
        assertEquals(CostStatus.OVER_BUDGET, metrics.costStatus());
    }

    @Test
    void treatsNullActivityValuesAsZero() {
        Activity activity = new Activity(UUID.randomUUID(), UUID.randomUUID(), "Null values", null, null, null,
                null, null, true, null, null);

        EvmMetrics metrics = service.calculateForActivity(activity);

        assertEquals(bd("0.0000"), metrics.plannedValue());
        assertEquals(bd("0.0000"), metrics.earnedValue());
        assertEquals(bd("0.0000"), metrics.costVariance());
        assertEquals(bd("0.0000"), metrics.scheduleVariance());
        assertEquals(CostStatus.NO_COST_DATA, metrics.costStatus());
        assertEquals(ScheduleStatus.NO_SCHEDULE_DATA, metrics.scheduleStatus());
    }

    @Test
    void returnsZeroMetricsForNullOrEmptyConsolidatedActivities() {
        EvmMetrics nullResult = service.calculateConsolidated(null);
        EvmMetrics emptyResult = service.calculateConsolidated(List.of());

        assertEquals(nullResult, emptyResult);
        assertEquals(bd("0.0000"), nullResult.plannedValue());
        assertEquals(bd("0.0000"), nullResult.earnedValue());
        assertEquals(CostStatus.NO_COST_DATA, nullResult.costStatus());
        assertEquals(ScheduleStatus.NO_SCHEDULE_DATA, nullResult.scheduleStatus());
    }

    @Test
    void consolidatesExistingAndCalculatedActivityMetrics() {
        EvmMetrics existingMetrics = new EvmMetrics(bd("20"), bd("24"), bd("16"), bd("0"), bd("3"),
                bd("1.2"), bd("13.3333"), bd("26.6667"), CostStatus.UNDER_BUDGET,
                ScheduleStatus.AHEAD_OF_SCHEDULE);
        Activity withMetrics = activity("40", "50", "60", "8", existingMetrics);
        Activity withoutMetrics = activity("60", "50", "50", "30", null);

        EvmMetrics consolidated = service.calculateConsolidated(List.of(withMetrics, withoutMetrics));

        assertEquals(bd("50.0000"), consolidated.plannedValue());
        assertEquals(bd("54.0000"), consolidated.earnedValue());
        assertEquals(bd("16.0000"), consolidated.costVariance());
        assertEquals(bd("4.0000"), consolidated.scheduleVariance());
        assertEquals(bd("1.4211"), consolidated.cpi());
        assertEquals(bd("1.0800"), consolidated.spi());
        assertEquals(CostStatus.UNDER_BUDGET, consolidated.costStatus());
        assertEquals(ScheduleStatus.AHEAD_OF_SCHEDULE, consolidated.scheduleStatus());
    }

    private Activity activity(String budget, String planned, String completed, String actualCost, EvmMetrics metrics) {
        return new Activity(UUID.randomUUID(), UUID.randomUUID(), "Activity", bd(budget), bd(planned), bd(completed),
                bd(actualCost), metrics, true, null, null);
    }

    private BigDecimal bd(String value) {
        return new BigDecimal(value);
    }
}