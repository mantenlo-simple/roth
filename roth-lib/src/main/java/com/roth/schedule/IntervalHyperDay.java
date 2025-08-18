package com.roth.schedule;

import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import com.roth.base.util.Data;

public class IntervalHyperDay {
	private static final String ORD_MODIFIER_REGEX = "-{0,1}[0-9](SU|MO|TU|WE|TH|FR|SA)";
	private static final String[] WEEKDAYS = {"SU", "MO", "TU", "WE", "TH", "FR", "SA"};
	private static final String INVALID_PERIOD = "Invalid period.  Must be MONTHS or YEARS.";
	private static final String INVALID_COUNT = "Invalid count.  Must be > 0.";
	private static final String INVALID_DOM_MODIFIER = "Invalid modifier.  A day of month modifier must be between 1 to 29, or -1 to -7.";
	private static final String INVALID_ORD_MODIFIER = "Invalid modifier.  An ordinal weekday must be a number 1 to 5 or -1 to -2, "
			+ "followed by a two character weekday ('SU', 'MO', 'TU', 'WE', 'TH', 'FR', or 'SA').";
	
	private IntervalHyperDay() {}
	
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
	
	public static LocalDateTime next(LocalDateTime last, ChronoUnit period, int count, String modifier) throws ParseException{
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
}
