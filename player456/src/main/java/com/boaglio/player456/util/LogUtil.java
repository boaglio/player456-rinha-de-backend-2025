package com.boaglio.player456.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogUtil {

    private static final Logger logger = LoggerFactory.getLogger(LogUtil.class);

    public static final boolean DEBUG = true;
    public static final boolean ERROR = false;
    public static final boolean ERROR_BOTH = false;
    public static long errorCount = 1;
    public static long bothErrorCount = 1;

    public static void log(String message) {
        if (DEBUG) {
            logger.info("Thread:%s - %s".formatted(Thread.currentThread().getName(),message));
        }
    }

    public static void logError(String message) {
        if (ERROR) {
            logger.info("%d - DEFAULT SERVER ERROR - Thread:%s - %s".formatted(errorCount++,Thread.currentThread().getName(),message));
        }
    }

    public static void logErrorBothServers(String message) {
        if (ERROR_BOTH) {
            logger.info("%d - ERROR BOTH SERVERS - Thread:%s - %s".formatted(bothErrorCount++,Thread.currentThread().getName(),message));
        }
    }

}
