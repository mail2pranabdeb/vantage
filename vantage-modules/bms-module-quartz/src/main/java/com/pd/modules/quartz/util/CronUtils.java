package com.pd.modules.quartz.util;

import java.text.ParseException;
import java.time.LocalDateTime;
import org.quartz.CronExpression;

/**
 * Cron expression utility class
 */
public class CronUtils {

    /**
     * Check if cron expression is valid
     */
    public static boolean isValid(String cronExpression) {
        return CronExpression.isValidExpression(cronExpression);
    }

    /**
     * Get next execution time
     */
    public static LocalDateTime getNextExecution(String cronExpression) {
        if (!isValid(cronExpression)) {
            return null;
        }
        try {
            CronExpression cron = new CronExpression(cronExpression);
            java.util.Date nextFireTime = cron.getNextValidTimeAfter(new java.util.Date());
            if (nextFireTime != null) {
                return LocalDateTime.ofInstant(nextFireTime.toInstant(), java.time.ZoneId.systemDefault());
            }
        } catch (ParseException e) {
            // ignore
        }
        return null;
    }
}
