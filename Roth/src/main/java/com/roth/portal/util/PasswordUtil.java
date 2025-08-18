package com.roth.portal.util;

import java.io.Serializable;
import java.sql.SQLException;
import java.time.LocalDateTime;

import com.roth.base.log.Log;
import com.roth.base.util.Data;
import com.roth.portal.db.PortalUtil;
import com.roth.portal.model.DomainBean;
import com.roth.portal.model.UserBean;
import com.roth.realm.RothRealm;
import com.roth.tags.html.Image;

public class PasswordUtil implements Serializable {
	private static final long serialVersionUID = 7452519289622792855L;

	protected static boolean hasMixed(String source) {
		return (source == null) ? false : source.matches(".*[a-z].*") && source.matches(".*[A-Z].*");
	}
	
	protected static boolean hasNumber(String source) {
		return (source == null) ? false : source.matches(".*[0-9].*");
	}
	
	protected static boolean hasSpecial(String source) {
		return (source == null) ? false : source.replaceAll("[0-9a-zA-Z]", "").length() > 0;
	}
	
	public static String changePassword(String userid, String oldPassword, String newPassword) {
		// Get user record for comparison.
		UserBean user = null;
		DomainBean domain = null;
		int dPos = userid.indexOf("@");
		String _userid = dPos < 0 ? userid : userid.substring(0, dPos);
		String _domainName = dPos < 0 ? "default" : userid.substring(dPos + 1);
		String where = "userid = {1} AND domain_id = {2} ";
		Long domainId;
		PortalUtil util = null;
		try { 
			util = new PortalUtil();
			domainId = util.getDomainId(_domainName);
			where = util.applyParameters(where, _userid, domainId);
			user = util.get(UserBean.class, where);
			domain = util.get(DomainBean.class, util.applyParameters("domain_id = {1}", domainId));
		}
		catch (SQLException e) {
			Log.logError("[PasswordUtil.changePassword] ERROR: Failed to get user record.", userid, null);
			Log.logException(e, userid);
			return "failure";
		}
		
		// If the old password doesn't match, then return the failure status.
		if (!user.getPassword().equals(RothRealm.digest(oldPassword, "SHA")) &&
		    !user.getPassword().equals(RothRealm.digest(oldPassword, "SHA-256")) &&
		    !user.getPassword().equals(RothRealm.digest(oldPassword, "SHA-512")) &&
		    !user.getPassword().equals(RothRealm.digest(oldPassword, "SHA3-512"))) 
		    return "unathorized";
		
		// If the new password is in the history, then reject it.
		if ((user.getPasswordHistory().get(RothRealm.digest(newPassword, "SHA")) != null) ||
			(user.getPasswordHistory().get(RothRealm.digest(newPassword, "SHA-256")) != null) ||
			(user.getPasswordHistory().get(RothRealm.digest(newPassword, "SHA-512")) != null) ||
			(user.getPasswordHistory().get(RothRealm.digest(newPassword, "SHA3-512")) != null)) {
			return "invalid:The \"New Password\" has been used before.";
		}
		
		// If the new password is invalid, then return failure status.
		boolean valid = true;
		if ((domain.getPwdMinLength() != null) && (newPassword.length() < domain.getPwdMinLength()))
			valid = false;
		if (domain.getPwdRequireMixed().equals("Y") && !hasMixed(newPassword))
			valid = false;
		if (domain.getPwdRequireNumber().equals("Y") && !hasNumber(newPassword))
			valid = false;
		if (domain.getPwdRequireSpecial().equals("Y") && !hasSpecial(newPassword))
			valid = false;
		String good = Image.getImage("check");
		String bad = Image.getImage("close");
		if (!valid) {
			String message = "The \"New Password\" supplied is invalid.<br/>";
			if (domain.getPwdMinLength() != null)
				message += ((newPassword.length() < domain.getPwdMinLength()) ? bad : good) + 
				           " &nbsp; It must contain at least " + domain.getPwdMinLength() + " characters.<br/>";
			if (domain.getPwdRequireMixed().equals("Y"))
				message += (hasMixed(newPassword) ? good : bad) + 
				           " &nbsp; It must contain both upper and lower case characters.<br/>";
			if (domain.getPwdRequireNumber().equals("Y"))
				message += (hasNumber(newPassword) ? good : bad) + 
				           " &nbsp; It must contain at least one number.<br/>";
			if (domain.getPwdRequireSpecial().equals("Y"))
				message += (hasSpecial(newPassword) ? good : bad) + 
				           " &nbsp; It must contain at least one special character.<br/>";
			return "invalid:" + message;
		}
		
		// Otherwise, set the new password and save the user record.
		user.setPassword(RothRealm.digest(newPassword, "SHA3-512"));
		if (domain.getPwdShelfLife() != null)
			user.setExpireDts((LocalDateTime)com.roth.tags.el.Util.dateAdd(LocalDateTime.now(), "day", domain.getPwdShelfLife()));
		try {
			util.save(user);
			if ((domain.getPwdRememberCount() != null) && (domain.getPwdRememberCount() > 0)) {
				String[] newhistory = new String[domain.getPwdRememberCount()];
				newhistory[0] = user.getPassword();
				for (String key : user.getPasswordHistory().keySet()) {
					Integer i = Data.strToInteger(user.getPasswordHistory().get(key));
					if (i < newhistory.length)
						newhistory[i] = key;
				}
				util.updatePasswordHistory(_userid, domainId, newhistory);
			}
		}
		catch (SQLException e) {
			Log.logError("[PasswordUtil.changePassword] ERROR: Failed to save user record.", userid, null);
			Log.logException(e, userid);
			return "failure";
		}
		// Return success status.
		return "success";
	}
	
	public static String changeForgottenPassword(String userid, String validationCode, String newPassword) {
		// Get user record for comparison.
		UserBean user = null;
		DomainBean domain = null;
		int dPos = userid.indexOf("@");
		String _userid = dPos < 0 ? userid : userid.substring(0, dPos);
		String _domainName = dPos < 0 ? "default" : userid.substring(dPos + 1);
		String where = "userid = {1} AND domain_id = {2} ";
		Long domainId;
		PortalUtil util = null;
		try { 
			util = new PortalUtil();
			domainId = util.getDomainId(_domainName);
			where = util.applyParameters(where, _userid, domainId);
			user = util.get(UserBean.class, where);
			domain = util.get(DomainBean.class, util.applyParameters("domain_id = {1}", domainId));
		}
		catch (SQLException e) {
			Log.logError("[PasswordUtil.changeForgottenPassword] ERROR: Failed to get user record.", userid, null);
			Log.logException(e, userid);
			return "failure";
		}
		String compare = null;
		try {
			compare = util.getValidationCode(userid);
			// If the old password doesn't match, then return the failure status.
			if (!compare.equals(RothRealm.digest(validationCode, "SHA3-512"))) 
			    return "unathorized";
		}
		catch (SQLException e) {
			Log.logError("[PasswordUtil.changeForgottenPassword] ERROR: Failed to validate code.", userid, null);
			Log.logException(e, userid);
			return "failure";
		}
		
		// If the new password is in the history, then reject it.
		if ((user.getPasswordHistory().get(RothRealm.digest(newPassword, "SHA")) != null) ||
			(user.getPasswordHistory().get(RothRealm.digest(newPassword, "SHA-256")) != null) ||
			(user.getPasswordHistory().get(RothRealm.digest(newPassword, "SHA-512")) != null) ||
			(user.getPasswordHistory().get(RothRealm.digest(newPassword, "SHA3-512")) != null)) {
			return "invalid:The \"New Password\" has been used before.";
		}
		
		// If the new password is invalid, then return failure status.
		boolean valid = true;
		if ((domain.getPwdMinLength() != null) && (newPassword.length() < domain.getPwdMinLength()))
			valid = false;
		if (domain.getPwdRequireMixed().equals("Y") && !hasMixed(newPassword))
			valid = false;
		if (domain.getPwdRequireNumber().equals("Y") && !hasNumber(newPassword))
			valid = false;
		if (domain.getPwdRequireSpecial().equals("Y") && !hasSpecial(newPassword))
			valid = false;
		String good = Image.getImage("check");
		String bad = Image.getImage("close");
		if (!valid) {
			String message = "The \"New Password\" supplied is invalid.<br/>";
			if (domain.getPwdMinLength() != null)
				message += ((newPassword.length() < domain.getPwdMinLength()) ? bad : good) + 
				           " &nbsp; It must contain at least " + domain.getPwdMinLength() + " characters.<br/>";
			if (domain.getPwdRequireMixed().equals("Y"))
				message += (hasMixed(newPassword) ? good : bad) + 
				           " &nbsp; It must contain both upper and lower case characters.<br/>";
			if (domain.getPwdRequireNumber().equals("Y"))
				message += (hasNumber(newPassword) ? good : bad) + 
				           " &nbsp; It must contain at least one number.<br/>";
			if (domain.getPwdRequireSpecial().equals("Y"))
				message += (hasSpecial(newPassword) ? good : bad) + 
				           " &nbsp; It must contain at least one special character.<br/>";
			return "invalid:" + message;
		}
		
		// Otherwise, set the new password and save the user record.
		user.setPassword(RothRealm.digest(newPassword, "SHA3-512"));
		if (domain.getPwdShelfLife() != null)
			user.setExpireDts((LocalDateTime)com.roth.tags.el.Util.dateAdd(LocalDateTime.now(), "day", domain.getPwdShelfLife()));
		try {
			util.save(user);
			if ((domain.getPwdRememberCount() != null) && (domain.getPwdRememberCount() > 0)) {
				String[] newhistory = new String[domain.getPwdRememberCount()];
				newhistory[0] = user.getPassword();
				for (String key : user.getPasswordHistory().keySet()) {
					Integer i = Data.strToInteger(user.getPasswordHistory().get(key));
					if (i < newhistory.length)
						newhistory[i] = key;
				}
				util.updatePasswordHistory(_userid, domainId, newhistory);
			}
		}
		catch (SQLException e) {
			Log.logError("[PasswordUtil.changeForgottenPassword] ERROR: Failed to save user record.", userid, null);
			Log.logException(e, userid);
			return "failure";
		}
		// Return success status.
		return "success";
	}
}
