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

import java.io.Serializable;
import java.util.Date;
import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;

import com.roth.base.util.Data;
import com.roth.base.log.Log;

public class RothLdap implements Serializable {
	private static final long serialVersionUID = -4426124873789950527L;

	private final static long WIN32_EPOCH_DIFF = 11644473600000L;
	
	private String domain = null;
	private String username = null;
	private boolean authenticated = false;
	private String authError = null;
	private LdapContext context = null;
	
	public RothLdap(String url, String domain, String username, String password) {
		this.domain = domain;
		this.username = username;
		try {
			Hashtable<String,String> props = new Hashtable<String,String>();
	        props.put(Context.SECURITY_PRINCIPAL, username + "@" + domain);
	        if (password != null) props.put(Context.SECURITY_CREDENTIALS, password);
			props.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
	        props.put(Context.PROVIDER_URL, url);
	        props.put(Context.REFERRAL, "follow");
	        props.put("com.sun.jndi.ldap.connect.timeout", "2000");
	        
	        try { context = new InitialLdapContext(props, null); }
	        catch (javax.naming.CommunicationException e) {
	            authError = "Failed to connect to " + domain + " using URL: " + url;
	        }
	        catch (NamingException e) {
	            authError = "Failed to authenticate " + username + "@" + domain + " using URL: " + url;
	        }
			
			authenticated = context != null;
		}
		catch (Exception e) { 
			Log.logException(e, null);
			authError = "An unexpected error ocurred.";
		}
	}
	
	public boolean isAuthenticated() { return authenticated; }
	public String getAuthError() { return authError; }
	
	public Integer getDaysToExpire() {
		if (context == null) return null;
		
		Long maxPwdAge = null;
		Long pwdAge = null;
		
		try {
			String[] attrList = {"maxPwdAge"};
			Attributes attrs = context.getAttributes(strToAttr(domain, ".", "dc"), attrList);
			// Default to 90 days, if not there. 
			maxPwdAge = (attrs.get("maxPwdAge") == null) ? 90 : Data.strToLong((String)attrs.get("maxPwdAge").get()) * -1 / 10000;
			
			String name = strToAttr(domain, ".", "dc");
			String filter = "(&(objectClass=user)(objectCategory=person)(userPrincipalName=" + username + "@*))";
			String[] userAttrList = {"pwdLastSet", "lastLogonTimestamp"};
			SearchControls searchControls = new SearchControls();
			searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
			searchControls.setReturningAttributes(userAttrList);
			NamingEnumeration<SearchResult> ne = context.search(name, filter, searchControls);
			if (ne != null) {
				while (ne.hasMore()) {
					SearchResult searchresult = ne.next();
					attrs = searchresult.getAttributes();
					
					Long nowPlusW32Epoch = new Date().getTime() + WIN32_EPOCH_DIFF;
					// Default to now, if not there.
					Long pwdLastSet = (attrs.get("pwdLastSet") == null) ? nowPlusW32Epoch : Data.strToLong((String)attrs.get("pwdLastSet").get()) / 10000;
					pwdAge = nowPlusW32Epoch - pwdLastSet;
				}
			}
			
			if (pwdAge == null) return null;
			Log.logDebug("pwdAge: " + pwdAge, username + "@" + domain);
		}
		catch (NamingException e) {
			Log.logException(e, null);
			return null;
		}
		
		Long result = (maxPwdAge - pwdAge) / 1000 / 60 / 60 / 24;
		return result.intValue();
	}
	
	protected static String strToAttr(String source, String delimiter, String attr) {
		String result = "";
		for (String s : source.split(delimiter))
			result += (result.isEmpty() ? "" : ",") + attr + "=" + s;
		return result;
	}
	
	protected static String timeString(Long milli) {
		Long a = milli / 1000;
		String result = (milli - (a * 1000)) + "ms";
		milli /= 1000;
		a /= 60;
		result = (milli - (a * 60)) + "s " + result;
		milli /= 60;
		a /= 60;
		result = (milli - (a * 60)) + "m " + result;
		milli /= 60;
		a /= 24;
		result = (milli - (a * 24)) + "h " + result;
		result = a + "d " + result;
		return result;
	}
}
