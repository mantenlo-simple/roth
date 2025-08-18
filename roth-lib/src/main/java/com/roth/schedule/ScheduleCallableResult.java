package com.roth.schedule;

import java.time.LocalDateTime;

public class ScheduleCallableResult {
	public enum ExitCode {
		COMPLETE,
		ERROR,
		CANCELLED
	}
	
	private LocalDateTime stopTime;
	private ExitCode exitCode;
	private String exitMessage;
	
	/**
	 * Signal the result of the thread.
	 * @param exitMessage Null if successful, or a message if a failure occurred.
	 */
	public ScheduleCallableResult(ExitCode exitCode, String exitMessage) {
		stopTime = LocalDateTime.now();
		this.exitCode = exitCode;
		this.exitMessage = exitMessage;
	}

	public LocalDateTime getStopTime() { return stopTime; }

	public ExitCode getExitCode() { return exitCode; }
	
	public String getExitMessage() { return exitMessage; }
}
