package com.roth.schedule.util;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.roth.base.log.Log;
import com.roth.base.util.Data;
import com.roth.jdbc.annotation.ConnectionDataSource;
import com.roth.jdbc.annotation.SQLFileContext;
import com.roth.jdbc.util.TableUtil;
import com.roth.schedule.model.ScheduleBean;
import com.roth.schedule.model.ScheduleEventBean;

@ConnectionDataSource(jndiName = "roth")
@SQLFileContext(path = "/com/roth/schedule/sql")
public class ScheduleUtil extends TableUtil {
	private static final long serialVersionUID = 748138370073531600L;

	/***   OLD CODE BEGIN   ***/
	public ScheduleUtil() throws SQLException {
		super();
	}

	public List<ScheduleBean> getPendingSchedules() throws SQLException {
		String statement = getSQLFile("getPendingSchedules.sql");
		return execQuery(statement, ArrayList.class, ScheduleBean.class);
	}
	
	public List<ScheduleEventBean> getPendingEvents() throws SQLException {
		String statement = getSQLFile("getPendingEvents.sql");
		return execQuery(statement, ArrayList.class, ScheduleEventBean.class);
	}
	
	
	//public static getNextRunTime(LocalDateTime lastRunTime, w)
	
	/***   OLD CODE END   ***/
	
	
	/**
	 * Get tasks for the specified hostname.  This presupposes that when a new task
	 * is created, then the first history item is created at the same time.  This
	 * also presupposes that when a task [history] item is executed, a new history
	 * item is created at the same time.
	 * @param hostname
	 * @return
	 * @throws SQLException
	 */
	public List<ScheduleEventBean> getScheduleEvents(String hostname) throws SQLException {
		String statement = """
				SELECT h.*
				  FROM schedule s
				  JOIN schedule_event e
				    ON e.schedule_id = s.schedule_id
				 WHERE e.run_dts <= SYSDATE()
				   AND s.node_name = {1}
				   AND s.status = 'A'
				   AND (s.end_dts IS NULL
				    OR  SYSDATE() < s.end_dts)
				   AND (e.status = 'P'
				    OR  (e.status = 'E'
				   AND   e.retries < s.retry_limit))
				""";
		statement = applyParameters(statement, hostname);
		return execQuery(statement, ArrayList.class, ScheduleEventBean.class);
	}
	
	// RegEx Strings
	private static final String COMPARE_DAY_FORMAT = "(^(S|-)(M|-)(T|-)(W|-)(T|-)(F|-)(S|-)$)|(^(S|-)(M|-)(T|-)(W|-)(T|-)(F|-)(S|-),(S|-)(M|-)(T|-)(W|-)(T|-)(F|-)(S|-)$)";
	private static final String COMPARE_DAY_INVALID = "(^-------$)|(^-------,-------$)";
	private static final String COMPARE_MONTH_FORMAT = "(^[1-9]$)|(^[1-3][0-9]$)|(^-[1-7]$)|(^[1-5](SU|MO|TU|WE|TH|FR|SA)$)|(^-[1-5](SU|MO|TU|WE|TH|FR|SA)$)";
	private static final String COMPARE_MONTH_INVALID = "(^3[2-9]$)";
	
	// Validation and Calculation Result Records
	public record ValidateResult(boolean valid, Integer ruleIndex) {}
	public record CalcResult(LocalDateTime nextEventDts, Integer ruleIndex) {}
	
	// === Support Methods ===
	/**
	 * Adjust day of week (dow) so that Sunday is 0 instead of 7.
	 * @param dow
	 * @return
	 */
	private static int adjustDow(int dow) { 
		return dow < 7 ? dow : 0; 
	}
	
	// === Calculation Methods ===
	/**
	 * Calculate the next run datetime from the task definition and lastRun instance.<br/>
	 * Note: If the lastRun datetime is the initial "run", then it may or may not actually
	 * qualify for the definition.  Use the validateDts method to see if the datetime is valid.
	 * @param schedule
	 * @param lastRun
	 * @return
	 */
	public static CalcResult calcNextRun(ScheduleBean schedule, ScheduleEventBean lastRun) {
		return switch (schedule.getIntervalType()) {
		case "m", "h" -> calcNextTimeRun(schedule, lastRun);
		case "D" -> calcNextDayRun(schedule, lastRun);
		case "M" -> calcNextMonthRun(schedule, lastRun);
		default -> null;
		};
	}
	
	/**
	 * "m" (minute) and "h" (hour) calculations only use the interval and intervalCount.
	 * @param lastRun
	 * @param schedule
	 * @return
	 */
	private static CalcResult calcNextTimeRun(ScheduleBean schedule, ScheduleEventBean lastRun) {
		CalcResult result = null;
		LocalDateTime now = LocalDateTime.now();
		do {
			result = switch (schedule.getIntervalType()) {
			case "m" -> new CalcResult(lastRun.getEventDts().plusMinutes(schedule.getIntervalCount()), null);
			case "h" -> new CalcResult(lastRun.getEventDts().plusHours(schedule.getIntervalCount()), null);
			default -> null;
			};
		} while (result != null && "S".equals(schedule.getCatchupMethod()) && result.nextEventDts.compareTo(now) < 0);
		return result;
	}
	
	/**
	 * "D" (day) calculations use the intervalCount if intervalRules is null, else
	 * intervalCount will be ignored.  The day rule is:
	 * SMTWTFS - Letters representing the day of the week.  If a dash appears, then
	 * that day is not wanted.  Example: -M-W-F- means that the desired days are Monday,
	 * Wednesday, and Friday.
	 * If intervalRules contains two weeks' worth separated by a comma (i.e., SMTWTFS,SMTWTFS) 
	 * then the calculations interpret alternating weeks.  Example -M-W-F-,--T-T-- means that
	 * the desired days are Monday, Wednesday, and Friday in week 1, and Tuesday and 
	 * Thursday in week 2.
	 * @param lastRun
	 * @param schedule
	 * @return
	 */
	private static CalcResult calcNextDayRun(ScheduleBean schedule, ScheduleEventBean lastRun) {
		// If there's no rules, then just increment by the intervalCount.
		if (schedule.getRules() == null)
			return new CalcResult(lastRun.getEventDts().plusDays(schedule.getIntervalCount()), null);
		// We don't care about the return value of validateDtsDay, we're calling it here to check rule formats.
		validateDtsDay(schedule, lastRun.getEventDts()); 
		// Process rules to calculate the proper offset for incrementing.
		String[] rules = schedule.getRules().split(",");
		int ruleIndex = Data.nvl(lastRun.getRuleIndex(), 0);
		int offset = 1;
		LocalDateTime checkDts = lastRun.getEventDts().plusDays(offset);
		LocalDateTime now = LocalDateTime.now();
		while (!checkDow(rules[ruleIndex], checkDts) || ("S".equals(schedule.getCatchupMethod()) && checkDts.compareTo(now) < 0)) {
			offset++;
			checkDts = lastRun.getEventDts().plusDays(offset);
			int cdow = adjustDow(checkDts.getDayOfWeek().getValue());
			if (cdow == 0 && rules.length > 1)
				ruleIndex = toggle(ruleIndex); 
		}
		return new CalcResult(checkDts, ruleIndex);
	}
	
	/**
	 * "M" (month) calculations use the intervalCount and intervalRules.  If the intervalCount is
	 * greater than 1, then the rules will only apply every x months.  The month rules are:<br/>
	 * Day of Month<br/>
	 * DD - A positive one or two digit number representing the day of the month.  If the number 
	 * exceeds the actual last day of the month then it will revert to the last day of the month.
	 * -D - A negative one digit number representing the day of the month relative to the last day.
	 * For example: -1 means the last day of the month, -2 means the second to last, and so on.<br/>
	 * Ordinal Weekday<br/>
	 * OWW - A positive one digit number followed by a two character day of week (e.g., SU, MO, TU, WE, TH, FR, SA).
	 * This indicates the ordinal week day.  For example: 4TH means the 4th Thursday of the month.
	 * -OWW - A negative one digit number followed by a two character day of the week.
	 * This indicates the ordinal week day relative to the end of the month.  For example: -1FR means
	 * the last Friday of the month.
	 * For the ordinal weekday rules, if the calculated date does not fall within the expected month,
	 * then a null is returned.
	 * Multiple rules may exist, separated by commas.  If so, then the next rule that is applicable 
	 * for a given calculation is applied.  
	 * @param lastRun
	 * @param schedule
	 * @return
	 */
	private static CalcResult calcNextMonthRun(ScheduleBean schedule, ScheduleEventBean lastRun) {
		if (schedule.getRules() == null)
			return new CalcResult(lastRun.getEventDts().plusMonths(schedule.getIntervalCount()), null);
		String[] rules = schedule.getRules().split(",");
		// Start with the next day (within a qualifying month) after the last run
		LocalDateTime checkDts = nextRunDts(schedule, lastRun.getEventDts());
		LocalDateTime now = LocalDateTime.now();
		boolean found = false;
		// Loop until you hit a day that meets one of the rules 
		while (!found) {
			for (int i = 0; i < rules.length; i++) {
				String rule = rules[i];
				if (checkMonthRule(rule, checkDts) && ("A".equals(schedule.getCatchupMethod()) || checkDts.compareTo(now) >= 0))
					return new CalcResult(checkDts, i);
			}
			checkDts = nextRunDts(schedule, checkDts);
		}
		return null; // <-- This line should never actually execute; it's there to satisfy the compiler.
	}
	
	/**
	 * Check the dow of the checkDts against the rule to see if it applies.
	 * @param rule
	 * @param checkDts
	 * @return
	 */
	private static boolean checkDow(String rule, LocalDateTime checkDts) {
		int cdow = adjustDow(checkDts.getDayOfWeek().getValue());
		return rule.charAt(cdow) != '-';
	}
	
	/**
	 * Check the checkDts against the rule to see if it applies.
	 * @param rule
	 * @param checkDts
	 * @return
	 */
	private static boolean checkMonthRule(String rule, LocalDateTime checkDts) {
		if (Data.isNumeric(rule)) {
			int day = Data.strToInteger(rule);
			if (day > 0)
				return checkDts.getDayOfMonth() == day;
			else
				return checkDts.getDayOfMonth() == checkDts.plusMonths(1).withDayOfMonth(1).plusDays(day).getDayOfMonth();
		} 
		else {
			int ord = Data.strToInteger(rule.substring(0, rule.length() - 2));
			int dow = Data.indexOf(rule.substring(rule.length() - 2), new String[] { "SU", "MO", "TU", "WE", "TH", "FR", "SA" });
			if (ord > 0) {
				int cdow = adjustDow(checkDts.getDayOfWeek().getValue());
				int cord = ((checkDts.getDayOfMonth() - 1) / 7) + 1;
				return (dow == cdow) && (ord == cord);
			}
			else {
				int cdow = adjustDow(checkDts.getDayOfWeek().getValue());
				int eom = checkDts.plusMonths(1).withDayOfMonth(1).minusDays(1).getDayOfMonth();
				int cord = ((eom - checkDts.getDayOfMonth()) / 7) + 1;
				return (dow == cdow) && (ord == -cord);
			}
		}
	}

	/**
	 * Get the hostname for localhost.
	 * @return
	 */
	public static String getHostname() {
		try {
			return InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			Log.logException(e, null);
		}
		return null;
	}
	
	/**
	 * Get the next history instance for a schedule.
	 * @param schedule
	 * @param event
	 * @return
	 */
	public static ScheduleEventBean nextEvent(ScheduleBean schedule, ScheduleEventBean event) {
		ScheduleEventBean result = new ScheduleEventBean();
		result.setScheduleId(event.getScheduleId());
		result.setStatus("P");
		CalcResult next = calcNextRun(schedule, event);
		result.setEventDts(next.nextEventDts);
		result.setRuleIndex(next.ruleIndex);
		result.setRetries(0);
		return result;
	}
	
	/**
	 * Support method for Month calculation.  It increments to the next day that 
	 * @param schedule
	 * @param checkDts
	 * @return
	 */
	private static LocalDateTime nextRunDts(ScheduleBean schedule, LocalDateTime checkDts) {
		LocalDateTime nextRun = checkDts.plusDays(1);
		if (schedule.getIntervalCount() == 1)
			return nextRun;
		// Make sure the month is correct for the intervals from start
		int y = nextRun.getYear() - schedule.getStartDts().getYear();
		int m = nextRun.getMonthValue() - schedule.getStartDts().getMonthValue();
		int months = y * 12 + m;
		if (months % schedule.getIntervalCount() == 0)
			return nextRun;
		months = schedule.getIntervalCount() - (months % schedule.getIntervalCount());
		return nextRun.plusMonths(months);
	}

	/**
	 * Support method: f(0) = 1 and f(1) = 0;
	 * @param source
	 * @return
	 */
	private static int toggle(int source) { 
		return 1 - source; 
	}
	
	// === Validation Methods ===
	/**
	 * Check to see if a datetime validates for the specified task definition. 
	 * @param schedule
	 * @param checkDts
	 * @return
	 */
	public static ValidateResult validateDts(ScheduleBean schedule, LocalDateTime checkDts) {
		return switch (schedule.getIntervalType()) {
		case "m", "h" -> new ValidateResult(true, 0);
		case "D" -> validateDtsDay(schedule, checkDts);
		case "M" -> validateDtsMonth(schedule, checkDts);
		default -> new ValidateResult(false, 0);
		};
	}
	
	/**
	 * Check to see if a datetime validates against a Day ("D") interval task definition. 
	 * @param schedule
	 * @param checkDts
	 * @return
	 */
	private static ValidateResult validateDtsDay(ScheduleBean schedule, LocalDateTime checkDts) {
		// If there are no rules, then no validity check is needed
		if (schedule.getRules() == null)
			return new ValidateResult(true, 0);
		// Check to see if the rules have a valid format
		if (!schedule.getRules().matches(COMPARE_DAY_FORMAT))
			throw new IllegalArgumentException(String.format("Day rules must match this RegEx: '%s'.", COMPARE_DAY_FORMAT));
		// Check to see if the rules are invalid (i.e., no applicable week days)
		if (schedule.getRules().matches(COMPARE_DAY_INVALID))
			throw new IllegalArgumentException(String.format("Day rules cannot match this RegEx: '%s'.  At least one day of at least one week must be enabled.", COMPARE_DAY_INVALID));
		// Process rules to see if the checkDts is valid.
		String[] rules = schedule.getRules().split(",");
		int ruleIndex = 0;
		boolean valid = checkDow(rules[ruleIndex], checkDts);
		if (!valid && rules.length > 1 && ruleIndex == 0) {
			ruleIndex = 1;
			valid = checkDow(rules[ruleIndex], checkDts);
		}
		return new ValidateResult(valid, ruleIndex);
	}
	
	/**
	 * Check to see if a datetime validates against a Month ("M") interval task definition.
	 * @param schedule
	 * @param checkDts
	 * @return
	 */
	private static ValidateResult validateDtsMonth(ScheduleBean schedule, LocalDateTime checkDts) {
		// If there are no rules, then no validity check is needed
		if (schedule.getRules() == null)
			return new ValidateResult(true, 0);
		String[] rules = schedule.getRules().split(",");
		for (String rule : rules) {
			// Check to see if the rule has a valid format
			if (!rule.matches(COMPARE_MONTH_FORMAT))
				throw new IllegalArgumentException(String.format("Month rules must match this RegEx: '%s'.", COMPARE_MONTH_FORMAT));
			// Check to see if the rule is invalid (i.e., invalid days of month)
			if (schedule.getRules().matches(COMPARE_MONTH_INVALID))
				throw new IllegalArgumentException(String.format("Month rules cannot match this RegEx: '%s'.  You cannot reference a day greater than 31.", COMPARE_MONTH_INVALID));
		}
		// Process rules to see if the checkDts is valid.
		int ruleIndex = 0;
		boolean valid = checkMonthRule(rules[ruleIndex], checkDts);
		while (!valid && rules.length > 1 && ruleIndex < (rules.length - 1)) {
			ruleIndex++;
			valid = checkMonthRule(rules[ruleIndex], checkDts);
		}
		return new ValidateResult(valid, ruleIndex);
	}
}
