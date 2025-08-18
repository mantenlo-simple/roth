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
package com.roth.portal.util;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import com.roth.jdbc.util.JdbcUtil;
import com.roth.base.log.Log;
import com.roth.jdbc.annotation.ConnectionDataSource;
import com.roth.jdbc.annotation.SQLFileContext;
import com.roth.portal.model.Desktop;
import com.roth.portal.model.Link;
import com.roth.portal.model.MenuItem;
import com.roth.portal.model.Theme;

@ConnectionDataSource(jndiName = "roth")
@SQLFileContext(path = "/com/roth/portal/sql")
public class Portal extends JdbcUtil {
	private static final long serialVersionUID = -6836051921307311260L;
	
	public Portal() throws SQLException { super(); }
	
	public Desktop[] getDesktops(String userid) throws SQLException {
		int dPos = userid.indexOf("@");
        String _domain = dPos < 0 ? "default" : userid.substring(dPos + 1);
        String _userid = dPos < 0 ? userid : userid.substring(0, dPos);
        String statement = getSQLFile("get_desktops.sql");
		statement = applyParameters(statement, _domain, _userid);
		Log.logDebug("\n" + statement + "\n", _userid, "getDesktops");
		return execQuery(statement, Desktop[].class);
	}
	
	public Desktop[] getDesktops(String portletUri, String userid, Integer pdtid) throws SQLException {
		int dPos = userid.indexOf("@");
        String _domain = dPos < 0 ? "default" : userid.substring(dPos + 1);
        String _userid = dPos < 0 ? userid : userid.substring(0, dPos);
        String statement = getSQLFile("get_desktops_by_portlet.sql");
        if (pdtid != null) statement += applyParameters("AND desktop_id = {1}", pdtid);
		statement = applyParameters(statement, portletUri, _domain, _userid);
		Log.logDebug("\n" + statement + "\n", _userid, "getDesktops");
		return execQuery(statement, Desktop[].class);
	}
	
	public Link[] getLinks(BigDecimal themeId) throws SQLException {
		String statement = "SELECT link_title AS title, " +
                                  "link_uri, " +
                                  "target " +
                             "FROM theme_link " +
                            "WHERE theme_id = {1}";
		statement = applyParameters(statement, themeId);
		return execQuery(statement, Link[].class);
	}
	
	public MenuItem getBook(BigDecimal bookId) throws SQLException {
		String statement = "SELECT 'BOOK' AS item_type," +
				                  "book_id, " +
				                  "book_title AS title " +
				             "FROM book " +
				            "WHERE book_id = {1}";
		statement = applyParameters(statement, bookId);
		return execQuery(statement, MenuItem.class);
	}
	
	public MenuItem[] getMenuItems(BigDecimal bookId, String userid) throws SQLException {
		int dPos = userid.indexOf("@");
        String _domain = dPos < 0 ? "default" : userid.substring(dPos + 1);
        String _userid = dPos < 0 ? userid : userid.substring(0, dPos);
        String statement = getSQLFile("get_menu_items.sql");
		statement = applyParameters(statement, bookId, _domain, _userid);
		return execQuery(statement, MenuItem[].class);
	}
	
	public Theme getTheme(BigDecimal themeId, String hostname, boolean mobile) throws SQLException {
		String statement = "SELECT alt_theme_id " +
				             "FROM theme_override " +
				            "WHERE theme_id = {1} " +
				              "AND host_name = {2}";
		statement = applyParameters(statement, themeId, hostname);
		BigDecimal altThemeId = execQuery(statement, BigDecimal.class);
		BigDecimal _themeId = (altThemeId != null) ? altThemeId : themeId;
		if (mobile) {
			statement = "SELECT mobile_theme_id " +
					      "FROM theme " +
					     "WHERE theme_id = {1}";
			statement = applyParameters(statement, _themeId);
			BigDecimal mobileThemeId = execQuery(statement, BigDecimal.class);
			if (mobileThemeId != null)
				_themeId = mobileThemeId;
		}
		
		statement = "SELECT theme_id, " +
		                   "theme_name, " +
		                   "custom_css_uri, " +
		                   "custom_js_uri, " +
		                   "custom_header_html, " +
		                   "custom_footer_html, " +
		                   "copyright_name, " +
		                   "password_uri, " +
		                   "home_uri, " +
		                   "home_uri_view_type, " +
		                   "logout_uri " +
		              "FROM theme " +
		             "WHERE theme_id = {1}";
		statement = applyParameters(statement, _themeId);
		return execQuery(statement, Theme.class);
	}
	
	public String getAggregateRoleProperty(String userid, String propertyName) throws SQLException {
		int dPos = userid.indexOf("@");
        String _domain = dPos < 0 ? "default" : userid.substring(dPos + 1);
        String _userid = dPos < 0 ? userid : userid.substring(0, dPos);
        String statement = getSQLFile("get_aggregate_role_property.sql");
	    statement = applyParameters(statement, _domain, _userid, propertyName);
	    return execQuery(statement, String.class);
	}
	
	public Long getDomainId(String domainName) throws SQLException {
		String statement = "SELECT domain_id FROM domain WHERE domain_name = {1}";
		statement = applyParameters(statement, domainName);
		return execQuery(statement, Long.class);
	}
	
	public String getUserFullName(String userid) throws SQLException {
		int dPos = userid.indexOf("@");
        String _domain = dPos < 0 ? "default" : userid.substring(dPos + 1);
        String _userid = dPos < 0 ? userid : userid.substring(0, dPos);
        String statement = "SELECT name FROM user WHERE userid = {1} AND domain_id = {2}";
		statement = applyParameters(statement, _userid, getDomainId(_domain));
		return execQuery(statement, String.class);
	}
    
    public boolean isAlternateAuth(String userid) throws SQLException {
    	int dPos = userid.indexOf("@");
        String _domain = dPos < 0 ? "default" : userid.substring(dPos + 1);
    	String statement = "SELECT pwd_source FROM domain WHERE domain_name = LOWER({1})";
    	statement = applyParameters(statement, _domain);
    	return execQuery(statement, String.class) != null;
    }
    
    public HashMap<String,String> getSettings(String category) throws SQLException {
    	String statement = "SELECT name, value FROM setting WHERE category = {1} ORDER BY name";
    	statement = applyParameters(statement, category);
    	suppressSettings = true;
    	return execQuery(statement, HashMap.class);
    }
    
    public void saveSettings(String category, HashMap<String,String> values, HashMap<String,Boolean> changed, String userid) throws SQLException {
    	if (changed == null)
    		return;
    	ArrayList<String> statements = new ArrayList<>();
    	Iterator<String> i = values.keySet().iterator();
    	while (i.hasNext()) {
    		String name = i.next();
    		String value = values.get(name);
    		Boolean _changed = changed.get(name);
    		if ((_changed != null) && _changed) {
    			statements.add(applyParameters("DELETE FROM setting WHERE category = {1} AND name = {2}", category, name));
    			if (value != null)
		    		statements.add(applyParameters("INSERT INTO setting " +
		    				                          "(category, name, value, updated_by, updated_dts) " +
		    				                       "VALUES " +
		    				                          "({1}, {2}, {3}, {4}, NOW())", category, name, value, userid));
    		}
    	}
    	execUpdate(statements.toArray(new String[statements.size()]));
    }
}
