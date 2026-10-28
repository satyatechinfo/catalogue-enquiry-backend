package com.main.util;

import com.main.exception.BadRequestException;

import java.math.BigDecimal;


public enum BudgetBand {
    UNDER_500("under-500", null, new BigDecimal("500")),
    FROM_500_TO_1500("500-1500", new BigDecimal("500"), new BigDecimal("1500")),
    FROM_1500_TO_3000("1500-3000", new BigDecimal("1500"), new BigDecimal("3000")),
    ABOVE_3000("above-3000", new BigDecimal("3000"), null);

    private final String code;
    private final BigDecimal lower;
    private final BigDecimal upper;

    BudgetBand(String code, BigDecimal lower, BigDecimal upper) {
        this.code = code;
        this.lower = lower;
        this.upper = upper;
    }

    /** Inclusive lower bound, or null when open ended. */
    public BigDecimal lower() { return lower; }

    /** Inclusive upper bound, or null when open ended. */
    public BigDecimal upper() { return upper; }

    public static BudgetBand fromCode(String code) {
        for (BudgetBand band : values()) {
            if (band.code.equalsIgnoreCase(code)) {
                return band;
            }
        }
        throw new BadRequestException("Unknown budget band '" + code
                + "'. Use one of: under-500, 500-1500, 1500-3000, above-3000.");
    }
}
