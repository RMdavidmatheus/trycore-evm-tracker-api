package com.business.trycoreevmtrackerapi.application.service;

import com.business.trycoreevmtrackerapi.domain.model.Activity;
import com.business.trycoreevmtrackerapi.domain.model.CostStatus;
import com.business.trycoreevmtrackerapi.domain.model.EvmMetrics;
import com.business.trycoreevmtrackerapi.domain.model.ScheduleStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class EvmCalculatorService {

    private static final BigDecimal HUNDRED = new BigDecimal("100");
    private static final int INTERMEDIATE_SCALE = 8;
    private static final int RESULT_SCALE = 4;

    public EvmMetrics calculateForActivity(Activity activity) {
        BigDecimal bac = sanitize(activity.plannedBudget());
        BigDecimal plannedRatio = toRatio(sanitize(activity.plannedPercentage()));
        BigDecimal completedRatio = toRatio(sanitize(activity.completedPercentage()));
        BigDecimal ac = sanitize(activity.actualCost());

        BigDecimal pv = bac.multiply(plannedRatio).setScale(RESULT_SCALE, RoundingMode.HALF_UP);
        BigDecimal ev = bac.multiply(completedRatio).setScale(RESULT_SCALE, RoundingMode.HALF_UP);

        return buildMetrics(bac, pv, ev, ac);
    }

    public EvmMetrics calculateConsolidated(List<Activity> activities) {
        if (activities == null || activities.isEmpty()) {
            return buildMetrics(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
        }

        BigDecimal totalBac = BigDecimal.ZERO;
        BigDecimal totalPv = BigDecimal.ZERO;
        BigDecimal totalEv = BigDecimal.ZERO;
        BigDecimal totalAc = BigDecimal.ZERO;

        for (Activity activity : activities) {
            BigDecimal bac = sanitize(activity.plannedBudget());
            BigDecimal ac = sanitize(activity.actualCost());
            EvmMetrics metrics = activity.metrics() != null
                    ? activity.metrics()
                    : calculateForActivity(activity);

            totalBac = totalBac.add(bac);
            totalPv = totalPv.add(metrics.plannedValue());
            totalEv = totalEv.add(metrics.earnedValue());
            totalAc = totalAc.add(ac);
        }

        return buildMetrics(
                totalBac.setScale(RESULT_SCALE, RoundingMode.HALF_UP),
                totalPv.setScale(RESULT_SCALE, RoundingMode.HALF_UP),
                totalEv.setScale(RESULT_SCALE, RoundingMode.HALF_UP),
                totalAc.setScale(RESULT_SCALE, RoundingMode.HALF_UP)
        );
    }

    private EvmMetrics buildMetrics(BigDecimal bac, BigDecimal pv, BigDecimal ev, BigDecimal ac) {
        BigDecimal cv = ev.subtract(ac).setScale(RESULT_SCALE, RoundingMode.HALF_UP);
        BigDecimal sv = ev.subtract(pv).setScale(RESULT_SCALE, RoundingMode.HALF_UP);

        BigDecimal cpi = calculateCpi(ev, ac);
        BigDecimal spi = calculateSpi(ev, pv);
        BigDecimal eac = calculateEac(bac, cpi);
        BigDecimal vac = calculateVac(bac, eac);

        CostStatus costStatus = evaluateCostStatus(cpi);
        ScheduleStatus scheduleStatus = evaluateScheduleStatus(spi);

        return new EvmMetrics(
                pv.setScale(RESULT_SCALE, RoundingMode.HALF_UP),
                ev.setScale(RESULT_SCALE, RoundingMode.HALF_UP),
                cv,
                sv,
                cpi,
                spi,
                eac,
                vac,
                costStatus,
                scheduleStatus
        );
    }

    private BigDecimal calculateCpi(BigDecimal ev, BigDecimal ac) {
        if (isZero(ac)) {
            return null;
        }
        return ev.divide(ac, RESULT_SCALE, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateSpi(BigDecimal ev, BigDecimal pv) {
        if (isZero(pv)) {
            return null;
        }
        return ev.divide(pv, RESULT_SCALE, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateEac(BigDecimal bac, BigDecimal cpi) {
        if (cpi == null || isZero(cpi)) {
            return null;
        }
        return bac.divide(cpi, RESULT_SCALE, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateVac(BigDecimal bac, BigDecimal eac) {
        if (eac == null) {
            return null;
        }
        return bac.subtract(eac).setScale(RESULT_SCALE, RoundingMode.HALF_UP);
    }

    private CostStatus evaluateCostStatus(BigDecimal cpi) {
        if (cpi == null) {
            return CostStatus.NO_COST_DATA;
        }
        int comparison = cpi.compareTo(BigDecimal.ONE);
        if (comparison > 0) {
            return CostStatus.UNDER_BUDGET;
        }
        if (comparison < 0) {
            return CostStatus.OVER_BUDGET;
        }
        return CostStatus.ON_BUDGET;
    }

    private ScheduleStatus evaluateScheduleStatus(BigDecimal spi) {
        if (spi == null) {
            return ScheduleStatus.NO_SCHEDULE_DATA;
        }
        int comparison = spi.compareTo(BigDecimal.ONE);
        if (comparison > 0) {
            return ScheduleStatus.AHEAD_OF_SCHEDULE;
        }
        if (comparison < 0) {
            return ScheduleStatus.BEHIND_SCHEDULE;
        }
        return ScheduleStatus.ON_SCHEDULE;
    }

    private BigDecimal toRatio(BigDecimal percentage) {
        return percentage.divide(HUNDRED, INTERMEDIATE_SCALE, RoundingMode.HALF_UP);
    }

    private BigDecimal sanitize(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private boolean isZero(BigDecimal value) {
        return value.compareTo(BigDecimal.ZERO) == 0;
    }
}
