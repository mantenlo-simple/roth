package com.roth.schedule;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.concurrent.Callable;

public interface ScheduleCallable extends Callable<ScheduleCallableResult> {
	/**
	 * Sets incoming parameters prior to executing the call method.
	 * @param args
	 */
	void setParams(String ... args);
	
	default void logException(Long scheduleEventId, Exception e) {
		ByteArrayOutputStream st = new ByteArrayOutputStream();
		e.printStackTrace(new PrintStream(st));
		logMessage(scheduleEventId, "EXCEPTION", e.getMessage() + "\n" + st.toString());
	}
	
	default void logMessage(Long scheduleEventId, String messageType, String message) {
		
	}
}
