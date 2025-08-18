package com.roth.schedule;

import java.sql.SQLException;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

import jakarta.servlet.ServletContext;

import com.roth.base.log.Log;
import com.roth.base.util.Data;
import com.roth.base.util.RothTimer;
import com.roth.jdbc.setting.model.ScheduleSettings;
import com.roth.jdbc.util.TableUtil;
import com.roth.schedule.model.ScheduleBean;
import com.roth.schedule.util.ScheduleUtil;

public class ScheduleData {
	// TIMER_INTERVAL: Number of seconds between timer tasks.
	private static final int TIMER_INTERVAL = Data.getWebEnv("schedulerTimerInterval", 5);
	
	// MAX_THREADS: Maximum Number of threads allowed to run at any time.
	private static final int MAX_THREADS = Data.getWebEnv("schedulerMaxThreads", 3);
	
	// MAX_AGE: Maximum age for events before removing from event log.  Format: XX:YY
	//      XX: The number of hours to keep event logs with intervals < DAY.
	//      YY: The number of days to keep event logs with intervals >= DAY.
	//private static final String MAX_AGE = Data.getWebEnv("scheduleMaxAge", "48,30");
	//private static final int MAX_AGE_HOURS = Data.strToInteger(MAX_AGE.split(",")[0]);
	//private static final int MAX_AGE_DAYS = Data.strToInteger(MAX_AGE.split(",")[1]);
	
	/*
	
	What is the job of ScheduleData?
	
	Usually, the job of a context listener data object is to provide a means for timer tasks to be rescheduled.
	
	*/
	
	
	private ServletContext context;
	private RothTimer timer;
	private Integer timerInterval;
	private Integer maxThreads;
	private List<ScheduleThread> threadPool;
	
	// Constructor
	public ScheduleData(ServletContext context) {
		this.context = context;
		if (Data.nvl(context.getInitParameter("usePortalSettings")).equals("true"))
			usePortalSettings();
		threadPool = new ArrayList<>();
	}
	
	/**
	 * Schedule the next timer task.
	 * @param task
	 * @param scheduleImmediate Whether to schedule it for right now (true) or on the next normalized interval (false).
	 */
	public void scheduleTask(TimerTask task, boolean scheduleImmediate) {
		LocalDateTime datetime = LocalDateTime.now();
		if (!scheduleImmediate)
			datetime = getTimer().normalize(datetime, ChronoUnit.SECONDS, getTimerInterval());
		Log.logDebug("Scheduling task at " + Data.dtsToStr(datetime) + ".", null, "scheduler");
		getTimer().schedule(task, datetime);
	}
	
	/**
	 * Check if the thread pool can execute another thread.
	 * @return
	 */
	public boolean isAvailable() {
		for (ScheduleThread thread : threadPool) {
			if (!thread.isRunning()) {
				try {
					ScheduleCallableResult result = thread.getResult();
					thread.getEvent().setEndDts(result.getStopTime());
					thread.getEvent().setStatus(result.getExitMessage() == null ? "C" : "E");
					if (result.getExitMessage() != null)
						thread.getEvent().setErrOut(result.getExitMessage());
					new TableUtil("roth").save(thread.getEvent());
				} 
				catch (InterruptedException | ExecutionException | SQLException e) {
					Log.logException(e, null);
				}
				releaseEvent(thread);
			}
		}
		
		return threadPool.size() < MAX_THREADS;
	}
	
	/**
	 * Add an event to the thread pool.
	 * If the thread pool is already full, nothing will happen.
	 * @param schedule
	 * @param event
	 */
	public void addEvent(ScheduleThread thread) {
		if (!isAvailable())
			return;
		thread.run();
		threadPool.add(thread);
	}
	
	/**
	 * Release a thread from the pool.  This is done by the ScheduleThread process.
	 * @param thread
	 */
	synchronized void releaseEvent(ScheduleThread thread) {
		threadPool.remove(thread); 
	}
	
	
	
	
	
	
	
	
	
	public ServletContext getContext() {
		return context;
	}
	
	
	
	
	
	public void updateThreadPool() {
		/*
		while (isAvailable()) {
			ScheduleCallable callable;
			
			
			executor.submit(callable);
		}
		*/
	}
	
	protected int getThreadPoolSize() {
		for (int i = threadPool.size() - 1; i >= 0; i--) {
			ScheduleThread t = threadPool.get(i);
			if (!t.isRunning())
				threadPool.remove(i);
		}
		return threadPool.size();
	}
	
	protected RothTimer getTimer() {
		if (timer == null)
			timer = new RothTimer();
		return timer;
	}
	
	protected int getTimerInterval() {
		return Data.nvl(timerInterval, TIMER_INTERVAL);
	}
	
	protected int getMaxThreads() {
		return Data.nvl(maxThreads, MAX_THREADS); 
	}
	
	protected void usePortalSettings() {
		try {
			ScheduleSettings settings = new ScheduleSettings();
			timerInterval = settings.getTimerInterval();
			maxThreads = settings.getMaxThreads();
		}
		catch (SQLException e) {
			Log.logException(e, null);
		}
	}
	
	/**
	 * Check to see if the period is valid.  If not, then an IllegalArgumentException is thrown.
	 * @param period
	 */
	protected static void validatePeriod(String period) {
		if (period == null || !Data.in(period, new String[] {"m", "h", "D", "M"}))
			throw new IllegalArgumentException("Invalid period specified: '" + period + "'.");
	}
	
	/**
	 * Check to see if the frequency is valid.  If not, then an IllegalArgumentException is thrown.
	 * @param frequency
	 */
	protected static void validateFrequency(Integer frequency) {
		if (frequency == null || frequency < 1)
			throw new IllegalArgumentException("Invalid frequency specified.");
	}
	
	/**
	 * Regular expression for a valid specifier for the day period.
	 */
	protected static final String DAY_SPECIFIER = "[-|S][-|M][-|T][-|W][-|T][-|F][-|S]";
	/**
	 * Regular expression for a valid ordinal weekday specifier for the month period.
	 */
	protected static final String ORD_WEEKDAY_SPECIFIER = "-{0,1}[0-9](SU|MO|TU|WE|TH|FR|SA)";
	/**
	 * Regular expression for a valid day of month specifier for the month period.
	 */
	protected static final String DAY_OF_MONTH_SPECIFIER = "-{0,1}[0-9]{1,2}";
	protected static final String INVALID_SPECIFIER = "Invalid specifier specified.";
	
	/**
	 * Check to see if the specifier is valid.  If not, then an IllegalArgumentException is thrown.
	 * @param period
	 * @param specifier
	 */
	protected static void validateSpecifier(String period, String specifier) {
		if (specifier == null) 
			return;
		if (("D".equals(period) && !specifier.matches(DAY_SPECIFIER)) ||
			("M".equals(period) && !specifier.matches(ORD_WEEKDAY_SPECIFIER) && !specifier.matches(DAY_OF_MONTH_SPECIFIER)))
			throw new IllegalArgumentException(INVALID_SPECIFIER);
	}
	
	protected static LocalDateTime normalize(LocalDateTime source, LocalDateTime start, String period, Integer frequency) {
		Long diff = LocalDateTime.from(start).until(source, ChronoUnit.MINUTES); 
		if ("h".equals(period))
			diff /= 60;
		Long norm = diff / frequency * frequency;
		diff = (norm < diff) ? norm + frequency : diff;
		if ("h".equals(period))
			diff *= 60;
		return source.plusMinutes(diff);
	}

	/**
	 * Calculate the next event date.  This assumes that the supplied date was the previous date from the same period, frequency, and specifier<br/>
	 * About the specifier:<br/>
	 * When used with a day period, the specifier (if used) determines what days of the week are valid.  Invalid days of the week are skipped.<br/>
	 * Examples: 'SMTWTFS' indicates all days of the week are valid; '-M-W-F-' indicates only Monday, Wednesday, and Friday are valid.<br/>
	 * When used with a month period, the specifier (if used) determines either what day of month (relative to the first or last day),
	 * or what ordinal weekday of the month (again, relative to the first or last of the month) to calculate.<br/>
	 * Examples: '1' indicates the first day of the month, '2' indicates the second day of the month, and so on.<br/>
	 *           '-1' indicates the last day of the month, '-2' indicates the second to last day of the month, and so on.<br/>
	 *           '1MO' indicates the first Monday of the month, '2MO' indicates the second Monday of the month, and so on.<br/>
	 *           '-1MO' indicates the last Monday of the month, '-2MO' indicates the second to last Monday of the month, and so on.<br/>
	 *           Days of the week for the month specifier should be 'SU', 'MO', 'TU', 'WE', 'TH', 'FR', or 'SA'.<br/>
	 * If the specifier is null, then period and frequency alone will be used to calculate. 
	 * @param prev Previous date.
	 * @param period The unit of time.  Valid values are 'm' for minute, 'h' for hour, 'D' for day, or 'M' for month.
	 * @param frequency The number of periods between events.
	 * @param specifier How to further interpret the period and frequency.  May be null.  This is ignored for minute and hour periods, 
	 * and for day periods where the frequency is greater than 1.
	 * @return
	 * @throws ParseException
	 */
	public static LocalDateTime calcNextEvent(LocalDateTime prev, String period, Integer frequency, String specifier, String startTime) throws ParseException {
		return calcEvent(prev, period, frequency, specifier, startTime, true);
	}
	
	private static ChronoUnit translatePeriod(String period) {
		switch (period) {
			case "s": return ChronoUnit.SECONDS;
			case "m": return ChronoUnit.MINUTES;
			case "h": return ChronoUnit.HOURS;
			case "D": return ChronoUnit.DAYS;
			case "M": return ChronoUnit.MONTHS;
			case "Y": return ChronoUnit.YEARS;
			default: return null;
		}
	}
	
	/**
	 * Calculate an event date closest to, but after the source, based on the period, frequency, and specifier.<br/>
	 * About the specifier:<br/>
	 * When used with a day period, the specifier (if used) determines what days of the week are valid.  Invalid days of the week are skipped.<br/>
	 * Examples: 'SMTWTFS' indicates all days of the week are valid; '-M-W-F-' indicates only Monday, Wednesday, and Friday are valid.<br/>
	 * When used with a month period, the specifier (if used) determines either what day of month (relative to the first or last day),
	 * or what ordinal weekday of the month (again, relative to the first or last of the month) to calculate.<br/>
	 * Examples: '1' indicates the first day of the month, '2' indicates the second day of the month, and so on.<br/>
	 *           '-1' indicates the last day of the month, '-2' indicates the second to last day of the month, and so on.<br/>
	 *           '1MO' indicates the first Monday of the month, '2MO' indicates the second Monday of the month, and so on.<br/>
	 *           '-1MO' indicates the last Monday of the month, '-2MO' indicates the second to last Monday of the month, and so on.<br/>
	 *           Days of the week for the month specifier should be 'SU', 'MO', 'TU', 'WE', 'TH', 'FR', or 'SA'.<br/>
	 * If the specifier is null, then period and frequency alone will be used to calculate. 
	 * @param date Origin date.
	 * @param period The unit of time.  Valid values are 'm' for minute, 'h' for hour, 'D' for day, or 'M' for month.
	 * @param frequency The number of periods between events.
	 * @param specifier How to further interpret the period and frequency.  May be null.  This is ignored for minute and hour periods, 
	 * and for day periods where the frequency is greater than 1.
	 * @param increment Whether to treat the origin date as the previous date (true) or a proximal date (false).
	 * @return
	 * @throws ParseException
	 */
	public static LocalDateTime calcEvent(LocalDateTime source, String period, Integer frequency, String specifier, String startTime, boolean increment) throws ParseException {
		ChronoUnit p = translatePeriod(period);
		LocalDateTime s = increment ? source : source.minus(frequency, p);
		while (s.compareTo(source) < 0 || s.compareTo(LocalDateTime.now()) < 0) {
			if (Data.in(period, new String[] {"s", "m", "h"}))
				s = IntervalHypoDay.next(s, p, frequency, specifier);
			else if (period.equals("D"))
				s = IntervalDay.next(s, p, frequency, specifier);
			else if (Data.in(period, new String[] {"M", "Y"}))
				s = IntervalHyperDay.next(s, p, frequency, specifier);
			else
				throw new IllegalArgumentException("Invalid period.  Period must be 'm' (minute), 'h' (hour), 'D' (Day), or 'M' (Month).");
		}
		return s;
		/*
		// Validate the period, frequency, and specifier.
		validatePeriod(period);
		validateFrequency(frequency);
		validateSpecifier(period, specifier);
		// Normalize the source date (i.e. truncate to the minute, hour, or day depending on period).
		LocalDateTime result; 
		if ("m".equals(period)) 
			result = source.truncatedTo(ChronoUnit.MINUTES);
		else if ("h".equals(period)) 
			result = source.truncatedTo(ChronoUnit.HOURS);
		else
			result = source.truncatedTo(ChronoUnit.DAYS);
		// Increment the period, if applicable.
		if (increment) {
			if ("m".equals(period) || "h".equals(period))
				result = Data.expToDts("N+" + frequency + period, result);
			else
				result = Data.expToDts("T+" + frequency + period, result);
		}
		// Validate result and, if necessary, calculate next valida date.
		if ("m".equals(period) || "h".equals(period))
			result = calcTimeEvent(result, period, frequency, startTime);
		else if ("D".equals(period))
			result = calcDayEvent(result, frequency, specifier, startTime);
		else if (specifier != null && specifier.matches(ORD_WEEKDAY_SPECIFIER))
			result = calcOrdWeekdayEvent(result, frequency, specifier, startTime);
		else if (specifier != null && specifier.matches(DAY_OF_MONTH_SPECIFIER))
			result = calcDayOfMonthEvent(result, frequency, specifier, startTime);
		// Return result if >= source, otherwise calculate next event date.
		
		return result.compareTo(source) >= 0 ? result : calcEvent(source, period, frequency, specifier, startTime, true);	
		*/
		
		/*
		if ("m".equals(period) || "h".equals(period))
			result = Data.expToDts("N+" + frequency + period, date);
		else if (specifier == null || ("D".equals(period) && frequency > 1))
			result = Data.expToDts("T+" + frequency + period, date);
		else if ("D".equals(period)) 
			result = calcNextDayEvent(date, specifier, increment);
		else if ("M".equals(period)) {
			try {
				if (specifier.matches(ORD_WEEKDAY_SPECIFIER))
					result = calcNextOrdWeekdayEvent(date, frequency, specifier, increment);
				else if (specifier.matches(DAY_OF_MONTH_SPECIFIER))
					result = calcNextDayOfMonthEvent(date, frequency, specifier, increment);
				else
					throw new IllegalArgumentException(INVALID_SPECIFIER);
			}
			catch (NumberFormatException e) {
				throw new IllegalArgumentException(INVALID_SPECIFIER);
			}
		}
		else
			throw new IllegalArgumentException(INVALID_SPECIFIER);
		return result.compareTo(date) < 0 ? calcEvent(date, period, frequency, specifier, true) : result;
		*/
	}
	
	/**
	 * Update the schedule record for the next 
	 * @param schedule
	 * @throws ParseException
	 * @throws SQLException
	 */
	protected static void updateSchedule(ScheduleBean schedule) throws ParseException, SQLException {
		/*
		LocalDateTime temp = calcNextEvent(schedule.getNextInstance(), schedule.getPeriod(), schedule.getFrequency(), schedule.getSpecifier(), schedule.getStartTime());
		// Only update if the schedule should continue. 
		if (schedule.getEndDate() != null && temp.compareTo(schedule.getEndDate()) >= 0) {
			schedule.setLastInstance(schedule.getNextInstance());
			schedule.setNextInstance(temp);
		}
		*/
		new ScheduleUtil().save(schedule);
	}
}