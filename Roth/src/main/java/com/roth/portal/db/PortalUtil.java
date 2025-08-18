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
package com.roth.portal.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import com.roth.base.log.Log;
import com.roth.base.util.Data;
import com.roth.portal.model.BookBean;
import com.roth.portal.model.BookContentBean;
import com.roth.portal.model.DesktopBean;
import com.roth.portal.model.DesktopLinkBean;
import com.roth.portal.model.DomainRoleBean;
import com.roth.portal.model.GroupBean;
import com.roth.portal.model.GroupRoleBean;
import com.roth.portal.model.LinkBean;
import com.roth.portal.model.LinkRoleBean;
import com.roth.portal.model.PageBean;
import com.roth.portal.model.PortletRoleBean;
import com.roth.portal.model.ThemeLinkBean;
import com.roth.portal.model.UserDomainBean;
import com.roth.portal.model.UserGroupBean;
import com.roth.portal.model.UserRoleBean;
import com.roth.jdbc.annotation.ConnectionDataSource;
import com.roth.jdbc.annotation.SQLFileContext;
import com.roth.jdbc.util.TableUtil;

@ConnectionDataSource(jndiName = "roth")
@SQLFileContext(path = "/com/roth/portal/sql")
public class PortalUtil extends TableUtil {
	private static final long serialVersionUID = 3797862336668817596L;

	public PortalUtil() throws SQLException { super(); }
	
	@Override
	protected void openCallback(Connection conn) throws SQLException {
		setSchema("roth");
	}
	
	// Get Book Contents
	public ArrayList<BookContentBean> getBookContents(Long bookId) throws SQLException {
		String statement = getSQLFile("getBookContents.sql");
		statement = applyParameters(statement, bookId);
		Log.logDebug(statement, null, "getBookContents");
		return execQuery(statement, ArrayList.class, BookContentBean.class);
	}
	
	// Get Portlets Not In Book
	public LinkedHashMap<String,String> getPortletsNotInBook(Long bookId) throws SQLException {
		String statement = getSQLFile("getPortletsNotInBook.sql");
		statement = applyParameters(statement, bookId);
		return execQuery(statement, LinkedHashMap.class);
	}
	
	public void movePage(Long bookId, Long sequence, int direction) throws SQLException {
		String statement = "CALL move_book_content({1}, {2}, {3});";
		statement = applyParameters(statement, bookId, sequence, direction);
		execUpdate(statement);
	}
	
	public void deletePage(PageBean bean) throws SQLException {
		delete(bean);
		String[] statements = {"UPDATE page SET sequence = sequence - 1 WHERE book_id = {bookId} AND sequence > {sequence}",
				               "UPDATE book SET sequence = sequence - 1 WHERE parent_book_id = {bookId} AND sequence > {sequence}"};
		statements[0] = applyParameterBean(statements[0], bean);
		statements[1] = applyParameterBean(statements[1], bean);
		execUpdate(statements);
	}
	
	// Save Book
	public void saveBook(BookBean bean) throws SQLException {
		save(bean);
		String statement = "CALL update_book_descendants({bookId})";
		statement = applyParameterBean(statement, bean);
		execUpdate(statement);
	}
	
	// Save Group
	public void saveGroup(GroupBean bean) throws SQLException {
		save(bean);
		String statement = "CALL update_group_descendants({groupId})";
		statement = applyParameterBean(statement, bean);
		execUpdate(statement);
	}
	
	// Get User-Groups
	public ArrayList<UserGroupBean> getUserGroups(String userid, Long domainId, Long groupId) throws SQLException {
		String filename = (userid != null) ? "getUserGroupsByUser.sql" : "getUserGroupsByGroup.sql";
		String statement = getSQLFile(filename);
		statement = applyParameters(statement, userid, domainId, groupId);
		return execQuery(statement, ArrayList.class, UserGroupBean.class);
	}
	
	// Save User-Groups
	public void saveUserGroups(ArrayList<UserGroupBean> beans, String userid, Long groupId) throws SQLException {
		ArrayList<UserGroupBean> deletes = new ArrayList<UserGroupBean>();
		ArrayList<UserGroupBean> inserts = new ArrayList<UserGroupBean>();
		
		for (int i = 0; i < beans.size(); i++) {
			UserGroupBean bean = beans.get(i);
			Object key = (userid != null) ? bean.getUserid() : bean.getGroupId();
			
			if ((key != null) && (bean.getCreatedDts() == null))
				inserts.add(bean);
			else if ((key == null) && (bean.getCreatedDts() != null)) { 
				deletes.add(bean);
				if (userid != null) bean.setUserid(userid);
				else bean.setGroupId(groupId);
			}
		}
		
		if (deletes.size() > 0) delete(deletes);
		if (inserts.size() > 0) save(inserts);
	}
	
	// Get User-Roles
	public ArrayList<UserRoleBean> getUserRoles(String userid, Long domainId, String roleName, boolean filterByDomain) throws SQLException {
		String filename = (userid != null) ? "getUserRolesByUser.sql" : "getUserRolesByRole.sql";
		String statement = getSQLFile(filename);
		String filter = "AND EXISTS (SELECT dr.role_name FROM domain_role dr WHERE dr.domain_id = {1} AND dr.role_name = r.role_name) ";
		filter = applyParameters(filter, domainId);
		statement = applyParameters(statement, userid, domainId, roleName, filterByDomain ? filter : "");
		return execQuery(statement, ArrayList.class, UserRoleBean.class);
	}
	
	// Save User-Roles
	public void saveUserRoles(ArrayList<UserRoleBean> beans, String userid, String roleName) throws SQLException {
		ArrayList<UserRoleBean> deletes = new ArrayList<UserRoleBean>();
		ArrayList<UserRoleBean> inserts = new ArrayList<UserRoleBean>();
		
		for (int i = 0; i < beans.size(); i++) {
			UserRoleBean bean = beans.get(i);
			String key = (userid != null) ? bean.getUserid() : bean.getRoleName();
			
			if ((key != null) && (bean.getCreatedDts() == null))
				inserts.add(bean);
			else if ((key == null) && (bean.getCreatedDts() != null)) { 
				deletes.add(bean);
				if (userid != null) bean.setUserid(userid);
				else bean.setRoleName(roleName);
			}
		}
		
		if (deletes.size() > 0) delete(deletes);
		if (inserts.size() > 0) save(inserts);
	}
	
	// Get User-Domains
	public ArrayList<UserDomainBean> getUserDomains(String userid, Long domainId) throws SQLException {
		String filename = (userid != null) ? "getUserDomainsByUser.sql" : "getUserDomainsByDomain.sql";
		String statement = getSQLFile(filename);
		statement = applyParameters(statement, userid, domainId);
		return execQuery(statement, ArrayList.class, UserDomainBean.class);
	}
	
	// Save User-Domains
	public void saveUserDomains(ArrayList<UserDomainBean> beans, String userid, Long domainId) throws SQLException {
		ArrayList<UserDomainBean> deletes = new ArrayList<UserDomainBean>();
		ArrayList<UserDomainBean> inserts = new ArrayList<UserDomainBean>();
		
		for (int i = 0; i < beans.size(); i++) {
			UserDomainBean bean = beans.get(i);
			Object key = (userid != null) ? bean.getUserid() : bean.getDomainId();
			
			if ((key != null) && (bean.getUpdatedDts() == null))
				inserts.add(bean);
			else if ((key == null) && (bean.getUpdatedDts() != null)) { 
				deletes.add(bean);
				if (userid != null) bean.setUserid(userid);
				else bean.setDomainId(domainId);
			}
		}
		
		if (deletes.size() > 0) delete(deletes);
		if (inserts.size() > 0) save(inserts);
	}
	
	// Get Domain-Roles
	public ArrayList<DomainRoleBean> getDomainRoles(Long domainId, String roleName) throws SQLException {
		String filename = (domainId != null) ? "getDomainRolesByDomain.sql" : "getDomainRolesByRole.sql";
		String statement = getSQLFile(filename);
		statement = applyParameters(statement, domainId, roleName);
		return execQuery(statement, ArrayList.class, DomainRoleBean.class);
	}
	
	// Save Domain-Roles
	public void saveDomainRoles(ArrayList<DomainRoleBean> beans, Long domainId, String roleName) throws SQLException {
		ArrayList<DomainRoleBean> deletes = new ArrayList<DomainRoleBean>();
		ArrayList<DomainRoleBean> inserts = new ArrayList<DomainRoleBean>();
		
		for (int i = 0; i < beans.size(); i++) {
			DomainRoleBean bean = beans.get(i);
			Object key = (domainId != null) ? bean.getDomainId() : bean.getRoleName();
			
			if ((key != null) && (bean.getCreatedDts() == null))
				inserts.add(bean);
			else if ((key == null) && (bean.getCreatedDts() != null)) { 
				deletes.add(bean);
				if (domainId != null) bean.setDomainId(domainId);
				else bean.setRoleName(roleName);
			}
		}
		
		if (deletes.size() > 0) delete(deletes);
		if (inserts.size() > 0) save(inserts);
	}
	
	// Get Group-Roles
	public ArrayList<GroupRoleBean> getGroupRoles(Long groupId, String roleName, Long domainId) throws SQLException {
		String filename = (groupId != null) ? "getGroupRolesByGroup.sql" : "getGroupRolesByRole.sql";
		String statement = getSQLFile(filename);
		String filter = "AND EXISTS (SELECT dr.role_name FROM domain_role dr WHERE dr.domain_id = {1} AND dr.role_name = r.role_name) ";
		filter = applyParameters(filter, domainId);
		statement = applyParameters(statement, groupId, roleName, domainId != null ? filter : "");
		return execQuery(statement, ArrayList.class, GroupRoleBean.class);
	}
	
	// Save Group-Roles
	public void saveGroupRoles(ArrayList<GroupRoleBean> beans, Long groupId, String roleName) throws SQLException {
		ArrayList<GroupRoleBean> deletes = new ArrayList<GroupRoleBean>();
		ArrayList<GroupRoleBean> inserts = new ArrayList<GroupRoleBean>();
		
		for (int i = 0; i < beans.size(); i++) {
			GroupRoleBean bean = beans.get(i);
			Object key = (groupId != null) ? bean.getGroupId() : bean.getRoleName();
			
			if ((key != null) && (bean.getCreatedDts() == null))
				inserts.add(bean);
			else if ((key == null) && (bean.getCreatedDts() != null)) { 
				deletes.add(bean);
				if (groupId != null) bean.setGroupId(groupId);
				else bean.setRoleName(roleName);
			}
		}
		
		if (deletes.size() > 0) delete(deletes);
		if (inserts.size() > 0) save(inserts);
	}

	// Get Portlet-Roles
	public ArrayList<PortletRoleBean> getPortletRoles(String roleName, Long portletId) throws SQLException {
		String filename = (roleName != null) ? "getPortletRolesByRole.sql" : "getPortletRolesByPortlet.sql";
		String statement = getSQLFile(filename);
		statement = applyParameters(statement, roleName, portletId);
		return execQuery(statement, ArrayList.class, PortletRoleBean.class);
	}
	
	// Save Portlet-Roles
	public void savePortletRoles(ArrayList<PortletRoleBean> beans, String roleName, Long portletId) throws SQLException {
		ArrayList<PortletRoleBean> deletes = new ArrayList<PortletRoleBean>();
		ArrayList<PortletRoleBean> inserts = new ArrayList<PortletRoleBean>();
		
		for (int i = 0; i < beans.size(); i++) {
			PortletRoleBean bean = beans.get(i);
			Object key = (roleName != null) ? bean.getRoleName() : bean.getPortletId();
			
			if ((key != null) && (bean.getCreatedDts() == null))
				inserts.add(bean);
			else if ((key == null) && (bean.getCreatedDts() != null)) { 
				deletes.add(bean);
				if (roleName != null) bean.setRoleName(roleName);
				else bean.setPortletId(portletId);
			}
		}
		
		if (deletes.size() > 0) delete(deletes);
		if (inserts.size() > 0) save(inserts);
	}
	
	/* Desktop Menu (Home Page) Support */
	public ArrayList<DesktopBean> getAvailableDesktops(String userid) throws SQLException {
		int dPos = userid.indexOf("@");
        String _domain = dPos < 0 ? "default" : userid.substring(dPos + 1);
        String _userid = dPos < 0 ? userid : userid.substring(0, dPos);
        String statement = getSQLFile("getAvailableDesktops.sql"); 
		statement = applyParameters(statement, _domain, _userid);
		return execQuery(statement, ArrayList.class, DesktopBean.class);
	}
	
	// Dec[rement] Desktop Links
	public int decDesktopLinks(DesktopLinkBean bean) throws SQLException {
		String statement = getSQLFile("decDesktopLinks.sql");
		statement = applyParameterBean(statement, bean);
		return execUpdate(statement);
	}
	
	// Dec[rement] Theme Links
	public int decThemeLinks(ThemeLinkBean bean) throws SQLException {
		String statement = getSQLFile("decThemeLinks.sql");
		statement = applyParameterBean(statement, bean);
		return execUpdate(statement);
	}

	// Swap Desktop Links
	public int[] swapDesktopLinks(Long desktopId, Long sequenceA, Long sequenceB) throws SQLException {
		String statement = getSQLFile("updateDesktopLinkSequence.sql");
		String[] statements = new String[3];
		statements[0] = applyParameters(statement, desktopId,        -1, sequenceA);
		statements[1] = applyParameters(statement, desktopId, sequenceA, sequenceB);
		statements[2] = applyParameters(statement, desktopId, sequenceB,        -1);
		return execUpdate(statements);
	}
	
	// Swap Desktop Links
	public int[] swapThemeLinks(Long themeId, Long sequenceA, Long sequenceB) throws SQLException {
		String statement = getSQLFile("updateThemeLinkSequence.sql");
		String[] statements = new String[3];
		statements[0] = applyParameters(statement, themeId,        -1, sequenceA);
		statements[1] = applyParameters(statement, themeId, sequenceA, sequenceB);
		statements[2] = applyParameters(statement, themeId, sequenceB,        -1);
		return execUpdate(statements);
	}
	
	// Get Links
	public ArrayList<LinkBean> getLinks(String userid) throws SQLException {
		int dPos = userid.indexOf("@");
        String _domain = dPos < 0 ? "default" : userid.substring(dPos + 1);
        String _userid = dPos < 0 ? userid : userid.substring(0, dPos);
		String statement = getSQLFile("getLinks.sql");
		statement = applyParameters(statement, userid, _domain, _userid);
		return execQuery(statement, ArrayList.class, LinkBean.class);
	}
	
	// Get Link-Roles
	public ArrayList<LinkRoleBean> getLinkRoles(Long linkId) throws SQLException {
		String statement = getSQLFile("getLinkRoles.sql");
		statement = applyParameters(statement, linkId);
		return execQuery(statement, ArrayList.class, LinkRoleBean.class);
	}
	
	// Save Link-Roles
	public void saveLinkRoles(ArrayList<LinkRoleBean> beans, Long linkId) throws SQLException {
		ArrayList<LinkRoleBean> deletes = new ArrayList<LinkRoleBean>();
		ArrayList<LinkRoleBean> inserts = new ArrayList<LinkRoleBean>();
		
		for (int i = 0; i < beans.size(); i++) {
			LinkRoleBean bean = beans.get(i);
			Long key = bean.getLinkId();
			
			if ((key != null) && (bean.getUpdatedDts() == null))
				inserts.add(bean);
			else if ((key == null) && (bean.getUpdatedDts() != null)) { 
				deletes.add(bean);
				bean.setLinkId(linkId);
			}
		}
		
		if (deletes.size() > 0) delete(deletes);
		if (inserts.size() > 0) save(inserts);
	}
	
	public Long getDomainId(String domainName) throws SQLException {
		String statement = "SELECT domain_id FROM domain WHERE domain_name = {1}";
		statement = applyParameters(statement, domainName);
		return execQuery(statement, Long.class);
	}
	
	public String getDomainName(Long domainId) throws SQLException {
		String statement = "SELECT domain_name FROM domain WHERE domain_id = {1}";
		statement = applyParameters(statement, domainId);
		return execQuery(statement, String.class);
	}
	
	public LinkedHashMap<String,String> getDomains() throws SQLException {
		String statement = "SELECT domain_id, domain_name FROM domain";
		return execQuery(statement, LinkedHashMap.class);
	}
	
	public LinkedHashMap<String,String> getRoles() throws SQLException {
		String statement = "SELECT role_name, role_name FROM role ORDER BY role_name";
		return execQuery(statement, LinkedHashMap.class);
	}
	
	public LinkedHashMap<String,String> getThemes() throws SQLException {
		return getThemes(null);
	}
	
	public LinkedHashMap<String,String> getThemes(Long excludeThemeId) throws SQLException {
		String statement = "SELECT theme_id, theme_name FROM theme";
		if (excludeThemeId != null)
			statement += applyParameters(" WHERE theme_id != {1}", excludeThemeId);
		return execQuery(statement, LinkedHashMap.class);
	}
	
	public void deleteDescendantGroups(Long groupId) throws SQLException {
		String statement = "DELETE FROM `group` WHERE lineage LIKE '% {1} %'";
		statement = applyParameters(statement, groupId);
		execUpdate(statement);
	}
	
	public HashMap<String,String> getPasswordHistory(String userid, Long domainId) throws SQLException {
		String statement = "SELECT password, sequence FROM password_history WHERE userid = {1} AND domain_id = {2}";
		statement = applyParameters(statement, userid, domainId);
		return execQuery(statement, LinkedHashMap.class);
	}
	
	public void updatePasswordHistory(String userid, Long domainId, String[] history) throws SQLException {
		String[] statements = new String[history.length + 1];
		statements[0] = "DELETE FROM password_history WHERE userid = {1} AND domain_id = {2}";
		statements[0] = applyParameters(statements[0], userid, domainId);
		for (int i = 1; i <= history.length; i++) {
			if (history[i - 1] == null) continue;
			statements[i] = "INSERT INTO password_history (userid, domain_id, password, sequence) VALUES ({1}, {2}, {3}, {4})";
			statements[i] = applyParameters(statements[i], userid, domainId, history[i - 1], i);
		}
		execUpdate(statements);
	}
	
	public String getValidationCode(String userid) throws SQLException {
		int dPos = userid.indexOf("@");
        String _domain = dPos < 0 ? "default" : userid.substring(dPos + 1);
        String _userid = dPos < 0 ? userid : userid.substring(0, dPos);
		String statement = "SELECT validation_code " +
				             "FROM validation_code " +
				            "WHERE userid = {1} " +
				              "AND domain_id = {2} " +
				              "AND expire_dts > NOW()";
		statement = applyParameters(statement, _userid, getDomainId(_domain));
		return execQuery(statement, String.class);		
	}
	
	public void updateValidationCode(String userid, String validationCode) throws SQLException {
		int dPos = userid.indexOf("@");
        String _domain = dPos < 0 ? "default" : userid.substring(dPos + 1);
        String _userid = dPos < 0 ? userid : userid.substring(0, dPos);
		String[] statements = {
			"DELETE FROM validation_code WHERE expire_dts < NOW()",
			"INSERT INTO validation_code (userid, domain_id, validation_code, expire_dts) " +
			"VALUES ({1}, {2}, {3}, DATE_ADD(NOW(), INTERVAL 5 MINUTE))"
		};
		statements[1] = applyParameters(statements[1], _userid, getDomainId(_domain), validationCode);
		execUpdate(statements);
	}
	
	public boolean canReset(String userid) throws SQLException {
		int dPos = userid.indexOf("@");
        String _domain = dPos < 0 ? "default" : userid.substring(dPos + 1);
		String statement = "SELECT pwd_allow_reset FROM domain WHERE domain_name = {1}";
		statement = applyParameters(statement, _domain);
		String result = execQuery(statement, String.class);
		return Data.nvl(result).equals("Y");
	}
	
	public LinkedHashMap<String,String> getGroups(Long domainId, String userid) throws SQLException {
		String statement = null;
		if (userid == null) {
			statement = "SELECT group_id, CONCAT(REPEAT('- ', LENGTH(TRIM(lineage)) - LENGTH(TRIM(REPLACE(lineage, ' ', '')))), group_name) AS group_name " +
				             "FROM `group` g ";
			if (domainId != null)
				statement += "WHERE domain_id = {1} ";
		}
		else
			statement = "SELECT g.group_id, CONCAT(REPEAT('- ', LENGTH(TRIM(g.lineage)) - LENGTH(TRIM(REPLACE(g.lineage, ' ', '')))), g.group_name) AS group_name " +
		                  "FROM `group` g JOIN user_group u " +
		                    "ON g.group_id = u.group_id " +
		                   "AND g.domain_id = u.domain_id " +
		                   "AND u.userid = {2} " +
		                 "WHERE g.domain_id = {1} ";
		statement += "ORDER BY get_group_sort(g.group_id)";
		statement = applyParameters(statement, domainId, userid);
		return execQuery(statement, LinkedHashMap.class);
	}
	
	public LinkedHashMap<String,String> getNewsGroups(Long domainId, String userid) throws SQLException {
		String statement = getSQLFile("getNewsGroups.sql");
		statement = applyParameters(statement, userid, domainId, userid);
		return execQuery(statement, LinkedHashMap.class);
	}
	
	public String getThemeName(Long themeId) throws SQLException {
		String statement = "SELECT theme_name " +
				             "FROM theme " +
				            "WHERE theme_id = {1}";
		statement = applyParameters(statement, themeId);
		return execQuery(statement, String.class);
	}
	
	public Long getThemeId(String themeName) throws SQLException {
		String statement = "SELECT theme_id " +
				             "FROM theme " +
				            "WHERE LOWER(theme_name) = {1}";
		statement = applyParameters(statement, themeName);
		return execQuery(statement, Long.class);
	}
}
