package com.roth.authenticator;

public enum CertificateValidity {
	CA_NOT_VALID,
	CRL_NOT_FOUND,
	EXPIRED,
	NOT_YET_VALID,
	REVOKED,
	VALID
}