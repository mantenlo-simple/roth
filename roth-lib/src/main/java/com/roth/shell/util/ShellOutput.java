package com.roth.shell.util;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
//import java.io.OutputStream;
import java.io.Serializable;
import java.util.Scanner;

public class ShellOutput implements Serializable {
	private static final long serialVersionUID = -2177541292362158673L;

	private int exitCode;
	private String stdOut;
	private String stdErr;
	
	public ShellOutput(Process process) throws IOException {
		// OutputStream stdin = process.getOutputStream ();
		stdOut = processStream(process.getInputStream ());
		stdErr = processStream(process.getErrorStream ());
		exitCode = process.exitValue();
	}
	
	public int getExitCode() { return exitCode; }
	public String getStdOut() { return stdOut; }
	public String getStdErr() { return stdErr; }
	
	@SuppressWarnings("resource")
	private String processStream(InputStream stream) throws IOException {
		Scanner scanner = new Scanner(new BufferedInputStream(stream));
		String result = "";
		while (scanner.hasNextLine())
			result += (result.isEmpty() ? "" : "\n") + scanner.nextLine();
		stream.close();
		return result; 
	}
}
