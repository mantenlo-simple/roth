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

import com.roth.base.log.Log;
import com.roth.base.util.Data;
import com.roth.portal.db.PortalUtil;
import com.roth.portal.util.AuthenticationUtil;
import com.roth.portal.util.PasswordUtil;
import com.roth.realm.RothRealm;
import com.roth.realm.util.RothRealmUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;

@WebServlet( urlPatterns = "/AuthenticationServlet/*" )
public class AuthenticationServlet extends ActionServlet {
	private static final long serialVersionUID = 1L;

	@Action(responses = { @Response(name = "404", httpStatusCode = 404) })
	public String begin(ActionConnection conn) {
		String id = getCookie(conn, "JSESSIONIDSSO", null) +
		            conn.getRequest().getHeader("User-Agent") +
		            conn.getRequest().getRemoteAddr();
		System.out.println(id);
		String crypt = mergeCrypt("jpayne", "password");
		System.out.println("[" + crypt + "]");
		System.out.println("[" + splitDecrypt(crypt) + "]");
		
		String c = Data.encrypt("Hello", 5);
		System.out.println("[encrypt]" + c);
		System.out.println("[decrypt]" + Data.decrypt(c, 5));
		return "404";
	}
	
	private String mergeCrypt(String username, String password) {
		String result = Data.toHexString(username.length(), 2) + username + password;
		return Data.encrypt(result, 42);
	}
	
	private String splitDecrypt(String crypt) {
		crypt = Data.decrypt(crypt, 42);
		int length = Integer.parseInt(crypt.substring(0, 2), 16);
		String username = crypt.substring(2, 2 + length);
		String password = crypt.substring(2 + length);
		return username + "|" + password;
	}
	
	/**
	 * Used to login through AJAX.
	 * Expected POST parameters: j_username, j_password
	 * @param conn
	 * @return
	 */
	@Action(responses = { @Response(name = SUCCESS, message = SUCCESS),
                          @Response(name = FAILURE, httpStatusCode = 401) })
	public String login(ActionConnection conn) {
		//String ssoid = getCookie(conn, "JSESSIONIDSSO", null);
		String username = conn.getRequest().getParameter("j_username");
		String password = conn.getRequest().getParameter("j_password");
		try { conn.getRequest().login(username, password); }
		catch (ServletException e) { return FAILURE; }
		return SUCCESS;
	}
	
	/**
	 * Used to logout through AJAX.
	 * No POST parameters expected.
	 * @param conn
	 * @return
	 */
	@Action(responses = { @Response(name = SUCCESS, message = SUCCESS),
                          @Response(name = FAILURE, httpStatusCode = 500) })
	public String logout(ActionConnection conn) {
		try { conn.getRequest().logout(); }
		catch (ServletException e) { return FAILURE; }
		return SUCCESS;
	}
	
	@Action(responses = { @Response(name = SUCCESS, message = SUCCESS),
				          @Response(name = "unathorized", httpStatusCode = 401),
				          @Response(name = "notfound", httpStatusCode = 404),
				          @Response(name = "invalid", httpStatusCode = 406),
				          @Response(name = FAILURE, httpStatusCode = 500) })
	public String changeExpiredPassword(ActionConnection conn) {
		String userid = conn.getString("userid"); 
		String oldPassword = conn.getString("oldPassword");
		String newPassword = conn.getString("newPassword");
		String[] result = PasswordUtil.changePassword(userid, oldPassword, newPassword).split(":");
		if (result.length > 1)
			conn.println(result[1]);
		return result[0];
	}
	
	@Action(forwards = { @Forward(name = SUCCESS, path = "changepassword.jsp") },
			responses = { @Response(name = FAILURE, httpStatusCode = 500, message = "The username or PIN is invalid.") })
	public String getValidationCode(ActionConnection conn) {
		String userid = conn.getString("userid");
		String pin = conn.getString("pin");
		
		if (AuthenticationUtil.validatePin(userid, pin) != AuthenticationUtil.PIN_VALID)
			return FAILURE;
		
		AuthenticationUtil.sendValidationCode(userid, pin);
		putBean("forgotten", "true", "request", conn);
		return SUCCESS;
	}
	
	@Action(responses = { @Response(name = SUCCESS, message = SUCCESS),
				          @Response(name = "unathorized", httpStatusCode = 401),
				          @Response(name = "notfound", httpStatusCode = 404),
				          @Response(name = "invalid", httpStatusCode = 406),
				          @Response(name = FAILURE, httpStatusCode = 500) })
	public String changeForgottenPassword(ActionConnection conn) {
		String userid = conn.getString("userid");
		String validationCode = conn.getString("validationCode");
		String newPassword = conn.getString("newPassword");
		String[] result = PasswordUtil.changeForgottenPassword(userid, validationCode, newPassword).split(":");
		if (result.length > 1)
			conn.println(result[1]);
		return result[0];
	}
	
	@Action(responses = { @Response(name = SUCCESS) })
	public String getDaysToExpire(ActionConnection conn) {
		Integer dte = null;
		String userid = conn.getString("userid");
		String password = conn.getString("password");
		try {
			RothRealmUtil util = new RothRealmUtil();
			int dPos = userid.indexOf("@");
			// If the password is authorized from a secondary source, then the expiration date is unknown.
			if (util.getPasswordSource(userid.substring(dPos + 1)) != null) {
				conn.print("");
				return SUCCESS;
			}
			String stored = util.getExpiredPassword(userid);
			if (!compareCredentials(stored, password)) {
				conn.print(new PortalUtil().canReset(userid) ? "canReset" : "");
				return SUCCESS;
			}
			dte = util.getDaysToExpire(userid);
			String result = Data.nvl(Data.integerToStr(dte));
	    	conn.print(result);
		}
		catch (Exception e) { Log.logException(e, userid); }
    	return SUCCESS; 
    }
	
	protected static boolean compareCredentials(String stored, String entered) {
        if (stored == null) return false;
        /*
         * The length of digested strings depend on the method used. 
         * If the *stored* string length is a known length, then compare 
         * the digested string using the appropriate digest algorithm.  
         * Otherwise, the stored password is not assumed to be encrypted. 
         */
        String compare = (stored.length() == 256) ? RothRealm.digest(entered, "SHA3-512")
        			   : (stored.length() == 128) ? RothRealm.digest(entered, "SHA-512")
        		       : (stored.length() ==  64) ? RothRealm.digest(entered, "SHA-256")
        		       : (stored.length() ==  40) ? RothRealm.digest(entered, "SHA") 
                       : (stored.length() ==  32) ? RothRealm.digest(entered, "MD5")
                       : entered;
        return stored.equals(compare);
    }
}
