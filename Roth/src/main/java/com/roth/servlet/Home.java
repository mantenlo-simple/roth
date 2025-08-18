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

import com.roth.portal.db.PortalUtil;

import jakarta.servlet.annotation.WebServlet;

@WebServlet( urlPatterns = "/Home/*" )
public class Home extends ActionServlet {
	private static final long serialVersionUID = 1L;

	@Action(forwards = { @Forward(name = "success", path = "/index.jsp"/*, pathMobi = "/indexMobi.jsp"*/) },
			responses = { @Response(name = "failure", httpStatusCode = 500) })
	public String begin(ActionConnection conn) {
		//Log.setLogLevel(Log.LOG_DEBUG);
		//Log.setLogStackTrace(true);
		try {
			if (conn.getRequest().getUserPrincipal() == null)
				return "success";
			conn.getRequest().setAttribute("homeInit", "true");
			String userid = conn.getUserName();
			PortalUtil util = new PortalUtil();
			putBean(util.getAvailableDesktops(userid), "availableDesktops", "request", conn);
			putBean(util.getLinks(userid), "links", "request", conn);
			/*
			String where = "(domain_id IS NULL " +
						"OR  (domain_id = {1} " +
					   "AND   group_id IS NULL) " +
					    "OR  (SELECT group_concat(g.lineage) " +
					           "FROM `group` g, " +
					                "user_group ug " +
					          "WHERE ug.group_id = g.group_id " +
					            "AND ug.userid = {2} " +
					            "AND ug.domain_id = {1}) LIKE CONCAT('% ', group_id, ' %')) " +
					   "AND language_code = {3}";
			where = util.applyParameters(where, conn.getDomainId(), conn.getUserid(), conn.getLocale().toLowerCase());
			Log.logDebug(where, conn.getUserName());
			putBean(util.getList(NewsBean.class, where, "sticky DESC, post_dts DESC", null), "news", "request", conn);
			*/
			
			//Context env = (Context)new InitialContext().lookup("java:comp/env");
			//System.out.println("rothTemp: " + (String)env.lookup("rothTemp"));
			
			//System.out.println("Temp: " + System.getenv("CATALINA_TMPDIR"));
			//System.out.println("Base: " + System.getenv("CATALINA_BASE"));
			//System.out.println("Home: " + System.getenv("CATALINA_HOME"));
		} 
		catch (Exception e) { return returnLogException(e, conn, "failure"); }
		return "success";
	}
	
	@Action(responses = { @Response(name = "success") })
	@MethodSecurity(roles = "Authenticated", methods = "GET")
	public String reflect(ActionConnection conn) {
		conn.getResponse().setHeader("X-Frame-Options", "SAMEORIGIN");
		conn.getResponse().setContentType("text/html");
		String output = conn.getString("src", "").replace("<base href=\"http://localhost:8180/\" /><meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />", "<meta charset=\"UTF-8\">");
		conn.print(output);
		return SUCCESS;
	}
}
