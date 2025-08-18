package com.roth.authenticator;

public class CertificateVerificationException extends Exception {
	private static final long serialVersionUID = 2836236876288878179L;
	
	public CertificateVerificationException(String message) {
		super(message);
	}
}
