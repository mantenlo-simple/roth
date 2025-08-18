package com.roth.schedule;

import java.nio.file.Files;
import java.nio.file.Paths;

import com.roth.base.util.Data;
import com.roth.shell.util.Shell;
import com.roth.shell.util.ShellOutput;

public class ShellCallable implements ScheduleCallable {

	private String eventId;
	private String command;
	
	@Override
	public ScheduleCallableResult call() throws Exception {
		String filePath = Data.enforceEnd(Data.getWebEnv("rothTemp", "/temp/"), "/") + "schedule_event_" + eventId;
		ShellOutput result = Shell.exec(command);
		Files.write(Paths.get(filePath + ".out"), Data.nvl(result.getStdOut()).getBytes());
		Files.write(Paths.get(filePath + ".err"), Data.nvl(result.getStdErr()).getBytes());
		return null;
	}

	@Override
	public void setParams(String... args) {
		eventId = args[0];
		command = args[1];
		
	}
}
