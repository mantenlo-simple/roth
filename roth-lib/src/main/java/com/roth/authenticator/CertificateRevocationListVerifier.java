/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.roth.authenticator;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.cert.CRLException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.InitialDirContext;

import org.bouncycastle.asn1.ASN1IA5String;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.x509.CRLDistPoint;
import org.bouncycastle.asn1.x509.DistributionPoint;
import org.bouncycastle.asn1.x509.DistributionPointName;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;

import com.roth.base.util.Data;

public final class CertificateRevocationListVerifier {
    private CertificateRevocationListVerifier() { }
    
    /**
     * Extracts the CRL distribution points from the certificate (if available)
     * and checks the certificate revocation status against the CRLs coming from
     * the distribution points. Supports HTTP, HTTPS, FTP and LDAP based URLs.
     * 
     * @param cert - the certificate to be checked for revocation
     * @throws CertificateVerificationException if the certificate is revoked
     */
    public static void verifyCertificateCRLs(X509Certificate cert) throws CertificateVerificationException {
        try {
            List<String> crlDistPoints = getCrlDistributionPoints(cert);
            for (String crlDP : crlDistPoints) {
                X509CRL crl = downloadCRL(crlDP);
                if (crl.isRevoked(cert))
                    throw new CertificateVerificationException("The certificate is revoked by CRL: %s".formatted(crlDP));
            }
        } catch (Exception ex) {
            if (ex instanceof CertificateVerificationException) {
                throw (CertificateVerificationException) ex;
            } else {
                throw new CertificateVerificationException("Can not verify CRL for certificate: %s".formatted(cert.getSubjectX500Principal()));
            }
        }
    }

    /**
     * Downloads CRL from given URL. Supports http, https, ftp and ldap based URLs.
     * @param crlURL
     * @throws IOException 
     * @throws CRLException 
     * @throws CertificateException 
     * @throws MalformedURLException 
     * @throws CertificateVerificationException 
     * @throws NamingException 
     * @throws URISyntaxException 
     */
    private static X509CRL downloadCRL(String crlURL) throws MalformedURLException, CertificateException, CRLException, IOException, CertificateVerificationException, NamingException, URISyntaxException  {
    	switch (crlURL.substring(0, crlURL.indexOf("://"))) {
	    	case "http", "https", "ftp": return downloadCRLFromWeb(crlURL);
	    	case "ldap": return downloadCRLFromLDAP(crlURL);
	    	default: throw new CertificateVerificationException("Can not download CRL from certificate distribution point: %s".formatted(crlURL));
    	}
    }

    /**
     * Downloads a CRL from given LDAP url, e.g.
     * ldap://ldap.infonotary.com/dc=identity-ca,dc=infonotary,dc=com
     * @throws IOException 
     */
    private static X509CRL downloadCRLFromLDAP(String ldapURL) throws CertificateException, NamingException, CRLException, CertificateVerificationException, IOException {
    	Hashtable<String, String> env = new Hashtable<>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, ldapURL);
		Attribute aval = new InitialDirContext(env).getAttributes("").get("certificateRevocationList;binary");
        byte[] val = (byte[]) aval.get();
        if (Data.isEmpty(val))
            throw new CertificateVerificationException("Can not download CRL from: %s".formatted(ldapURL));
        else
        	try (InputStream inStream = new ByteArrayInputStream(val)) {
        		return (X509CRL) CertificateFactory.getInstance("X.509").generateCRL(inStream);
        	}
    }

    /**
     * Downloads a CRL from given HTTP/HTTPS/FTP URL, e.g.
     * http://crl.infonotary.com/crl/identity-ca.crl
     * @throws URISyntaxException 
     */
    private static X509CRL downloadCRLFromWeb(String crlURL) throws MalformedURLException, IOException, CertificateException, CRLException, URISyntaxException {
        try (InputStream crlStream = new URI(crlURL).toURL().openStream()) {
            return (X509CRL) CertificateFactory.getInstance("X.509").generateCRL(crlStream);
        }
    }

    /**
     * Extracts all CRL distribution point URLs from the
     * "CRL Distribution Point" extension in a X.509 certificate. If CRL
     * distribution point extension is unavailable, returns an empty list.
     */
    public static List<String> 
    getCrlDistributionPoints(X509Certificate cert) throws CertificateParsingException, IOException {
        byte[] crldpExt = cert.getExtensionValue(Extension.cRLDistributionPoints.getId());
        if (crldpExt == null)
            return new ArrayList<String>();
        byte[] crldpExtOctets;
        try (ASN1InputStream oAsnInStream = new ASN1InputStream(new ByteArrayInputStream(crldpExt))) {
	        ASN1Primitive derObjCrlDP = oAsnInStream.readObject();
	        DEROctetString dosCrlDP = (DEROctetString) derObjCrlDP;
	        crldpExtOctets = dosCrlDP.getOctets();
        }
        CRLDistPoint distPoint;
        try (ASN1InputStream oAsnInStream2 = new ASN1InputStream(new ByteArrayInputStream(crldpExtOctets))) {
	        ASN1Primitive derObj2 = oAsnInStream2.readObject();
	        distPoint = CRLDistPoint.getInstance(derObj2);
        }
        List<String> crlUrls = new ArrayList<String>();
        for (DistributionPoint dp : distPoint.getDistributionPoints()) {
            DistributionPointName dpn = dp.getDistributionPoint();
            // Look for URIs in fullName
            if (dpn != null
                && dpn.getType() == DistributionPointName.FULL_NAME) {
                GeneralName[] genNames = GeneralNames.getInstance(dpn.getName()).getNames();
                // Look for an URI
                for (int j = 0; j < genNames.length; j++) {
                    if (genNames[j].getTagNo() == GeneralName.uniformResourceIdentifier) {
                        String url = ASN1IA5String.getInstance(genNames[j].getName()).getString();
                        crlUrls.add(url);
                    }
                }
            }
        }
        return crlUrls;
    }

}