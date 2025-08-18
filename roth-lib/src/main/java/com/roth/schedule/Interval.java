package com.roth.schedule;

import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import com.roth.base.util.Data;

public class Interval {
	private static final String INVALID_COUNT = "Invalid count.  Must be > 0.";
	private static final String INVALID_PERIOD = "Invalid period.  Must be MONTHS or YEARS.";
	
	private Interval() {}
	
	/**
	 * Calculates the next date-time in a schedule relative to the last date-time of the schedule.<br/>
	 * Rules:<br/>
	 * <ul>
	 *     <li>
	 *         All units require a count of 1 or more.
	 *     </li>
	 *     <li>
	 *         When period is ChronoUnit.SECONDS/MINUTES/HOURS
	 *         <ul>
	 *             <li>
	 *                 If the modifier is empty, then the interval is the count of seconds, minutes, or hours.
	 *             </li>
	 *             <li>
	 *                 When not empty, the modifier defines the valid time range during the day for scheduling intervals.
	 *                 For example "06:30:00-18:30:00" means that intervals are only valid between 6:30 am and 6:30 pm, 
	 *                 and "18:30:00-06:30:00" means that intervals are only valid between 6:30 pm and 6:30 am.
	 *             </li>
	 *         </ul>
	 *     </li>
	 *     <li>
	 *         When period is ChronoUnit.DAYS
	 *         <ul>
	 *             <li>
	 *                 If the modifier is empty, then the interval is the count of days.
	 *             </li>
	 *             <li>
	 *                 If the count is > 1, then the modifier is ignored even if not empty.
	 *             </li>
	 *             <li>
	 *                 When not empty, the modifier defines the valid days of the week represented by the first letter
	 *                 of each valid day, or a dash to indicate that intervals are not valid on that day.  For example,
	 *                 "SMTWTFS" means that intervals are valid every day of the week, and '-M-W-F-" means that intervals
	 *                 are valid on Mondays, Wednesdays, and Fridays, but not on Sundays, Tuesdays, Thursdays, and Saturdays.
	 *                 Please note that these are positional, and there must be 7 characters, for example "MWF" is not a
	 *                 valid modifier.
	 *             </li>
	 *         </ul>
	 *     </li>
	 *     <li>
	 *         When period is ChronoUnit.MONTHS/YEARS
	 *         <ul>
	 *             <li>
	 *                 If the modifier is empty, then the interval is the count of months or years.
	 *             </li>
	 *             <li>
	 *                 When not empty, the modifier defines one of two things: a day of  month, or an ordinal weekday of month.
	 *                 Note that count may still be > 1.  In this case the modifier applies after the count is applied.
	 *                 <ul>
	 *                     <li>
	 *                         For day of month, valid values include 1 to 28 or -7 to -1.  Positive numbers represent that day of month, 
	 *                         while negative numbers represent days relative to the end of the month (-1 = last day, -2 = 2nd to last day, and so on).<br/>
	 *                         Note about count: If count = 2 and modifier = "15", then the valid interval is the 15th of every other month.
	 *                     </li>
	 *                     <li>
	 *                         For ordinal weekday of month, valid values include 1 to 5 or -2 to -1, followed by the two-character 
	 *                         representation of the days of the week ("SU", "MO", "TU", "WE", "TH", "FR", or "SA").  For example
	 *                         "4TH" means the 4th Thursday of the month, and -1FR means the last Friday of the month.<br/>
	 *                         Note about count: If count = 12 and modifier = "4TH" and the last reference was in November, then
	 *                         the valid interval is Thanksgiving Day in the USA.
	 *                     </li>
	 *                 </ul>
	 *             </li>
	 *         </ul>
	 *     </li>
	 * </ul>
	 * 
	 * @param last
	 * @param period
	 * @param count
	 * @param modifier
	 * @return
	 * @throws ParseException
	 */
	public static LocalDateTime next(LocalDateTime last, ChronoUnit period, int count, String modifier) throws ParseException {
		if (count < 1)
			throw new IllegalArgumentException(INVALID_COUNT);
		if (period == ChronoUnit.DAYS)
			return nextDay(last, period, count, modifier);
		if (Data.in(period, new ChronoUnit[] {ChronoUnit.MONTHS, ChronoUnit.YEARS}))
			return nextHyperDay(last, period, count, modifier);
		if (Data.in(period, new ChronoUnit[] {ChronoUnit.SECONDS, ChronoUnit.MINUTES, ChronoUnit.HOURS}))
			return nextHypoDay(last, period, count, modifier);
		throw new IllegalArgumentException(INVALID_PERIOD);
	}
	
	
	// Day
	private static final String DAY_MODIFIER_REGEX = "[-|S][-|M][-|T][-|W][-|T][-|F][-|S]";
	private static final String INVALID_DAY_MODIFIER = "Invalid modifier.  A day modifier must be a pattern of weekdays.  "
			+ "The pattern must follow: SMTWTFS, where each letter represents a weekday between Sunday and Saturday.  "
			+ "If the letter is present in that ordinal position, then that weekday is valid; else if a dash ('-') is"
			+ "present in that ordinal position, then that weekday is not valid and will be skipped.";

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
	private static LocalDateTime nextDay(LocalDateTime last, ChronoUnit period, int count, String modifier) {
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
	
	// Hyper Day (i.e., bigger than a day -- months)
	
	private static final String ORD_MODIFIER_REGEX = "-{0,1}[0-9](SU|MO|TU|WE|TH|FR|SA)";
	private static final String[] WEEKDAYS = {"SU", "MO", "TU", "WE", "TH", "FR", "SA"};
	private static final String INVALID_DOM_MODIFIER = "Invalid modifier.  A day of month modifier must be between 1 to 29, or -1 to -7.";
	private static final String INVALID_ORD_MODIFIER = "Invalid modifier.  An ordinal weekday must be a number 1 to 5 or -1 to -2, "
			+ "followed by a two character weekday ('SU', 'MO', 'TU', 'WE', 'TH', 'FR', or 'SA').";
	
	private static LocalDateTime calcOrdWeekdayEvent(LocalDateTime source, String modifier) throws ParseException {
		String ors = modifier.substring(0, modifier.length() - 2);
		int ord = Data.strToInteger(ors);
		int wd = Data.indexOf(modifier.substring(modifier.length() - 2), WEEKDAYS);
		if (ord == 0 || ord > 5 || ord < -4 || wd < 0)
			throw new IllegalArgumentException(INVALID_ORD_MODIFIER);
		LocalDateTime result = source;
		if (ord > 0) {
			int wdf = Data.weekday(Data.expToDts("F", result));
			int off = wd - wdf;
			if (off < 0) off += 7;
			off += (ord - 1) * 7;
			result = Data.expToDts("F+" + off + "D", result);
		}
		else {
			int wdl = Data.weekday(Data.expToDts("F+1M-1D", result));
			int off = wdl - wd;
			if (off < 0) off += 7;
			off += (Math.abs(ord) - 1) * 7;
			result = Data.expToDts("F+1M-1D-" + off + "D", result);
		}
		return result.withHour(source.getHour()).withMinute(source.getMinute()).withSecond(source.getSecond());
	}
	
	private static LocalDateTime nextHyperDay(LocalDateTime last, ChronoUnit period, int count, String modifier) throws ParseException{
		if (!Data.in(period, new ChronoUnit[] {ChronoUnit.MONTHS, ChronoUnit.YEARS}))
			throw new IllegalArgumentException(INVALID_PERIOD);
		if (count < 1)
			throw new IllegalArgumentException(INVALID_COUNT);
		LocalDateTime result = last.plus(count, period);
		if (period == ChronoUnit.MONTHS && modifier != null) {
			// Modifier specifies day of month
			if (Data.isNumeric(modifier)) {
				int dom = Data.strToInteger(modifier);
				if (dom > 0 && dom < 29)
					result = result.withDayOfMonth(dom);
				else if (dom > -8 && dom < 0)
					result = result.withDayOfMonth(1).plusMonths(1).minusDays(dom);
				else
					throw new IllegalArgumentException(INVALID_DOM_MODIFIER);
			}
			// Modifier specifies ordinal weekday
			else if (modifier.matches(ORD_MODIFIER_REGEX))
				result = calcOrdWeekdayEvent(result, modifier);
			// Modifier is invalid
			else
				throw new IllegalArgumentException(INVALID_ORD_MODIFIER);
		}
		return result;
	}
	
	// Hypoday (i.e., smaller than a day -- hours, minutes, seconds)
	
	private static final String DASH = "-";
	private static final String MIDNIGHT = "00:00:00";
	
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
	private static LocalDateTime nextHypoDay(LocalDateTime last, ChronoUnit period, int count, String modifier) {
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
