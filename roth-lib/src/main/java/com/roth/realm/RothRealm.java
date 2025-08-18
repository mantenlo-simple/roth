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
package com.roth.realm;

import java.security.Principal;
import java.security.PublicKey;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Random;

import javax.security.auth.x500.X500Principal;

import org.apache.catalina.realm.RealmBase;

import com.roth.base.log.Log;
import com.roth.base.util.Data;
import com.roth.realm.util.RothRealmUtil;

public class RothRealm extends RealmBase {
    protected static final String name = "RothRealm";
    protected static final String info = "com.roth.realm.RothRealm/1.3";
    
    private Long seed;
    public Long getSeed() { return seed; }
    public void setSeed(Long seed) { this.seed = seed; }
    
    //@Override
    //protected String getName() { return name; }
    
    @Override
    protected String getPassword(String userid) {
        try { return new RothRealmUtil().getPassword(userid); }
        catch (SQLException e) { return null; }
    }
    
    @Override
    protected Principal getPrincipal(String userid) {
        return getPrincipal(userid, "");
    }
    
    protected Principal getPrincipal(String userid, String password) {
        try { return new RothRealmUtil().getPrincipal(userid, password); }
        catch (SQLException e) { return null; }
    }
    
    protected String getPasswordSource(String domain) {
    	try { return new RothRealmUtil().getPasswordSource(domain); }
        catch (SQLException e) { return null; }
    }
    
    protected Integer getDaysToExpire(String userid) {
    	try { return new RothRealmUtil().getDaysToExpire(userid); }
        catch (SQLException e) { return null; }
    }
    
    @Override
    public Principal authenticate(X509Certificate[] certs) {
    	boolean debugAuth = Data.getWebEnv("debugAuth", false);
    	if (debugAuth)
    		Log.log("DEBUG", "Entering authentication for X509", "com.roth.realm.RothRealm.authenticate(X509Certificate[])", null, false, null);
    	Principal principal = null;
    	
    	for (X509Certificate cert : certs) {
    		if (debugAuth)
        		Log.log("DEBUG", "X509 cert found", "com.roth.realm.RothRealm.authenticate(X509Certificate[])", null, false, null);
    		try { cert.checkValidity(); } 
    		catch (CertificateExpiredException | CertificateNotYetValidException e) { Log.logException(e, null); }
    		PublicKey pubkey = cert.getPublicKey(); // Use this as the password equivalent.
    		X500Principal x500 = cert.getSubjectX500Principal();
    		String certName = x500.getName();
    		
    		if (debugAuth) {
        		Log.log("DEBUG", "certNamne: " + certName, "com.roth.realm.RothRealm.authenticate(X509Certificate[])", null, false, null);
        		Log.log("DEBUG", "pubKey: " + pubkey.toString(), "com.roth.realm.RothRealm.authenticate(X509Certificate[])", null, false, null);
    		}
    	}
    	// Check certs for validity against approved authorities.
    	// Check certs for validity against user table.
    	// If user account exists, is enabled, is not expired, is not locked, then create principal object. 
    	if (debugAuth)
    		Log.log("DEBUG", "Exiting authenticate for X509", "com.roth.realm.RothRealm.authenticate(X509Certificate[])", null, false, null);
    	return principal;
    }
    
    @Override
    public Principal authenticate(String userid, String credentials) {
    	boolean debugAuth = Data.getWebEnv("debugAuth", false);
    	if (debugAuth)
    		Log.log("DEBUG", "Entering authenticate for user/pass", "com.roth.realm.RothRealm.authenticate(String, String)", null, false, null);
        // If either is null, then it's already known to be invalid, so don't bother going further.
        if (Data.isEmpty(userid) || Data.isEmpty(credentials)) return null;
        int dPos = userid.indexOf("@");
        String _domain = dPos < 0 ? "default" : userid.substring(dPos + 1);
        String _userid = dPos < 0 ? userid : userid.substring(0, dPos);
        String pwdSource = getPasswordSource(_domain);
        boolean validated = false;
        Principal principal = null;
        String password = null;
        Integer daysToExpire = null;
        Log.logDebug("pwdSource: " + pwdSource, _userid);
        // If an alternative password source is set...
        if (pwdSource != null) {
        	if (pwdSource.startsWith("ldap:")) {
        		/* LDAP format: url|domain|...
        		       example: ldap://host.domain.com|domain.com|... */
        		String[] url = pwdSource.split("\\|");
        		Log.logDebug("ldap url: " + url[0], _userid);
        		Log.logDebug("ldap domain: " + url[1], _userid);
        		RothLdap ldap = new RothLdap(url[0], url[1], _userid, credentials);
        		validated = ldap.isAuthenticated();
        		if (validated) {
        			password = digest(credentials, "SHA3-512");
        			// The next line seems to fail.  Leave this out until investigated.
        			//daysToExpire = ldap.getDaysToExpire();
        		}
        		else
        			Log.logError(ldap.getAuthError(), _userid, null);
        	}
        }
        // Otherwise use default behavior.
        else {
        	// Get stored password.
	        password = getPassword(userid);
	        // Check the credentials (password) against what is stored in the environment.
	        validated = compareCredentials(password, credentials);
	        daysToExpire = getDaysToExpire(userid);
        }
        // Log trace, if applicable.
        if (containerLog.isTraceEnabled()) {
            String code = (validated) ? "rothRealm.authSuccess" : "rothRealm.authFailure";
            containerLog.trace(sm.getString(code, userid, credentials));
        }
        if (validated) {
        	principal = getPrincipal(userid, password);
        	((RothPrincipal)principal).setDaysToExpire(daysToExpire);
        }
        try {
        	new RothRealmUtil().updateAuthLog(userid, credentials.hashCode(), "unknown", validated); 
        }
        catch (SQLException e) { Log.logException(e, _userid); }
        return principal;
    }

    public static String digest(String source, String algorithm) {
		if (algorithm.equals("SHA3-512"))
			return Data.digest(source, "SHA3-512") + Data.digest("" + source.hashCode(), "SHA3-512");
		else
			return Data.digest(source, algorithm);
	}
    
    protected boolean compareCredentials(String stored, String entered) {
        if (stored == null) return false;
        boolean restrictedAuth = Data.getWebEnv("restrictedAuth", true);
        /*
         * The length of digested strings depend on the method used. 
         * If the *stored* string length is a known length, then compare 
         * the digested string using the appropriate digest algorithm.  
         * Otherwise, the stored password is not assumed to be encrypted.
         * SHA-1 and MD5 have been deprecated. 
         */
        String compare = (stored.length() == 256) ? digest(entered, "SHA3-512")
        			   : (stored.length() == 128) ? digest(entered, "SHA-512")
        		       : !restrictedAuth && (stored.length() ==  64) ? digest(entered, "SHA-256")
                       : !restrictedAuth ? entered
                       : digest("N0t@Pa$sword" + new Random(LocalDateTime.now().getNano()).nextLong(Long.MAX_VALUE), "SHA3-512");
        return stored.equals(compare);
    }
}
