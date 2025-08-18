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

public class LogChannelMessage implements ChannelMessage {
	private static final long serialVersionUID = 6553733983930314688L;
	
	private String logConfig;
	private String userid;
	
	public LogChannelMessage(String logConfig, String userid) { 
		this.logConfig = logConfig;
		this.userid = userid;
	}

	public String getLogConfig() { return logConfig; }
	public void setLogConfig(String logConfig) { this.logConfig = logConfig; }

	public String getUserid() { return userid; }
	public void setUserid(String userid) { this.userid = userid; }

	@Override
	public void receiveMessage() {
		Log.setConfig(logConfig, userid);
		String origin = this.getClass().getCanonicalName() + ".receiveMessage";
		String message = "Changing logConfig to: \n" + logConfig;
		Log.log("MESSAGE: ", message, origin, userid, false, null);
	}
}
