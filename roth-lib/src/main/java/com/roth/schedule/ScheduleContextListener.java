package com.roth.schedule;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;

import com.roth.base.log.Log;
import com.roth.base.util.Data;

public class ScheduleContextListener implements ServletContextListener {
	private static final boolean ENABLED = Data.getWebEnv("schedulerEnabled", false);

	private ScheduleData scheduleData;
	
	@Override
	public void contextInitialized(ServletContextEvent event) {
		String origin = "com.roth.schedule.listener.ScheduleContextListener.contextInitialized";
		String message = "ScheduleContextListener initializing.";
		Log.log("MESSAGE: ", message, origin, "[SYSTEM]", false, null);
		Log.logDebug("Scheduler is " + (ENABLED ? "disabled" : "enabled") + ".", null, "scheduler");
		
		if (!ENABLED)
			return;
		
		scheduleData = new ScheduleData(event.getServletContext());
		scheduleData.scheduleTask(new ScheduleTimerTask(scheduleData), true);
	}

	@Override
	public void contextDestroyed(ServletContextEvent event) {
		scheduleData.getTimer().cancel();
		scheduleData.getTimer().purge();
		
		String origin = "com.roth.schedule.listener.ScheduleContextListener.contextDestroyed";
		String message = "ScheduleContextListener destroyed.";
		Log.log("MESSAGE: ", message, origin, "[SYSTEM]", false, null);
	}
}
