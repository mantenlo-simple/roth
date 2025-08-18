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

import java.sql.SQLException;

import jakarta.servlet.annotation.WebServlet;

import com.roth.base.log.Log;
import com.roth.base.util.Data;
import com.roth.jdbc.util.TableUtil;
import com.roth.portal.model.DomainBean;
import com.roth.portal.model.UserBean;
import com.roth.portal.model.UserProfile;
import com.roth.portal.model.UserProperty;
import com.roth.portal.util.PasswordUtil;
import com.roth.portal.util.Portal;
import com.roth.realm.util.RothRealmUtil;
import com.roth.servlet.annotation.ActionServletSecurity;
import com.roth.servlet.annotation.Navigation;

@WebServlet(urlPatterns = "/Profile/*")
@ActionServletSecurity(roles = "Authenticated")
@Navigation()
public class Profile extends ActionServlet {
	private static final long serialVersionUID = 1L;

	@Action(forwards = { @Forward(name = SUCCESS, path = "index.jsp") })
	public String begin(ActionConnection conn) {
		return SUCCESS;
	}
	
	@Action(forwards = { @Forward(name = SUCCESS, path = "edit.jsp") },
			responses = { @Response(name = FAILURE, httpStatusCode = 500) })
	public String edit(ActionConnection conn) {
		try {
			TableUtil util = new TableUtil("roth");
			String userid = getUserName(conn);
			String _userid = userid.split("@")[0];
	    	String domainName = (_userid.equals(userid)) ? "default" : userid.split("@")[1];
			String filter = util.applyParameters("userid = {1} AND domain_id = {2}", _userid, new Portal().getDomainId(domainName));
			UserBean user = util.get(UserBean.class, filter);
			putBean(user.getName(), "userFullName", "request", conn);
			putBean(UserProfile.getUserProfile(getUserName(conn)), "profile", "request", conn);
			putBean(util.get(DomainBean.class, util.applyParameters("domain_name = {1}", conn.getDomainName())), "domain", "request", conn);
		}
		catch (SQLException e) {
			Log.logException(e, getUserName(conn));
			conn.println("Unable to retrieve user profile due to a database error.");
			return FAILURE;
		}
		return SUCCESS;
	}
	
	@Action(responses = { @Response(name = SUCCESS, message = "User profile successfully saved."),
			              @Response(name = FAILURE, httpStatusCode = 500) })
	@Post(beans = { @Bean(name = "profile", scope = "request", beanClass = UserProfile.class) })
	public String save(ActionConnection conn) {
		try { 
			UserProfile profile = getBean(0, conn);
			UserProfile.setUserProfile(profile);
		}
		catch (SQLException e) {
			Log.logException(e, getUserName(conn));
			conn.println("Unable to save user profile due to a database error.");
			return FAILURE;
		}
		return SUCCESS;
	}
	
	@Action(responses = { @Response(name = SUCCESS, message = SUCCESS),
			              @Response(name = "unathorized", httpStatusCode = 401),
			              @Response(name = "notfound", httpStatusCode = 404),
			              @Response(name = "invalid", httpStatusCode = 406),
			              @Response(name = FAILURE, httpStatusCode = 500) })
	public String changePassword(ActionConnection conn) {
		conn.getResponse().setContentType("text/plain");
		// If the user is not logged in, then return not found status.
		if (conn.getRequest().getUserPrincipal() == null)
			return "notfound";
		String userid = conn.getUserName(); 
		String oldPassword = conn.getString("oldPassword");
		String newPassword = conn.getString("newPassword");
		if (userid.equalsIgnoreCase(newPassword) || userid.contains(newPassword)) {
			conn.println("Invalid password: You cannot use a password derived from the userid.");
			return "invalid";
		}
		String[] result = PasswordUtil.changePassword(userid, oldPassword, newPassword).split(":");
		if (result.length > 1)
			conn.println(result[1]);
		return result[0];
	}
	
    @Action(responses = { @Response(name = SUCCESS, message = "You are still logged in.") })
    public String keepAlive(ActionConnection conn) { return SUCCESS; }
    
    @Action(responses = { @Response(name = SUCCESS) })
    public String getDaysToExpire(ActionConnection conn) {
    	try {
	    	Integer dte = new RothRealmUtil().getDaysToExpire(conn.getUserName());
	    	String result = Data.nvl(Data.integerToStr(dte));
	    	conn.getResponse().setContentType("text/plain");
	    	conn.print(result);
    	}
    	catch (SQLException e) { Log.logException(e, conn.getUserName()); }
    	return SUCCESS; 
    }
    
    @Action(forwards = { @Forward(name = SUCCESS, path = "reset.jsp"),
    		             @Forward(name = "getpin", path = "getpin.jsp") },
            responses = { @Response(name = FAILURE, httpStatusCode = 500) })
    public String getResetInfo(ActionConnection conn) {
    	// Password reset is based on a PIN and email.  If the PIN is valid,
    	// then an email with a generated validation number is sent to the
    	// stored email address.  The validation number will expire in 5 minutes.
    	try {
	    	UserProperty p = UserProperty.getUserProperty(conn.getUserName(), "_reset_pin");
	    	String compare = null;
	    	String pin = Data.envl(conn.getString("pin"));
	    	boolean redo = Data.nvl(conn.getString("redo")).equals("Y");
	    	if ((pin == null) && (p.getPropertyValue() != null) && !redo)
	    		return "getpin";
	    	else if (redo || ((pin == null) && (p.getPropertyValue() == null)))
	    		return SUCCESS;
	    	compare = Data.decrypt(p.getPropertyValue(), Long.valueOf(pin));
	    	if (!compare.equals(pin)) {
	    		// If the decryption succeeded, but the decrypted value is invalid...
	    		conn.println("The entered PIN does not match.");
	    		return FAILURE;
	    	}
	    	UserProperty e = UserProperty.getUserProperty(conn.getUserName(), "_reset_email");
	    	String email = Data.decrypt(e.getPropertyValue(), Long.valueOf(pin));
	    	
	    	putBean(pin, "pin", "request", conn);
	    	putBean(email, "email", "request", conn);
    	}
    	catch (NumberFormatException e) {
    		// Decryption may fail when the seed is bad. 
    		return returnLogException(e, conn, FAILURE, "The entered PIN does not match.");
    	}
    	catch (SQLException e) {
    		return returnLogException(e, conn, FAILURE, "Unable to get stored reset information.");
    	}
    	catch (Exception e) {
    		return returnLogException(e, conn, FAILURE, "An unexpected error occurred.");
    	}
    	
    	return SUCCESS;
    }
    
    @Action(forwards = { @Forward(name = SUCCESS, action = "getResetInfo") },
            responses = { @Response(name = FAILURE, httpStatusCode = 500) })
    public String saveResetInfo(ActionConnection conn) {
    	String pin = Data.nvl(conn.getString("pin"), conn.getString("storedPin"));
    	String email = conn.getString("email");
    	try {
    		UserProperty p = UserProperty.getUserProperty(conn.getUserName(), "_reset_pin");
	    	p.setPropertyValue(Data.encrypt(pin, Long.valueOf(pin)));
	    	UserProperty.setUserProperty(p);
	    	
	    	UserProperty e = UserProperty.getUserProperty(conn.getUserName(), "_reset_email");
	    	e.setPropertyValue(Data.encrypt(email, Long.valueOf(pin)));
	    	UserProperty.setUserProperty(e);
    	}
    	catch (Exception e) {
    		return returnLogException(e, conn, FAILURE, "ERROR: Unable to save reset information.");
    	}
    	
    	return SUCCESS;
    }
 }
