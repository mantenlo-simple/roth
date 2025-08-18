package com.roth.realm.model;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;

import org.apache.commons.codec.binary.Hex;

import com.roth.authenticator.CertificateRevocationListVerifier;
import com.roth.authenticator.CertificateValidity;
import com.roth.authenticator.CertificateVerificationException;

/**
 * A convenience wrapper for an X509 certificate.
 */
public class RothX509Certificate {
	private X509Certificate certificate;
	
	public RothX509Certificate(X509Certificate certificate) {
		this.certificate = certificate;
	}
	
	/**
	 * Get the subject from the certificate.
	 * @return
	 */
	public String getSubject() {
		return certificate.getSubjectX500Principal().getName();
	}
	
	/**
	 * Get the thumbprint from the certificate.
	 * @return
	 * @throws CertificateEncodingException
	 * @throws NoSuchAlgorithmException
	 */
	public String getThumbprint() throws CertificateEncodingException, NoSuchAlgorithmException {
		return new String(Hex.encodeHex(MessageDigest.getInstance("SHA-1").digest(certificate.getEncoded()))).toLowerCase();
	}
	
	public CertificateValidity getValidity() {
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
}