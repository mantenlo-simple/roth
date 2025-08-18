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

import java.io.Serializable;
import java.sql.SQLException;

import com.roth.base.log.Log;
import com.roth.base.util.Data;
import com.roth.comm.util.Email;
import com.roth.portal.db.PortalUtil;
import com.roth.portal.model.UserProperty;
import com.roth.realm.RothRealm;

public class AuthenticationUtil implements Serializable {
	private static final long serialVersionUID = -1741027321955112217L;

	public static String getUserid(String userid) {
		if (userid == null) 
			return null;
		int dPos = userid.indexOf("@");
        return dPos < 0 ? userid : userid.substring(0, dPos);
	}
	
	public static String getDomainName(String userid) {
		if (userid == null) 
			return null;
		int dPos = userid.indexOf("@");
	    return dPos < 0 ? "default" : userid.substring(dPos + 1);
	}
	
	public static Long getDomainId(String userid) {
		try { 
			return new Portal().getDomainId(getDomainName(userid)); 
		}
		catch (SQLException e) {
			Log.logException(e, null);
			return null;
		}
	}
	
	public static final int PIN_VALID = 0;
	public static final int PIN_INVALID = 1;
	public static final int PIN_NOT_MATCHED = 2;
	
	public static int validatePin(String userid, String pin) {
		UserProperty p = null;
		try {
			p = UserProperty.getUserProperty(userid, "_reset_pin");
			if (p.getPropertyValue() == null)
				return PIN_INVALID;
		}
		catch (Exception e) {
			return PIN_INVALID;
		}
		try {
			String compare = Data.decrypt(p.getPropertyValue(), Long.valueOf(pin));
			return compare.equals(pin) ? PIN_VALID : PIN_NOT_MATCHED;
		}
		catch (Exception e) {
			return PIN_NOT_MATCHED;
		}
	}
	
	public static void sendValidationCode(String userid, String pin) {
		try {
			UserProperty e = UserProperty.getUserProperty(userid, "_reset_email");
			String email = Data.decrypt(e.getPropertyValue(), Long.valueOf(pin));
			String validationCode = Data.encrypt(pin, Long.valueOf(pin)).substring(3, 11);
			new PortalUtil().updateValidationCode(userid,  RothRealm.digest(validationCode, "SHA3-512"));
			// Send email
			Email smtp = new Email();
			smtp.sendText(null, email, "Forgotten Password", "Validation Code: " + validationCode);
		}
		catch (Exception e) {
			Log.logException(e, null);
		}
	}
}
