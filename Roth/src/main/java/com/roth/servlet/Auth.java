
package com.roth.servlet;

import java.io.FileNotFoundException;
import java.net.MalformedURLException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.HttpConstraint;
import jakarta.servlet.annotation.ServletSecurity;
import jakarta.servlet.annotation.WebServlet;

import com.roth.base.log.Log;
import com.roth.base.util.Data;

@WebServlet("/Auth/*")
// This requires the traditional @ServletSecurity annotation to invoke the authenticator.
@ServletSecurity(@HttpConstraint(rolesAllowed = "Authenticated"))
public class Auth extends ActionServlet {
	private static final long serialVersionUID = 1L;

	@Action(responses = { @Response(name = "success") })
	public String begin(ActionConnection conn) throws MalformedURLException, FileNotFoundException {
		conn.getResponse().setHeader("X-Frame-Options", "SAMEORIGIN");
		String uri = Data.sanitize(conn.getString("redirect"), true);
		Log.logDebug("Auth Redirect URI: " + uri, null);
		String redirect = "window.top.document.location = '" + uri + "'";
		String close = "window.top.Roth.getDialog('wait').hide(); " +
				       "window.top.Roth.getDialog('login').hide(); " +
				       "window.top.Roth.getDialog('info').flash('You are successfully logged in.<br/>Please resubmit your last request.');";
		String script = uri != null ? redirect : close;
		
		String html = Data.readTextFile(getClass(), "/com/roth/servlet/resource/auth.html").replace("SCRIPT", script); 
		conn.getResponse().setContentType("text/html");
		conn.println(html);
		return "success";
	}
	
	@Action(responses = { @Response(name = "success"),
		                  @Response(name = "failure", httpStatusCode = 500) })
	public String logout(ActionConnection conn) {
		try { 
			conn.getRequest().logout();
			expireXsrfCookie(conn);
		}
		catch (ServletException e) { return returnLogException(e, conn, "failure"); }
		return "success";
	}
}
