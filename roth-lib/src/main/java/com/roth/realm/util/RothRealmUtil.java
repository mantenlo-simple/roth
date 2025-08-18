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
package com.roth.realm.util;

import java.security.Principal;
import java.sql.SQLException;
import java.util.ArrayList;

import com.roth.jdbc.annotation.ConnectionDataSource;
import com.roth.jdbc.annotation.SQLFileContext;
import com.roth.jdbc.util.TableUtil;
import com.roth.portal.util.Portal;
import com.roth.realm.RothPrincipal;
import com.roth.realm.model.AuthLogBean;

@ConnectionDataSource(jndiName = "roth")
@SQLFileContext(path = "/com/roth/realm/sql/")
public class RothRealmUtil extends TableUtil {
    private static final long serialVersionUID = 4819559478910883371L;
    
    public RothRealmUtil() throws SQLException { super(); }
    
    public String getPasswordSource(String domain) throws SQLException {
        String statement = getSQLFile("getPasswordSource.sql");
        statement = applyParameters(statement, domain);
        return execQuery(statement, String.class);
    }
    
    private String getDomain(String source) {
    	int dPos = source.indexOf("@");
    	return dPos < 0 ? "default" : source.substring(dPos + 1);
    }
    
    private String getUserid(String source) {
    	int dPos = source.indexOf("@");
    	return dPos < 0 ? source : source.substring(0, dPos);
    }
    
    private String getUsername(String source) {
    	return source.indexOf("@") > -1 ? source : source + "@default";
    }
    
    public String getPassword(String userid) throws SQLException {
        String statement = getSQLFile("getPassword.sql");
        statement = applyParameters(statement, getDomain(userid), getUserid(userid), 0);
        return execQuery(statement, String.class);
    }
    
    public String getExpiredPassword(String userid) throws SQLException {
        String statement = getSQLFile("getExpiredPassword.sql");
        statement = applyParameters(statement, getDomain(userid), getUserid(userid), 0);
        return execQuery(statement, String.class);
    }
    
    protected ArrayList<String> getRoles(String userid) throws SQLException {
    	String statement = getSQLFile("getRoles.sql");
        statement = applyParameters(statement, getDomain(userid), getUserid(userid), 0);
        ArrayList<String> result = execQuery(statement, ArrayList.class, String.class);
        return result;
    }
    
    public Principal getPrincipal(String userid, String password) throws SQLException {
        String pwd = !password.isEmpty() ? password : getPassword(userid);
        String domain = getDomain(userid);
        return (pwd == null) ? null : new RothPrincipal(getUsername(userid), getRoles(userid), getUserid(userid), new Portal().getDomainId(domain), domain);
    }
    
    public Integer getDaysToExpire(String userid) throws SQLException {
    	String statement = getSQLFile("getDaysToExpire.sql");
    	statement = applyParameters(statement, getDomain(userid), getUserid(userid), 0);
        return execQuery(statement, Integer.class);
    }
    
    public void updateAuthLog(String userid, int pwdHash, String remoteHost, boolean validated) throws SQLException {
        Long _domainId = new Portal().getDomainId(getDomain(userid));
        AuthLogBean log = new AuthLogBean(getUserid(userid), _domainId, pwdHash, remoteHost, validated); 
        save(log);
    }
}
