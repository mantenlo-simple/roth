package com.roth.shell.util;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import com.roth.base.util.Data;

public class Shell {
	public static ShellOutput exec(String command) throws IOException {
		return exec(command, null, null);
	}
	
	public static ShellOutput exec(String command, Map<String,String> envp) throws IOException {
		return exec(command, envp, null);
	}
	
	public static ShellOutput exec(String command, String dir) throws IOException {
		return exec(command, null, dir);
	}
	
	public static ShellOutput exec(String command, Map<String,String> envp, String dir) throws IOException {
		ProcessBuilder builder = new ProcessBuilder().directory(new File(dir)).command(command.split(" "));
		if (!Data.isEmpty(envp))
			builder.environment().putAll(envp);
		return new ShellOutput(builder.start());
	}
}
