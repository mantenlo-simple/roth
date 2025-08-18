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
package com.roth.servlet;

import jakarta.servlet.annotation.WebServlet;

import java.util.List;

import org.apache.catalina.tribes.ChannelException;

import com.roth.base.log.Log;
import com.roth.base.log.LogChannelMessage;
import com.roth.base.log.LogLevel;
import com.roth.base.util.Data;
import com.roth.servlet.annotation.ActionServletSecurity;

@WebServlet(urlPatterns = "/Logging/*")
@ActionServletSecurity(roles = "SystemAdmin")
public class Logging extends ActionServlet {
	private static final long serialVersionUID = 1L;

	@Action(forwards = { @Forward(name = "success", path = "index.jsp") })
	public String begin(ActionConnection conn) {
		/*
		putBean(Log.getLogCodes(), "logCodes", "request", conn);
		putBean(Log.getLogLevel(), "logLevel", "request", conn);
		putBean(Log.getLogStackTrace() ? "Y" : "N", "logStackTrace", "request", conn);
		*/
		
		putBean(Log.getConfig(conn.getUserName()), "logSettings", conn);
		return "success";
	}
	
	@Action(responses = { @Response(name = "success", message = "<span>Log settings successfully changed.</span>") })
	public String set(ActionConnection conn) {
		Log.setConfig(conn.getString("logSettings"), conn.getUserName());
		/*
		Log.setLogCodes(List.of(conn.getString("logCodes").split(",")));
		Log.setLogLevel(LogLevel.valueOf(conn.getString("logLevel")));
		Log.setLogStackTrace(conn.getString("logStackTrace").equals("Y"));
		
		try {
			new LogChannelMessage(Log.getConfig(conn.getUserName()), conn.getUserName()).sendMessage();
		}
		catch (ChannelException e) {
			Log.logException(e, null);
		}
		String origin = "servlets.Logging";
		String message = "Changing Log.logLevel to: " + Log.getLogLevel().name() +
				         (Log.getLogStackTrace() ? " with " : " without ") + "stacktrace" +
				         (Data.isEmpty(Log.getLogCodes()) ? "." : ", and logCodes: " + Log.getLogCodes());
		Log.log("MESSAGE: ", message, origin, conn.getUserName(), false);
		*/
		return "success";
	}
}
