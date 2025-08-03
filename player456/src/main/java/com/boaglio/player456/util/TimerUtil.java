package com.boaglio.player456.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class TimerUtil {
    private static final Logger logger = LoggerFactory.getLogger(TimerUtil.class);
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.###", new DecimalFormatSymbols(Locale.US));
    private final long startTime;

    public TimerUtil() {
        this.startTime = System.nanoTime();
    }

    public void logElapsedTime(String operation) {
        long endTime = System.nanoTime();
        double elapsedMillis = (endTime - startTime) / 1_000_000.0;
        logger.info("{} took {} ms", operation, DECIMAL_FORMAT.format(elapsedMillis));
    }
}