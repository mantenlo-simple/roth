/*
Copyright 2010 James M. Payne

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */
package com.roth.base.log;

import com.roth.servlet.util.ChannelMessage;

public class LogChannelMessageOld implements ChannelMessage {
	private static final long serialVersionUID = 6553733983930314688L;
	
	private String logCodes;
	private Integer logLevel;
	private Boolean logStackTrace;
	private String userid;
	
	public LogChannelMessageOld(Integer logLevel, Boolean logStackTrace, String logCodes, String userid) { 
		this.logLevel = logLevel;
		this.logStackTrace = logStackTrace;
		this.logCodes = logCodes;
		this.userid = userid;
	}

	public String getLogCodes() { return logCodes; }
	public void setLogCodes(String logCodes) { this.logCodes = logCodes; }
	
	public Integer getLogLevel() { return logLevel; }
	public void setLogLevel(Integer logLevel) { this.logLevel = logLevel; }

	public Boolean getLogStackTrace() { return logStackTrace; }
	public void setLogStackTrace(Boolean logStackTrace) { this.logStackTrace = logStackTrace; }

	public String getUserid() { return userid; }
	public void setUserid(String userid) { this.userid = userid; }

	@Override
	public void receiveMessage() {
		// Deprecate Log in favor of Logger
	//	Log.setLogCodes(logCodes);
	//	Log.setLogLevel(logLevel);
	//	Log.setLogStackTrace(logStackTrace);
		
		/*
		Logger.setLogCodes(logCodes == null ? EMPTY_ARRAY : logCodes.split(","));
		Logger.setLogLevel(LogLevel.ofLevel(logLevel));
		Logger.setLogStackTrace(logStackTrace);
		*/
		
	//	String origin = this.getClass().getCanonicalName() + ".receiveMessage";
	//	String message = "Changing Log.logLevel to: " + Log.LOG_LEVELS[logLevel] +
	//			         (logStackTrace ? " with " : " without ") + "stacktrace" +
	//			         (Data.isEmpty(logCodes) ? "." : ", and logCodes: " + logCodes);
	//	Log.log("MESSAGE: ", message, origin, userid, false);
	}
}
