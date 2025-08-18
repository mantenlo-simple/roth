package com.roth.schedule;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.roth.base.log.Log;
import com.roth.base.util.Data;
import com.roth.schedule.model.ScheduleBean;
import com.roth.schedule.model.ScheduleEventBean;

public class ScheduleThread {
	private ScheduleBean schedule;
	private ScheduleEventBean event;
	private ExecutorService executor;
	private Future<ScheduleCallableResult> future;
	
	public ScheduleThread(ScheduleBean schedule, ScheduleEventBean event) {
		this.schedule = schedule;
		this.event = event;
	}
	
	public boolean isRunning() { return !(future.isCancelled() || future.isDone()); }
	
	public boolean cancel() { return future.cancel(true); }
	
	public ScheduleCallableResult getResult() throws InterruptedException, ExecutionException { return future.get(); }

	public ScheduleBean getSchedule() { return schedule; }
	public ScheduleEventBean getEvent() { return event; }
	
	public void setFuture(Future<ScheduleCallableResult> future) { this.future = future; }
	
	public void run() {
		executor = Executors.newSingleThreadExecutor();
		ScheduleCallable callable = getCallable();
		if (callable != null)
			future = executor.submit(callable);
	}
	
	private ScheduleCallable getCallable() {
		ScheduleCallable result = null;
		String params = schedule.getCommand();
		if (ScheduleBean.COMMAND_TYPE_JAVA.equals(schedule.getCommandType())) {
			/* Format: cannonical.class.Name(paramValue1,paramValue2,...) */
			String callableClassName = getCommandClass(schedule.getCommand());
			params = getCommandParams(schedule.getCommand());
			try { result = (ScheduleCallable)Data.newInstance(Class.forName(callableClassName)); } 
			catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException | ClassNotFoundException e) { Log.logException(e, null); }
		}
		else
			result = ScheduleBean.COMMAND_TYPE_SHELL.equals(schedule.getCommandType()) ? new ShellCallable() : new SqlCallable();
		if (result != null)
			result.setParams(event.getScheduleEventId().toString(), params);
		return result;
	}
	
	private String getCommandClass(String command) {
		return Data.isEmpty(command) || command.indexOf('(') < 0 || command.indexOf(')') < 0 ? null
			 : command.split("(")[0];
	}
	
	private String getCommandParams(String command) {
		return Data.isEmpty(command) || command.indexOf('(') < 0 || command.indexOf(')') < 0 ? null
			 : command.split("(")[1].split(")")[0];
	}
}