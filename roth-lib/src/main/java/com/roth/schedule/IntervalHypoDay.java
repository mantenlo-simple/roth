package com.roth.schedule;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import com.roth.base.util.Data;

public class IntervalHypoDay {
	private static final String DASH = "-";
	private static final String MIDNIGHT = "00:00:00";
	
	private IntervalHypoDay() {}
	
	/**
	 * Ensure that the time range limit given to a second, minute, or hour is valid.
	 * @param source
	 * @return
	 */
	private static String validateTimeRange(String source) {
		String result = MIDNIGHT + DASH + MIDNIGHT;
		if (source == null)
			return result;
		boolean hasDash = source.contains(DASH);
		boolean validLength = Data.in(source.length(), new Integer[] {9, 17});
		if (!hasDash || !validLength)
			throw new IllegalArgumentException("Invalid time range format.  Must be format 'HH:mm:ss-HH:mm:ss', 'HH:mm:ss-' or '-HH:mm:ss'.");
		if (source.startsWith(DASH))
			result = MIDNIGHT + source;
		else if (source.endsWith(DASH))
			result = source + MIDNIGHT;
		else if (source.length() == 17)
			result = source;
		return result;
	}
	
	/** 
	 * Convert time string to a normalized date.
	 * @param time
	 * @param origin
	 * @param end
	 * @return
	 */
	private static LocalDateTime toTime(String time, LocalDateTime origin, boolean bumpMidnight) {
		LocalDateTime result = origin;
		String[] segs = time.split(":");
		result = result.truncatedTo(ChronoUnit.DAYS).plusHours(Data.strToInteger(segs[0])).plusMinutes(Data.strToInteger(segs[1])).plusSeconds(Data.strToInteger(segs[1]));
		return (bumpMidnight && time.equals(MIDNIGHT)) ? result.plusDays(1) : result;
	}
	
	/**
	 * Calculate the next time based on ChroniUnit.SECONDS/.MINUTES/.HOURS.
	 * @param last
	 * @param period
	 * @param count
	 * @param modifier Limits the time of day that scheduled instances can occur.  Format: "00:00:00-00:00:00"
	 * @return
	 */
	public static LocalDateTime next(LocalDateTime last, ChronoUnit period, int count, String modifier) {
		if (!Data.in(period, new ChronoUnit[] {ChronoUnit.SECONDS, ChronoUnit.MINUTES, ChronoUnit.HOURS}))
			throw new IllegalArgumentException("Invalid period.  Must be SECONDS, MINUTES, or HOURS.");
		if (count < 1)
			throw new IllegalArgumentException("Invalid count.  Must be > 0.");
		LocalDateTime result = last.plus(count, period);
		String[] timeRange = validateTimeRange(modifier).split("-");
		LocalDateTime start = toTime(timeRange[0], last, false);
		LocalDateTime end = toTime(timeRange[1], last, true);
		if (start.compareTo(end) < 0) { 
			if (result.compareTo(start) < 0)
				result = start;
			else if (result.compareTo(end) > 0)
				result = start.plusDays(1);
		}
		else { 
			boolean incEnd = result.compareTo(end) > 0;
			if (incEnd)
				end = end.plusDays(1);
			if (result.compareTo(start) < 0 && incEnd)
				result = start;
			else if (result.compareTo(end) > 0)
				result = start.plusDays(1);
		}
		return result;
	}
}
