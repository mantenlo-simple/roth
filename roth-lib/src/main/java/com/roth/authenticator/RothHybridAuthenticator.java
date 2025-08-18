package com.roth.authenticator;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.Principal;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateFactory;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.catalina.Globals;
import org.apache.catalina.authenticator.AuthenticatorBase;
import org.apache.catalina.authenticator.Constants;
import org.apache.catalina.authenticator.FormAuthenticator;
import org.apache.catalina.authenticator.SSLAuthenticator;
import org.apache.catalina.connector.Request;
import org.apache.coyote.ActionCode;
import org.bouncycastle.asn1.x509.AccessDescription;
import org.bouncycastle.asn1.x509.AuthorityInformationAccess;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.X509ObjectIdentifiers;
import org.bouncycastle.cert.jcajce.JcaX509ExtensionUtils;

import com.roth.base.log.Log;
import com.roth.base.util.Data;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class RothHybridAuthenticator extends AuthenticatorBase {
	private static final String DATE_ONE = Data.dateToStr(new Date(1), "EEE, dd MMM yyyy HH:mm:ss zzz");
	
	FormAuthenticator formAuthenticator = new FormAuthenticator();
	SSLAuthenticator sslAuthenticator = new SSLAuthenticator();
	
	@Override
	protected boolean doAuthenticate(Request request, HttpServletResponse response) throws IOException {
		// Have we already authenticated someone?)
        Principal principal = request.getUserPrincipal();
        //String ssoId = (String) request.getNote(Constants.REQ_SSOID_NOTE);
        if (principal != null) {
            // Associate the session with any existing SSO session in order
            // to get coordinated session invalidation at logout
            String ssoId = (String) request.getNote(Constants.REQ_SSOID_NOTE);
            if (ssoId != null)
                associate(ssoId, request.getSessionInternal(true));
            return true;
        }
        // Get certificates from the request
        X509Certificate certs[] = (X509Certificate[]) request.getAttribute(Globals.CERTIFICATES_ATTR);
        if (Data.isEmpty(certs)) {
        	request.getCoyoteRequest().action(ActionCode.REQ_SSL_CERTIFICATE, null);
            certs = (X509Certificate[]) request.getAttribute(Globals.CERTIFICATES_ATTR);
        }
        // Delegate authentication request
        AuthenticatorBase authenticator = !Data.isEmpty(certs) ? sslAuthenticator : formAuthenticator;
        return authenticator.authenticate(request, response);
	}

	@Override
	protected String getAuthMethod() {
		return HttpServletRequest.CLIENT_CERT_AUTH;
	}
	
	
	
	public static CertificateValidity getValidity(X509Certificate certificate) {
		// Check whether the Root CA is valid (i.e., the Root CA is in the Trust store)
		
	
		// Check whether the certificate has been revoked.
		// Throw an exception if the CRL could not be found (suggest that the certificate is not "real").
		try {
			CertificateRevocationListVerifier.verifyCertificateCRLs(certificate);
		}
		catch (CertificateVerificationException e) {
			return e.getMessage().contains("revoked") 
				 ? CertificateValidity.REVOKED 
			     : CertificateValidity.CRL_NOT_FOUND;
		}
		// Check whether the certificate is currently valid
		// i.e., valid_from_datetime >= current_datetime >= valid_to_datetime
		try {
			certificate.checkValidity(); return CertificateValidity.VALID;
		}
		catch (CertificateExpiredException e) {
			return CertificateValidity.EXPIRED;
		}
		catch (CertificateNotYetValidException e) {
			return CertificateValidity.NOT_YET_VALID;
		}
	}
	
	
	
	
	
	

	public X509Certificate getIssuerCert(X509Certificate cert) {
		// get Authority Information Access extension (will be null if extension is not present)
		byte[] extVal = cert.getExtensionValue(Extension.authorityInfoAccess.getId());
		try {
			AuthorityInformationAccess aia = AuthorityInformationAccess.getInstance(JcaX509ExtensionUtils.parseExtensionValue(extVal));
			// check if there is a URL to issuer's certificate
			AccessDescription[] descriptions = aia.getAccessDescriptions();
			for (AccessDescription ad : descriptions) {
			    // check if it's a URL to issuer's certificate
			    if (ad.getAccessMethod().equals(X509ObjectIdentifiers.id_ad_caIssuers)) {
			        GeneralName location = ad.getAccessLocation();
			        if (location.getTagNo() == GeneralName.uniformResourceIdentifier) {
			            String issuerUrl = location.getName().toString();
			            System.out.println(issuerUrl);
			            // http URL to issuer (test in your browser to see if it's a valid certificate)
			            // you can use java.net.URL.openStream() to create a InputStream and create
			            // the certificate with your CertificateFactory
		            	return (X509Certificate)CertificateFactory.getInstance("X.509").generateCertificate(new URI(issuerUrl).toURL().openStream());
			        }
			    }
			}
		}
		catch (CertificateException | IOException | URISyntaxException e) {
			Log.logException(e, null);
        	return null;
		}
		return null;
	}

	public List<X509Certificate> getCertificationPath(X509Certificate cert) throws CertificateException, IOException {
		List<X509Certificate> result = new ArrayList<>();
		X509Certificate issuer = getIssuerCert(cert);
		while (issuer != null) {
			result.add(issuer);
			issuer = getIssuerCert(issuer);
		}
		return result;
	}



















	

	

	/*
	public String testCert(HttpServletRequest request) {
		X509Certificate[] certs = (X509Certificate[])request.getAttribute("jakarta.servlet.request.X509Certificate");
		if (Data.isEmpty(certs))
			return "No certs found.";
		X509Certificate cert = certs[0];
		return "Validity Value: %s".formatted(validity(cert).toString());
		
		String[] subject = cert.getSubjectX500Principal().getName().split(",");
		for (String s : subject)
			conn.println(s);
		
		return SUCCESS;
	}
	*/

}