package com.roth.schedule;

import java.sql.SQLException;
import java.util.List;
import java.util.TimerTask;

import com.roth.base.log.Log;
import com.roth.schedule.model.ScheduleBean;
import com.roth.schedule.model.ScheduleEventBean;
import com.roth.schedule.util.ScheduleUtil;

public class ScheduleTimerTask extends TimerTask {

	private ScheduleData scheduleData;
	
	public ScheduleTimerTask(ScheduleData scheduleData) {
		this.scheduleData = scheduleData;
	}
	
	@Override
	public void run() {
		Log.logDebug("ScheduleTimerTask heartbeat.", null, "schedule");
		
		// Do something
		
		/*
		 * Schedule made up of the following:
		 * 
		 * period    -> 'm' - minute
		 *              'h' - hour
		 *              'D' - day
		 *              'M' - month
		 * frequency -> number of periods
		 * specifier -> (see comment at bottom)
		 * 
		 * even_type -> 'S' - Shell execute
		 *              'J' - Java execute
		 *              'P' - Stored procedure/function execute
		 *              'Q' - SQL execute; can be a SELECT (output to "std out"), or INSERT/UPDATE/DELETE (updated records count to "std out")
		 *         All exceptions to "err out".
		 *         
		 * event status -> 'P' - Pending execution
		 *                 'R' - Running (executing)
		 *                 'F' - Finished (executing has finished)
		 *                 'E' - Error (execution ended with an error/exception)
		 *                 'C' - Canceled (execution was canceled)
		 *                 'S' - Suspended (execution is suspended; only a pending or aborted event can be suspended)
		 */
		
		// First check to see if any events need to be created.
		
		// CHANGE OF PLAN: The first event for a schedule should be created at the following times:
		//    - When the schedule is created.
		//    - When the schedule is enabled after being previously disabled.
		// Subsequent events should be created at the following times:
		//    - When a scheduled event reaches a 'C' (complete) status
		//    - When the user manually initiates a new event from the schedule
		//        - This can only be done if there are no events in the 'P' (pending) or 'R' (running) statuses
		// Events can be rerun if the status is 'E' (error), 'A' (aborted), or 'S' (suspended).
		
		List<ScheduleBean> schedules = null;
		/*
		try {
			ScheduleUtil util = new ScheduleUtil();
			schedules = util.getPendingSchedules();
			List<StateBean> batch = new ArrayList<>();
			if (schedules != null)
				for (ScheduleBean schedule : schedules) {
					Log.logDebug("ScheduleTimerTask - Scheduling an event for ID: " + schedule.getScheduleId() + ".", null, "schedule");
					ScheduleEventBean event = new ScheduleEventBean(schedule.getScheduleId());
					batch.add(event);
					LocalDateTime lastInstance = Data.nvl(schedule.getNextInstance(), LocalDateTime.now());
					
					
					
					schedule.setNextInstance(ScheduleData.calcNextEvent(lastInstance, schedule.getPeriod(), schedule.getFrequency(), schedule.getSpecifier(), schedule.getStartTime()));
					batch.add(schedule);
				}
			if (!batch.isEmpty())
				util.save(batch);
		} catch (SQLException | ParseException e) {
			Log.logException(e, null);
		}
		*/
		
		// Second check to see if any events need to be executed.
		List<ScheduleEventBean> events = null;
		try {
			ScheduleUtil util = new ScheduleUtil();
			events = util.getPendingEvents();
			if (events != null)
				for (ScheduleEventBean event : events) {
					Log.logDebug("ScheduleTimerTask - Executing event ID: " + event.getScheduleEventId() + ".", null, "schedule");
					if (schedules != null) {
						ScheduleBean schedule = schedules.stream().filter(s -> s.getScheduleId().equals(event.getScheduleId())).findFirst().get();
						/*
						// Simulate running...
						// Switch to "Running" status.
						event.setStatus("R");
						event.setEventStartDts(LocalDateTime.now());
						// Switch to "Complete" status.
						event.setStatus("C");
						event.setEventEndDts(LocalDateTime.now());
						*/
						// Save it.
						util.save(event);
						scheduleData.addEvent(new ScheduleThread(schedule, event));
					}
				}
		} catch (SQLException e) {
			Log.logException(e, null);
		}

		//scheduleData.
		
		// Third clean up old data.
		
		scheduleData.scheduleTask(new ScheduleTimerTask(scheduleData), false);
	}

}
