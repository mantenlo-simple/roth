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
package com.roth.setting;

import java.io.Serializable;

import org.apache.catalina.tribes.ChannelListener;
import org.apache.catalina.tribes.Member;

import com.roth.base.log.Log;
import com.roth.jdbc.setting.model.SettingsChannelMessage;
import com.roth.jdbc.util.JdbcUtil;

public class SettingChannelListener implements ChannelListener {

	@Override
	public boolean accept(Serializable arg0, Member arg1) {
		return arg0 instanceof SettingsChannelMessage;
	}

	@Override
	public void messageReceived(Serializable arg0, Member arg1) {
		SettingsChannelMessage msg = (SettingsChannelMessage)arg0;
		JdbcUtil.getSettings().setQueryTimeout(msg.getQueryTimeout());
		String origin = "com.roth.setting.SettingChannelListener.messageReceived";
		String message = "Changing JdbcBean.queryTimeout to: " + msg.getQueryTimeout() + ".";
		Log.log("MESSAGE: ", message, origin, msg.getUserid(), false, null);
	}
}
