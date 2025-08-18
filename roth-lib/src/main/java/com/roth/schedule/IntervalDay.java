package com.roth.schedule;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import com.roth.base.util.Data;

public class IntervalDay {
	private static final String DAY_MODIFIER_REGEX = "[-|S][-|M][-|T][-|W][-|T][-|F][-|S]";
	private static final String INVALID_DAY_MODIFIER = "Invalid modifier.  A day modifier must be a pattern of weekdays.  "
			+ "The pattern must follow: SMTWTFS, where each letter represents a weekday between Sunday and Saturday.  "
			+ "If the letter is present in that ordinal position, then that weekday is valid; else if a dash ('-') is"
			+ "present in that ordinal position, then that weekday is not valid and will be skipped.";

	private IntervalDay() {}
	
	private static int adjustWeekday(int weekday) {
		return weekday < 7 ? weekday : 0;
	}
	
	/**
	 * Calculate the next time based on ChroniUnit.DAYS.  Modifier is ignored if count > 1.
	 * @param last
	 * @param period
	 * @param count
	 * @param modifier Limits the days of week that the scheduled instance can occur.  Format "SMTWTFS"
	 *                 If the letter of the day is there, then the day is valid, else if the letter is
	 *                 replace by a dash "-", then the day is not valid.  At least one weekday must be
	 *                 valid. 
	 * @return
	 */
	public static LocalDateTime next(LocalDateTime last, ChronoUnit period, int count, String modifier) {
		if (!Data.in(period, new ChronoUnit[] {ChronoUnit.DAYS}))
			throw new IllegalArgumentException("Invalid period.  Must be DAYS.");
		if (count < 1)
			throw new IllegalArgumentException("Invalid count.  Must be > 0.");
		LocalDateTime result = last.plus(count, period);
		if (count == 1 && modifier != null && !modifier.equals("-------")) {
			if (modifier.matches(DAY_MODIFIER_REGEX)) {
				int c = 0;
				int wd = adjustWeekday(result.getDayOfWeek().getValue());
				while(modifier.charAt(wd) == '-' && c < 7) {
					result = result.plus(1, period);
					c++;
				}
			}
			else
				throw new IllegalArgumentException(INVALID_DAY_MODIFIER);
		}
		return result;
	}
}
