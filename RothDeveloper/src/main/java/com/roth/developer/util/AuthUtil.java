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
package com.roth.developer.util;

import java.sql.SQLException;

import com.roth.base.log.Log;
//import com.roth.portal.util.PortalUtil;
import com.roth.base.util.Data;
import com.roth.jdbc.annotation.ConnectionDataSource;
import com.roth.jdbc.util.JdbcUtil;

@ConnectionDataSource(jndiName = "roth")
public class AuthUtil extends JdbcUtil {
	private static final long serialVersionUID = -2309478800957108557L;

	public AuthUtil() throws SQLException { super(); }
	
	protected String getPassword(String userid) throws SQLException {
		int dPos = userid.indexOf("@");
        String _domain = dPos < 0 ? "default" : userid.substring(dPos + 1);
        String _userid = dPos < 0 ? userid : userid.substring(0, dPos);
        String statement = "SELECT password " + 
							 "FROM user " +
							"WHERE userid = {1} " +
							  "AND domain_id = (SELECT domain_id " +
							                     "FROM domain " + 
							                    "WHERE domain_name = {2})";
        statement = applyParameters(statement, _userid, _domain);
        return execQuery(statement, String.class);
    }
	
	/*
	protected String digest(String source, String type) {
        if (source == null) return null;
        try { 
        	MessageDigest md = MessageDigest.getInstance(type);
        	md.reset();
        	return HexUtils.toHexString(md.digest(source.getBytes()));
        }
        catch (Exception e) {
        	Log.logException(e, null);
        	return null; 
        }
    }
    */
	
	public static String digest(String source, String algorithm) {
		if (algorithm.equals("SHA3-512"))
			return Data.digest(source, "SHA3-512") + Data.digest("" + source.hashCode(), "SHA3-512");
		else
			return Data.digest(source, algorithm);
	}
	
	protected boolean compareCredentials(String stored, String entered) {
        if (stored == null) return false;
        String compare = (stored.length() == 256) ? digest(entered, "SHA3-512")
		  		       : (stored.length() == 128) ? digest(entered, "SHA-512")
				  	   : (stored.length() ==  64) ? digest(entered, "SHA-256")
		  		       : (stored.length() ==  40) ? digest(entered, "SHA") 
		               : (stored.length() ==  32) ? digest(entered, "MD5")
		               : entered;
        return stored.equals(compare);
    }
	
	public boolean isAuthorized(String userid, String password) {
		if ((userid == null) || (password == null)) return false;
		try { return compareCredentials(getPassword(userid), password); }
		catch (SQLException e) {
			Log.logException(e, userid);
			return false;
		}
	}
}
