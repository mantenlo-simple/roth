package com.roth.base.util;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimerTask;

public class RothTimer extends java.util.Timer {
	private static final String NON_POSITIVE_PERIOD = "Non-positive period.";
	private static final Map<TemporalUnit, Long> NORMAL_MAP;
	static {
		Map<TemporalUnit, Long> map = new HashMap<>();
		map.put(ChronoUnit.HOURS, 24L);
		map.put(ChronoUnit.MINUTES, 1_440L);
		map.put(ChronoUnit.SECONDS, 86_400L);
		map.put(ChronoUnit.MILLIS, 86_400_000L);
		map.put(ChronoUnit.NANOS, 86_400_000_000L);
		NORMAL_MAP = map;
	}
	
	public enum NormalForm { 
		FLOOR (0), 
		CEILING (1);
		
		private int multiplier;
		private NormalForm(int multiplier) { this.multiplier = multiplier; }
		long offset(long count) { return count * multiplier; }
	}
	
	/*
	 * Question: Currently normalization is based on a day.  Should normalization be able to be based on any temporal unit?
	 * This might be difficult.  There is no way to do that with a year in anything but months (365.2422 days; 52.177 weeks).
	 * It is reasonable to have normalization periods that long?  Probably not, since a rapid release process means that the
	 * most up-time a server will have is about 2-3 weeks. 
	 * 
	 */
	
	
	
	/**
	 * Checks to see if the interval and count are normal, meaning that 24 hours can be evenly divided by the combination of interval and count.
	 * Additionally, as normalization is based on instances within a day, only time-based temporal units are allowed.  Though millis and nanos
	 * are allowed, it is impractical to use them.<br/>
	 * <br/>
	 * Examples:<br/>
	 * 4 HOURS is normal (6 of them = 24 hours), 5 HOURS is not (4.8 of them = 24 hours)<br/>
	 * 30 MINUTES is normal (48 of them = 24 hours), 50 minutes is not (28.8 of them = 24 hours)
	 * @param interval
	 * @param count
	 * @throws IllegalArgumentException
	 */
	public boolean checkIfNormal(TemporalUnit interval, long count) throws IllegalArgumentException {
		if (!interval.isTimeBased())
			throw new IllegalArgumentException("Date-based temporal units are not valid for normalization.");
		Long check = NORMAL_MAP.get(interval);
		return count == count / check * check;
	}
	
	/**
	 * Normalize the time to the next interval count relative to midnight of the day.<br/>
	 * <br/>
	 * Example 1: If normalizing time to ChronoUnit.MINUTES, with a count of 5, then the interval would be every 5 minutes starting at midnight:<br/>
	 * 00:00, 00:05, 00:10, 00:15 ... 23:50, 23:55<br/>
	 * So, if inputting 13:21, the next normalized interval would be 13:25.<br/>
	 * <br/>
	 * Example 2: If normalizing time to ChronoUnit.HOURS, with a count of 3, then teh interval would be every 3 hours starting at midnight:<br/>
	 * 00:00, 03:00, 06:00 ... 18:00, 21:00<br/>
	 * So, if inputting 13:21, the next normalized interval would be 15:00.<br/>
	 * 
	 * @param source
	 * @param interval
	 * @param count
	 * @return
	 */
	public Date normalize(Date source, TemporalUnit interval, long count) {
		return Date.from(normalize(ZonedDateTime.ofInstant(source.toInstant(), ZoneId.systemDefault()), interval, count).toInstant());
	}
	
	public Date normalize(Date source, TemporalUnit interval, long count, NormalForm normalForm) {
		return Date.from(normalize(ZonedDateTime.ofInstant(source.toInstant(), ZoneId.systemDefault()), interval, count, normalForm).toInstant());
	}
	
	/**
	 * Normalize the time to the next interval count relative to midnight of the day.<br/>
	 * <br/>
	 * Example 1: If normalizing time to ChronoUnit.MINUTES, with a count of 5, then the interval would be every 5 minutes starting at midnight:<br/>
	 * 00:00, 00:05, 00:10, 00:15 ... 23:50, 23:55<br/>
	 * So, if inputting 13:21, the next normalized interval would be 13:25.<br/>
	 * <br/>
	 * Example 2: If normalizing time to ChronoUnit.HOURS, with a count of 3, then teh interval would be every 3 hours starting at midnight:<br/>
	 * 00:00, 03:00, 06:00 ... 18:00, 21:00<br/>
	 * So, if inputting 13:21, the next normalized interval would be 15:00.<br/>
	 * 
	 * @param source
	 * @param interval
	 * @param count
	 * @return
	 */
	public LocalDateTime normalize(LocalDateTime source, TemporalUnit interval, long count) {
		return normalize(source.atZone(ZoneId.systemDefault()), interval, count).toLocalDateTime();
	}
	
	public LocalDateTime normalize(LocalDateTime source, TemporalUnit interval, long count, NormalForm normalForm) {
		return normalize(source.atZone(ZoneId.systemDefault()), interval, count, normalForm).toLocalDateTime();
	}
	
	/**
	 * Normalize the time to the next interval count relative to midnight of the day.<br/>
	 * <br/>
	 * Example 1: If normalizing time to ChronoUnit.MINUTES, with a count of 5, then the interval would be every 5 minutes starting at midnight:<br/>
	 * 00:00, 00:05, 00:10, 00:15 ... 23:50, 23:55<br/>
	 * So, if inputting 13:21, the next normalized interval would be 13:25.<br/>
	 * <br/>
	 * Example 2: If normalizing time to ChronoUnit.HOURS, with a count of 3, then the interval would be every 3 hours starting at midnight:<br/>
	 * 00:00, 03:00, 06:00 ... 18:00, 21:00<br/>
	 * So, if inputting 13:21, the next normalized interval would be 15:00.<br/>
	 * 
	 * @param source
	 * @param interval
	 * @param count
	 * @return
	 */
	public ZonedDateTime normalize(ZonedDateTime source, TemporalUnit interval, long count) {
		return normalize(source, interval, count, NormalForm.CEILING);
	}
	
	/**
	 * Normalize the time of the interval count relative to midnight of the day.  If the normalized date-time equals the source, the increment
	 * parameter indicates whether that is the intended value (false) or whether to increment to the next interval (true).<br/>
	 * <br/>
	 * Example 1: If normalizing time to ChronoUnit.MINUTES, with a count of 5, then the interval would be every 5 minutes starting at midnight:<br/>
	 * 00:00, 00:05, 00:10, 00:15 ... 23:50, 23:55<br/>
	 * So, if inputting 13:21, the next normalized interval would be 13:25.<br/>
	 * <br/>
	 * Example 2: If normalizing time to ChronoUnit.HOURS, with a count of 3, then the interval would be every 3 hours starting at midnight:<br/>
	 * 00:00, 03:00, 06:00 ... 18:00, 21:00<br/>
	 * So, if inputting 13:21, the next normalized interval would be 15:00.<br/>
	 * 
	 * @param source
	 * @param interval
	 * @param count
	 * @param increment
	 * @return
	 */
	public ZonedDateTime normalize(ZonedDateTime source, TemporalUnit interval, long count, NormalForm normalForm) {
		checkIfNormal(interval, count);
		ZonedDateTime result = source.truncatedTo(ChronoUnit.DAYS);
		long diff = result.until(source, interval);
		long floor = diff / count * count;
		return result.plus(floor + normalForm.offset(count), interval);
	}
	
	
	
	
	/**
     * Schedules the specified task for execution at the specified time.  If
     * the time is in the past, the task is scheduled for immediate execution.
     *
     * @param task task to be scheduled.
     * @param time time at which task is to be executed.
     * @throws IllegalStateException if task was already scheduled or
     *         cancelled, timer was cancelled, or timer thread terminated.
     * @throws NullPointerException if {@code task} or {@code time} is null
     */
    public void schedule(TimerTask task, LocalDateTime time) {
        schedule(task, Date.from(time.atZone(ZoneId.systemDefault()).toInstant()));
    }
    
    /**
     * Schedules the specified task for execution at the specified time.  If
     * the time is in the past, the task is scheduled for immediate execution.
     *
     * @param task task to be scheduled.
     * @param time time at which task is to be executed.
     * @throws IllegalStateException if task was already scheduled or
     *         cancelled, timer was cancelled, or timer thread terminated.
     * @throws NullPointerException if {@code task} or {@code time} is null
     */
    public void schedule(TimerTask task, ZonedDateTime time) {
    	schedule(task, Date.from(time.toInstant()));
    }
    
    /**
     * Schedules the specified task for repeated <i>fixed-delay execution</i>,
     * beginning at the specified time. Subsequent executions take place at
     * approximately regular intervals, separated by the specified period.
     *
     * <p>In fixed-delay execution, each execution is scheduled relative to
     * the actual execution time of the previous execution.  If an execution
     * is delayed for any reason (such as garbage collection or other
     * background activity), subsequent executions will be delayed as well.
     * In the long run, the frequency of execution will generally be slightly
     * lower than the reciprocal of the specified period (assuming the system
     * clock underlying {@code Object.wait(long)} is accurate).  As a
     * consequence of the above, if the scheduled first time is in the past,
     * it is scheduled for immediate execution.
     *
     * <p>Fixed-delay execution is appropriate for recurring activities
     * that require "smoothness."  In other words, it is appropriate for
     * activities where it is more important to keep the frequency accurate
     * in the short run than in the long run.  This includes most animation
     * tasks, such as blinking a cursor at regular intervals.  It also includes
     * tasks wherein regular activity is performed in response to human
     * input, such as automatically repeating a character as long as a key
     * is held down.
     *
     * @param task   task to be scheduled.
     * @param firstTime First time at which task is to be executed.
     * @param period time in milliseconds between successive task executions.
     * @throws IllegalArgumentException if {@code period <= 0}
     * @throws IllegalStateException if task was already scheduled or
     *         cancelled, timer was cancelled, or timer thread terminated.
     * @throws NullPointerException if {@code task} or {@code firstTime} is null
     */
    public void schedule(TimerTask task, LocalDateTime firstTime, long period) {
        if (period <= 0)
            throw new IllegalArgumentException(NON_POSITIVE_PERIOD);
        schedule(task, Date.from(firstTime.atZone(ZoneId.systemDefault()).toInstant()), -period);
    }
    
    /**
     * Schedules the specified task for repeated <i>fixed-delay execution</i>,
     * beginning at the specified time. Subsequent executions take place at
     * approximately regular intervals, separated by the specified period.
     *
     * <p>In fixed-delay execution, each execution is scheduled relative to
     * the actual execution time of the previous execution.  If an execution
     * is delayed for any reason (such as garbage collection or other
     * background activity), subsequent executions will be delayed as well.
     * In the long run, the frequency of execution will generally be slightly
     * lower than the reciprocal of the specified period (assuming the system
     * clock underlying {@code Object.wait(long)} is accurate).  As a
     * consequence of the above, if the scheduled first time is in the past,
     * it is scheduled for immediate execution.
     *
     * <p>Fixed-delay execution is appropriate for recurring activities
     * that require "smoothness."  In other words, it is appropriate for
     * activities where it is more important to keep the frequency accurate
     * in the short run than in the long run.  This includes most animation
     * tasks, such as blinking a cursor at regular intervals.  It also includes
     * tasks wherein regular activity is performed in response to human
     * input, such as automatically repeating a character as long as a key
     * is held down.
     *
     * @param task   task to be scheduled.
     * @param firstTime First time at which task is to be executed.
     * @param period time in milliseconds between successive task executions.
     * @throws IllegalArgumentException if {@code period <= 0}
     * @throws IllegalStateException if task was already scheduled or
     *         cancelled, timer was cancelled, or timer thread terminated.
     * @throws NullPointerException if {@code task} or {@code firstTime} is null
     */
    public void schedule(TimerTask task, ZonedDateTime firstTime, long period) {
        if (period <= 0)
            throw new IllegalArgumentException(NON_POSITIVE_PERIOD);
        schedule(task, Date.from(firstTime.toInstant()), -period);
    }
    
    /**
     * Schedules the specified task for repeated <i>fixed-rate execution</i>,
     * beginning at the specified time. Subsequent executions take place at
     * approximately regular intervals, separated by the specified period.
     *
     * <p>In fixed-rate execution, each execution is scheduled relative to the
     * scheduled execution time of the initial execution.  If an execution is
     * delayed for any reason (such as garbage collection or other background
     * activity), two or more executions will occur in rapid succession to
     * "catch up."  In the long run, the frequency of execution will be
     * exactly the reciprocal of the specified period (assuming the system
     * clock underlying {@code Object.wait(long)} is accurate).  As a
     * consequence of the above, if the scheduled first time is in the past,
     * then any "missed" executions will be scheduled for immediate "catch up"
     * execution.
     *
     * <p>Fixed-rate execution is appropriate for recurring activities that
     * are sensitive to <i>absolute</i> time, such as ringing a chime every
     * hour on the hour, or running scheduled maintenance every day at a
     * particular time.  It is also appropriate for recurring activities
     * where the total time to perform a fixed number of executions is
     * important, such as a countdown timer that ticks once every second for
     * ten seconds.  Finally, fixed-rate execution is appropriate for
     * scheduling multiple repeating timer tasks that must remain synchronized
     * with respect to one another.
     *
     * @param task   task to be scheduled.
     * @param firstTime First time at which task is to be executed.
     * @param period time in milliseconds between successive task executions.
     * @throws IllegalArgumentException if {@code period <= 0}
     * @throws IllegalStateException if task was already scheduled or
     *         cancelled, timer was cancelled, or timer thread terminated.
     * @throws NullPointerException if {@code task} or {@code firstTime} is null
     */
    public void scheduleAtFixedRate(TimerTask task, LocalDateTime firstTime, long period) {
        if (period <= 0)
            throw new IllegalArgumentException(NON_POSITIVE_PERIOD);
        schedule(task, Date.from(firstTime.atZone(ZoneId.systemDefault()).toInstant()), period);
    }
    
    /**
     * Schedules the specified task for repeated <i>fixed-rate execution</i>,
     * beginning at the specified time. Subsequent executions take place at
     * approximately regular intervals, separated by the specified period.
     *
     * <p>In fixed-rate execution, each execution is scheduled relative to the
     * scheduled execution time of the initial execution.  If an execution is
     * delayed for any reason (such as garbage collection or other background
     * activity), two or more executions will occur in rapid succession to
     * "catch up."  In the long run, the frequency of execution will be
     * exactly the reciprocal of the specified period (assuming the system
     * clock underlying {@code Object.wait(long)} is accurate).  As a
     * consequence of the above, if the scheduled first time is in the past,
     * then any "missed" executions will be scheduled for immediate "catch up"
     * execution.
     *
     * <p>Fixed-rate execution is appropriate for recurring activities that
     * are sensitive to <i>absolute</i> time, such as ringing a chime every
     * hour on the hour, or running scheduled maintenance every day at a
     * particular time.  It is also appropriate for recurring activities
     * where the total time to perform a fixed number of executions is
     * important, such as a countdown timer that ticks once every second for
     * ten seconds.  Finally, fixed-rate execution is appropriate for
     * scheduling multiple repeating timer tasks that must remain synchronized
     * with respect to one another.
     *
     * @param task   task to be scheduled.
     * @param firstTime First time at which task is to be executed.
     * @param period time in milliseconds between successive task executions.
     * @throws IllegalArgumentException if {@code period <= 0}
     * @throws IllegalStateException if task was already scheduled or
     *         cancelled, timer was cancelled, or timer thread terminated.
     * @throws NullPointerException if {@code task} or {@code firstTime} is null
     */
    public void scheduleAtFixedRate(TimerTask task, ZonedDateTime firstTime, long period) {
        if (period <= 0)
            throw new IllegalArgumentException(NON_POSITIVE_PERIOD);
        schedule(task, Date.from(firstTime.toInstant()), period);
    }
}
