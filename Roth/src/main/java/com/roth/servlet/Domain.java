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
import java.util.ArrayList;

import jakarta.servlet.annotation.WebServlet;

import com.roth.jdbc.util.TableUtil;
import com.roth.portal.db.PortalUtil;
import com.roth.portal.model.DomainBean;
import com.roth.portal.model.DomainPropertyBean;
import com.roth.portal.model.DomainRoleBean;
import com.roth.portal.model.UserDomainBean;
import com.roth.servlet.annotation.ActionServletSecurity;
import com.roth.servlet.annotation.Navigation;
import com.roth.servlet.annotation.SimpleAction;

@WebServlet(urlPatterns = "/Domain/*")
@ActionServletSecurity(roles = {"SecurityAdmin", "DomainAdmin"})
@Navigation(contextPath = "/configuration",
	        simpleActions = { @SimpleAction(name = "begin", action = "load") })
public class Domain extends ActionServlet {
	private static final long serialVersionUID = 1L;

	@Action(forwards = { @Forward(name = "success", path = "/configuration/index.jsp"),
                         @Forward(name = "ajax", path = "_list.jsp") },
            responses = { @Response(name = "failure", httpStatusCode = 500) })
    public String load(ActionConnection conn) {
		putBean("domain", "tabpage", "request", conn);
		try {
			PortalUtil util = new PortalUtil();
			boolean securityAdmin = conn.getRequest().isUserInRole("SecurityAdmin");
			String where = securityAdmin ? null : util.applyParameters("domain_id = {1}", util.getDomainId(conn.getDomainName()));
			putBean(util.getList(DomainBean.class, where), "domains", "request", conn);
		} 
		catch (Exception e) { return returnLogException(e, conn, "failure"); }
		return isCallingActionName(BEGIN, conn) ? SUCCESS : "ajax";
	}
	
	@Action(forwards = { @Forward(name = "success", path = "_edit.jsp") },
            responses = { @Response(name = "failure", httpStatusCode = 500) })
	public String edit(ActionConnection conn) {
		DomainBean bean = new DomainBean();
		Long domainId = conn.getLong("domainId");
		String domainName = conn.getString("domainName");
		try {
			TableUtil util = new TableUtil("roth");
			if (domainId != null)
				bean = util.get(DomainBean.class, util.applyParameters("domain_id = {1}", domainId));
			else if (domainName != null)
				bean = util.get(DomainBean.class, util.applyParameters("domain_name = {1}", domainName));
		}
		catch (SQLException e) { return returnLogException(e, conn, "failure"); }
		putBean(bean, "domain", "request", conn);
		return "success";
	}
	
	@Action(forwards = { @Forward(name = "success", action = "load") },
            responses = { @Response(name = "failure", httpStatusCode = 500) })
	@Post(beans = { @Bean(name = "domain", beanClass = DomainBean.class, scope = "request") })
	public String save(ActionConnection conn) {
		DomainBean domain = getBean(0, conn);
		if (domain.getPwdChallengeMin() == null)
			domain.setPwdChallengeMin(1);
		if (domain.getPwdAllowReset() == null)
			domain.setPwdAllowReset("N");
		domain.setUpdatedBy(getUserName(conn));
		try { new PortalUtil().save(domain); }
		catch (SQLException e) { return returnLogException(e, conn, "failure"); }
    	return "success";
	}
	
	@Action(forwards = { @Forward(name = "success", action = "load"),
			             @Forward(name = "return", path = "/configuration/index.jsp") },
			responses = { @Response(name = "default", httpStatusCode = 403, message = "The 'default' domain cannot be deleted."),
			              @Response(name = "failure", httpStatusCode = 500) })
	@Post(beans = { @Bean(name = "domain", beanClass = DomainBean.class, scope = "request") })
	public String delete(ActionConnection conn) {
		DomainBean domain = getBean(0, conn);
		if (domain.getDomainId() == 0) return "default";
		try { new PortalUtil().delete(domain); }
		catch (SQLException e) { return returnLogException(e, conn, "failure"); }
    	return "success";
	}
	
	@Action(forwards = { @Forward(name = "success", path = "__users.jsp") },
            responses = { @Response(name = "failure", httpStatusCode = 500) })
    public String loadUsers(ActionConnection conn) {
		Long domainId = conn.getLong("domainId");
		try { conn.getRequest().setAttribute("domainUsers", new PortalUtil().getUserDomains(null, domainId)); } 
		catch (SQLException e) { return returnLogException(e, conn, "failure"); }
		return "success";
	}
	
	@Action(forwards = { @Forward(name = "success", action = "loadUsers") },
            responses = { @Response(name = "failure", httpStatusCode = 500) })
	@Post(beans = { @Bean(name = "domainUsers", scope = "request", beanClass = UserDomainBean.class, typeClass = ArrayList.class) })
	public String saveUsers(ActionConnection conn) {
		ArrayList<UserDomainBean> beans = getBean(0, conn);
		Long domainId = conn.getLong("domainId");
		for (int i = 0; i < beans.size(); i++)
			beans.get(i).setUpdatedBy(getUserName(conn));
		try { new PortalUtil().saveUserDomains(beans, null, domainId); }
		catch (SQLException e) { return returnLogException(e, conn, "failure"); }
    	return "success";
	}
	
	@Action(forwards = { @Forward(name = "success", path = "__roles.jsp") },
            responses = { @Response(name = "failure", httpStatusCode = 500) })
    public String loadRoles(ActionConnection conn) {
		Long domainId = conn.getLong("domainId");
		try { 
			PortalUtil util = new PortalUtil();
			conn.getRequest().setAttribute("domainRoles", util.getDomainRoles(domainId, null)); 
		} 
		catch (SQLException e) { return returnLogException(e, conn, "failure"); }
		return "success";
	}
	
	@Action(forwards = { @Forward(name = "success", action = "loadRoles") },
            responses = { @Response(name = "failure", httpStatusCode = 500) })
	@Post(beans = { @Bean(name = "domainRoles", scope = "request", beanClass = DomainRoleBean.class, typeClass = ArrayList.class) })
	public String saveRoles(ActionConnection conn) {
		ArrayList<DomainRoleBean> beans = getBean(0, conn);
		Long domainId = conn.getLong("domainId");
		for (int i = 0; i < beans.size(); i++)
			beans.get(i).setCreatedBy(getUserName(conn));
		try { new PortalUtil().saveDomainRoles(beans, domainId, null); }
		catch (SQLException e) { return returnLogException(e, conn, "failure"); }
    	return "success";
	}
	
	@Action(forwards = { @Forward(name = "success", path = "__properties.jsp") },
            responses = { @Response(name = "failure", httpStatusCode = 500) })
    public String loadProperties(ActionConnection conn) {
		Long domainId = conn.getLong("domainId");
        try { 
        	PortalUtil util = new PortalUtil();
        	String filter = util.applyParameters("domain_id = {1}", domainId);
        	conn.getRequest().setAttribute("domainProperties", util.getList(DomainPropertyBean.class, filter)); 
        } 
        catch (SQLException e) { return returnLogException(e, conn, "failure"); }
        return "success";
    }
    
    @Action(forwards = { @Forward(name = "success", path = "___editproperty.jsp") },
            responses = { @Response(name = "failure", httpStatusCode = 500) })
    public String editProperty(ActionConnection conn) {
    	Long domainId = conn.getLong("domainId");
        String propertyName = conn.getRequest().getParameter("propertyName");
        try {
        	PortalUtil util = new PortalUtil();
        	String filter = util.applyParameters("domain_id = {1} AND property_name = {2}", domainId, propertyName);
	        if (propertyName != null)
	            putBean(util.get(DomainPropertyBean.class, filter), "domainProperty", "request", conn);
	        else {
	        	DomainPropertyBean bean = new DomainPropertyBean();
	            bean.setDomainId(domainId);
	            putBean(bean, "domainProperty", "request", conn);
	        }
        }
        catch (SQLException e) { return returnLogException(e, conn, "failure"); }
        return "success";
    }
    
    @Action(forwards = { @Forward(name = "success", action = "loadProperties") },
            responses = { @Response(name = "failure", httpStatusCode = 500) })
    @Post(beans = { @Bean(name = "domainProperty", scope = "request", beanClass = DomainPropertyBean.class) })
    public String saveProperty(ActionConnection conn) {
    	DomainPropertyBean bean = getBean(0, conn);
        bean.setUpdatedBy(getUserName(conn));
        try { new PortalUtil().save(bean); }
        catch (SQLException e) { return returnLogException(e, conn, "failure"); }
        return "success";
    }
    
    @Action(forwards = { @Forward(name = "success", action = "loadProperties") },
            responses = { @Response(name = "failure", httpStatusCode = 500) })
    @Post(beans = { @Bean(name = "domainProperty", scope = "request", beanClass = DomainPropertyBean.class) })
    public String deleteProperty(ActionConnection conn) {
    	DomainPropertyBean bean = getBean(0, conn);
        try { new PortalUtil().delete(bean); }
        catch (SQLException e) { return returnLogException(e, conn, "failure"); }
        return "success";
    }
}
