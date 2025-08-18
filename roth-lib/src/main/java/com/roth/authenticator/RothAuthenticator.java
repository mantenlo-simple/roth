package com.roth.authenticator;

import java.io.IOException;
import java.security.Principal;
import java.security.cert.X509Certificate;

import org.apache.catalina.Globals;
import org.apache.catalina.authenticator.AuthenticatorBase;
import org.apache.catalina.connector.Request;
import org.apache.coyote.ActionCode;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class RothAuthenticator extends AuthenticatorBase {

	@Override
	protected boolean doAuthenticate(Request request, HttpServletResponse response) throws IOException {
        // NOTE: We don't try to reauthenticate using any existing SSO session,
        // because that will only work if the original authentication was
        // BASIC or FORM, which are less secure than the CLIENT-CERT auth-type
        // specified for this webapp
        //
        // Change to true below to allow previous FORM or BASIC authentications
        // to authenticate users for this webapp
        // TODO make this a configurable attribute (in SingleSignOn??)
        if (checkForCachedAuthentication(request, response, false)) {
            return true;
        }

        // Retrieve the certificate chain for this client
        if (containerLog.isDebugEnabled()) {
            containerLog.debug(" Looking up certificates");
        }

        X509Certificate certs[] = getRequestCertificates(request);

        if ((certs == null) || (certs.length < 1)) {
            if (containerLog.isDebugEnabled()) {
                containerLog.debug("  No certificates included with this request");
            }
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED,
                    sm.getString("authenticator.certificates"));
            return false;
        }

        // Authenticate the specified certificate chain
        Principal principal = context.getRealm().authenticate(certs);
        if (principal == null) {
            if (containerLog.isDebugEnabled()) {
                containerLog.debug("  Realm.authenticate() returned false");
            }
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED,
                               sm.getString("authenticator.unauthorized"));
            return (false);
        }

        // Cache the principal (if requested) and record this authentication
        register(request, response, principal,
                HttpServletRequest.CLIENT_CERT_AUTH, null, null);
        return (true);
	}

	@Override
	protected String getAuthMethod() {
		return HttpServletRequest.CLIENT_CERT_AUTH;
	}
	
	@Override
	protected boolean isPreemptiveAuthPossible(Request request) {
	    X509Certificate[] certs = getRequestCertificates(request);
	    return certs != null && certs.length > 0;
	}

	protected X509Certificate[] getRequestCertificates(final Request request)
	        throws IllegalStateException {
	
	    X509Certificate certs[] =
	            (X509Certificate[]) request.getAttribute(Globals.CERTIFICATES_ATTR);
	
	    if ((certs == null) || (certs.length < 1)) {
	        try {
	            request.getCoyoteRequest().action(ActionCode.REQ_SSL_CERTIFICATE, null);
	            certs = (X509Certificate[]) request.getAttribute(Globals.CERTIFICATES_ATTR);
	        } catch (IllegalStateException ise) {
	            // Request body was too large for save buffer
	            // Return null which will trigger an auth failure
	        }
	    }
	
	    return certs;
	}
}
