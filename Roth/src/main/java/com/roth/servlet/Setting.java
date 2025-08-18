package com.roth.servlet;

import org.apache.catalina.tribes.Channel;
import org.apache.catalina.tribes.ChannelException;

import com.roth.base.log.Log;
import com.roth.jdbc.setting.model.JdbcSettings;
import com.roth.jdbc.setting.model.MailSettings;
import com.roth.jdbc.setting.model.MiscSettings;
import com.roth.jdbc.setting.model.SettingsChannelMessage;
import com.roth.jdbc.setting.model.SmtpSettings;
import com.roth.servlet.annotation.ActionServletSecurity;
import com.roth.servlet.annotation.Navigation;
import com.roth.servlet.annotation.SimpleAction;
import com.roth.setting.SettingContextListener;

import jakarta.servlet.annotation.WebServlet;

@WebServlet("/Setting/*")
@ActionServletSecurity(roles = "PortalAdmin")
@Navigation(simpleActions = { @SimpleAction(name = "begin", action = "load") })
public class Setting extends ActionServlet {
	private static final long serialVersionUID = 1L;

	@Action(forwards = { @Forward(name = SUCCESS, path = "/configuration/_settings.jsp") },
			responses = { @Response(name = FAILURE, httpStatusCode = 500) })
	public String load(ActionConnection conn) {
		try {
			SmtpSettings smtp = new SmtpSettings();
			putBean(smtp, "smtp", conn);
			MailSettings mail = new MailSettings();
			putBean(mail, "mail", conn);
			JdbcSettings jdbc = new JdbcSettings();
			putBean(jdbc, "jdbc", conn);
			MiscSettings misc = new MiscSettings();
			putBean(misc.getValueList(), "misc", conn);
		}
		catch (Exception e) { return returnLogException(e, conn, FAILURE); }
		return "success";
	}
	
	@Action(forwards = { @Forward(name = "success", action= "load") },
			responses = { @Response(name = "failure", httpStatusCode = 500) })
	@Post(beans = { @Bean(name = "smtp", scope = "request", beanClass = SmtpSettings.class),
			        @Bean(name = "mail", scope = "request", beanClass = MailSettings.class),
	                @Bean(name = "jdbc", scope = "request", beanClass = JdbcSettings.class) })
	public String save(ActionConnection conn) {
		try {
			SmtpSettings smtp = getBean(0, conn);
			smtp.save(conn.getUserName());
			MailSettings mail = getBean(1, conn);
			mail.save(conn.getUserName());
			JdbcSettings jdbc = getBean(2, conn);
			jdbc.save(conn.getUserName());
			MiscSettings misc = new MiscSettings();
			misc.setValueList(conn.getString("misc"));
			misc.save(conn.getUserName());
			
			//JdbcUtil.settings.setQueryTimeout(jdbc.getQueryTimeout());
			Channel channel = SettingContextListener.getChannel();
			if (channel != null) {
				try { channel.send(channel.getMembers(), 
						           new SettingsChannelMessage(jdbc.getQueryTimeout(),
						                                     conn.getUserName()), 
						           Channel.SEND_OPTIONS_DEFAULT); }
				catch (ChannelException e) { returnLogException(e, conn, "failure"); }
			}
			String origin = "servlets.Setting";
			String message = "Changing JdbcBean.queryTimeout to: " + jdbc.getQueryTimeout() + ".";
			Log.log("MESSAGE: ", message, origin, conn.getUserName(), false, null);
		}
		catch (Exception e) { return returnLogException(e, conn, "failure"); }
		return "success";
	}
}
