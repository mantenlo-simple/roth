package com.roth.comm.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.security.KeyStore;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.sql.SQLException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;

//import javax.activation.DataHandler;
import jakarta.activation.MailcapCommandMap;
import jakarta.activation.MimetypesFileTypeMap;
import jakarta.mail.Address;
import jakarta.mail.Authenticator;
import jakarta.mail.Folder;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Store;
import jakarta.mail.Transport;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.internet.MimeUtility;

import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder;
import org.bouncycastle.mail.smime.SMIMEException;
import org.bouncycastle.mail.smime.SMIMESignedGenerator;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;

import com.roth.base.log.Log;
import com.roth.base.util.Data;
import com.roth.jdbc.setting.model.MailSettings;
import com.roth.jdbc.setting.model.SmtpSettings;

public class Email implements Serializable {
	private static final long serialVersionUID = 7597178904815740908L;

	private String smtpHost;
	private int smtpPort;
	private String smtpUser;
	private String smtpPassword;
	private int smtpMode;
	private String smtpFrom;

	private String mailHost;
	private int mailPort;
	private String mailUser;
	private String mailPassword;
	private String mailProtocol;
	private int mailMode;
	
	private String keyStoreInstance;
	private String keyStoreFileName;
	private String keyStorePassword;
	
	private boolean applyDstToOffset;
	private int timeOffset;
	private String timeZone;
	
	public static final int MODE_SIMPLE = 0;
	public static final int MODE_SSL = 1;
	public static final int MODE_TLS = 2;
	
	public static final String MAIL_PROTOCOL_POP = "pop3";
	public static final String MAIL_PROTOCOL_IMAP = "imap";
	
	public static final int BODY_HTML = 0;
	public static final int BODY_PLAIN = 1;
	public static final int BODY_BOTH = 2;
	
	private int bodyMode = BODY_HTML; 
	
	private static final String TEXT_HTML = "text/html";
	private static final String TEXT_CALENDAR = "text/calendar;method=REQUEST";
	private static final String DETECT_CALENDAR = "BEGIN:VCALENDAR";
	
	private static final String SMTP_DATE_FORMAT = "EEE, d MMM yyyy HH:mm:ss Z (z)";
	
	/**
	 * Initialize using localhost and port 25.
	 */
	public Email() {
		this((String)null, null);
	}
	
	/**
	 * Initialize with specified host and port 25.
	 * @param smtpHost
	 */
	public Email(String smtpHost) {
		this(smtpHost, null);
	}
	
	/**
	 * Initialize with specified host and port.
	 * @param smtpHost
	 * @param smtpPort
	 */
	public Email(String smtpHost, Integer smtpPort) {
		try {
			init(new SmtpSettings(), new MailSettings());
			if (smtpHost != null)
				this.smtpHost = smtpHost;
			if (smtpPort != null)
				this.smtpPort = smtpPort;
		}
		catch (SQLException e) { Log.logException(e, null); }
	}
	
	/**
	 * Initialize with a settings package.
	 * @param settings
	 */
	public Email(SmtpSettings smtp, MailSettings mail) {
		init(smtp, mail);
	}
	
	private void init(SmtpSettings smtp, MailSettings mail) {
		this.timeZone = Data.envl(Data.getWebEnv("smtpTimeZone", ""));
		this.timeOffset = Data.getWebEnv("smtpTimeOffset", 0);
		this.applyDstToOffset = Data.getWebEnv("smtpApplyDstToOffset", false);
		
		final MimetypesFileTypeMap mimetypes = (MimetypesFileTypeMap) MimetypesFileTypeMap.getDefaultFileTypeMap();
		mimetypes.addMimeTypes("text/calendar ics ICS");
		final MailcapCommandMap mailcap = (MailcapCommandMap) MailcapCommandMap.getDefaultCommandMap();
		mailcap.addMailcap("text/calendar;; x-java-content-handler=com.sun.mail.handlers.text_plain");
		
		this.smtpHost = Data.nvl(smtp.getHost(), Data.getWebEnv("smtpHost", "localhost"));
		this.smtpPort = Data.nvl(smtp.getPort(), Data.getWebEnv("smtpPort", 25));
		this.smtpUser = Data.envl(Data.nvl(smtp.getUser(), Data.getWebEnv("smtpUser", "")));
		this.smtpPassword = Data.envl(Data.nvl(smtp.getPassword(), Data.getWebEnv("smtpPassword", "")));
		this.smtpMode = Data.nvl(smtp.getMode(), Data.nvl(Data.getWebEnv("smtpMode", MODE_SIMPLE)));
		this.smtpFrom = Data.envl(Data.nvl(smtp.getFrom(), Data.getWebEnv("smtpFrom", "")));
		this.keyStoreInstance = Data.envl(Data.nvl(smtp.getKeyStoreInstance(), Data.getWebEnv("smtpKeyStoreInstance", "")));
		this.keyStoreFileName = Data.envl(Data.nvl(smtp.getKeyStoreFileName(), Data.getWebEnv("smtpKeyStoreFileName", "")));
		this.keyStorePassword = Data.envl(Data.nvl(smtp.getKeyStorePassword(), Data.getWebEnv("smtpKeyStorePassword", "")));
		
		this.mailHost = Data.nvl(mail.getHost(), Data.getWebEnv("mailHost", "localhost"));
		this.mailPort = Data.nvl(mail.getPort(), Data.getWebEnv("mailPort", 25));
		this.mailUser = Data.envl(Data.nvl(mail.getUser(), Data.getWebEnv("mailUser", "")));
		this.mailPassword = Data.envl(Data.nvl(mail.getPassword(), Data.getWebEnv("mailPassword", "")));
		this.mailMode = Data.nvl(mail.getMode(), Data.nvl(Data.getWebEnv("mailMode", MODE_SIMPLE)));
		this.mailProtocol = Data.nvl(mail.getProtocol(), Data.getWebEnv("mailProtocol", "pop3"));
	}
	
	/**
	 * Provide authentication information for the smtp server.
	 * @param smtpUser
	 * @param smtpPassword
	 */
	public void setSmtpAuth(String smtpUser, String smtpPassword) { 
		this.smtpUser = smtpUser;
        this.smtpPassword = smtpPassword; 
    }
	
	/**
	 * Set the smtpMode.  Valid values are SMTP_MODE_SIMPLE, SMTP_MODE_SSL, SMTP_MODE_TLS.
	 * @param smtpMode
	 */
	public void setSmtpMode(int smtpMode) { this.smtpMode = smtpMode; }
	
	/**
	 * Set signing certificate key store.
	 * IMPORTANT: This requires a private key; the first private key encountered will be used,
	 * so use a key store file with only one private key.
	 * @param instance
	 * @param fileName
	 * @param password
	 */
	public void setKeyStore(String instance, String fileName, String password) {
		this.keyStoreInstance = instance;
		this.keyStoreFileName = fileName;
		this.keyStorePassword = password;
	}
	

	/**
	 * Send an HTML email.  Multiple to email addresses may be supplied by using a comma delimiter.
	 * The to parameter may include multiple email addresses.  Each address is assumed to be for the 
	 * TO: field in the email, unless prefixed with 'CC:' or 'BCC:', for which the address will be
	 * added to the CC: or BCC: field respectively.
	 * @param from
	 * @param to (see note on CC: and BCC:)
	 * @param subject
	 * @param body
	 * @return
	 * @throws Exception
	 */
	public boolean sendEmail(String from, String to, String subject, String body) throws MessagingException {
		return sendEmail(Data.nvl(from, smtpFrom), to.split(","), subject, body);
	}
	
	/**
	 * Send an HTML email.
	 * The to parameter may include multiple email addresses.  Each address is assumed to be for the 
	 * TO: field in the email, unless prefixed with 'CC:' or 'BCC:', for which the address will be
	 * added to the CC: or BCC: field respectively.
	 * @param from
	 * @param to (see note on CC: and BCC:)
	 * @param subject
	 * @param body
	 * @return
	 * @throws Exception
	 */
	public boolean sendEmail(String from, String[] to, String subject, String body) throws MessagingException {
		MimeMessage m = getMessage(Data.nvl(from, smtpFrom), to, subject);
		// BODY
		m.setContent(body, TEXT_HTML);
		Transport.send(m);
		return true;
	}
	
	/**
	 * Send an HTML email with attachment(s).  Multiple to email addresses may be supplied by using a comma delimiter.
	 * Multiple attachment filenames may be supplied by using a comma delimiter.
	 * The to parameter may include multiple email addresses.  Each address is assumed to be for the 
	 * TO: field in the email, unless prefixed with 'CC:' or 'BCC:', for which the address will be
	 * added to the CC: or BCC: field respectively.
	 * @param from
	 * @param to (see note on CC: and BCC:)
	 * @param subject
	 * @param body
	 * @param attach File name(s) on the local file system (use full paths).
	 * @return
	 * @throws Exception
	 */
	public boolean sendEmail(String from, String to, String subject, String body, String attach) throws MessagingException, IOException {
		return sendEmail(Data.nvl(from, smtpFrom), to.split(","), subject, body, attach == null ? null : attach.split(","), null);
	}
	
	/**
	 * Send an HTML email with attachments.
	 * The to parameter may include multiple email addresses.  Each address is assumed to be for the 
	 * TO: field in the email, unless prefixed with 'CC:' or 'BCC:', for which the address will be
	 * added to the CC: or BCC: field respectively.
	 * @param from
	 * @param to (see note on CC: and BCC:)
	 * @param subject
	 * @param body
	 * @param attach File names on the local file system (use full paths).
	 * @return
	 * @throws Exception
	 */
	public boolean sendEmail(String from, String[] to, String subject, String body, String[] attach) throws MessagingException, IOException {
		return sendEmail(Data.nvl(from, smtpFrom), to, subject, body, attach, null);
	}
	
	/**
	 * Send an HTML email with attachment(s).  Multiple to email addresses may be supplied by using a comma delimiter.
	 * Multiple attachment file names may be supplied by using a comma delimiter.  Multiple overrides may be supplied
	 * using a comma delimiter.
	 * The to parameter may include multiple email addresses.  Each address is assumed to be for the 
	 * TO: field in the email, unless prefixed with 'CC:' or 'BCC:', for which the address will be
	 * added to the CC: or BCC: field respectively.
	 * @param from
	 * @param to (see note on CC: and BCC:)
	 * @param subject
	 * @param body
	 * @param attach File name(s) on the local file system (use full paths).
	 * @param override File name(s) to override the local file system name(s) as the attachment name(s).
	 * @return
	 * @throws IOException 
	 * @throws MessagingException 
	 * @throws Exception
	 */
	public boolean sendEmail(String from, String to, String subject, String body, String attach, String override) throws MessagingException, IOException {
		return sendEmail(Data.nvl(from, smtpFrom), to.split(","), subject, body, attach == null ? null : attach.split(","), override == null ? null : override.split(","));
	}
	
	/**
	 * Send an HTML email with attachments.
	 * The to parameter may include multiple email addresses.  Each address is assumed to be for the 
	 * TO: field in the email, unless prefixed with 'CC:' or 'BCC:', for which the address will be
	 * added to the CC: or BCC: field respectively.
	 * @param from
	 * @param to (see note on CC: and BCC:)
	 * @param subject
	 * @param body
	 * @param attach File names on the local file system (use full paths).
	 * @param override File names to override the local file system names as the attachment names.
	 * @return
	 * @throws IOException 
	 * @throws Exception
	 */
	public boolean sendEmail(String from, String[] to, String subject, String body, String[] attach, String[] override) throws MessagingException, IOException {
		MimeMessage m = getMessage(Data.nvl(from, smtpFrom), to, subject);
		Multipart mp = new MimeMultipart(); 
		MimeBodyPart p = new MimeBodyPart();
		// BODY
		if (body.contains(DETECT_CALENDAR)) {
        	p.setHeader("Content-Class", "urn:content-classes:calendarmessage");
        	p.setHeader("Content-ID", "calendar_message");
        	//p.setDataHandler(new DataHandler(new ByteArrayDataSource(body, TEXT_CALENDAR)));
        	p.setContent(body, TEXT_CALENDAR);
        }
        else
	        p.setContent(body, TEXT_HTML);
        mp.addBodyPart(p);
        // ATTACHMENTS
		if (attach != null)
			addAttachments(mp, attach, override);
		m.setContent(mp);
		Transport.send(m);
		return true;
	}
	
	// ================= SIGNED EMAIL METHODS  =================
	
	/**
	 * Send a signed email.  This will send a combination of plain text and HTML, with or without attachments.  
	 * The to parameter may include multiple email addresses.  Each address is assumed to be for the 
	 * TO: field in the email, unless prefixed with 'CC:' or 'BCC:', for which the address will be
	 * added to the CC: or BCC: field respectively.
	 * @param from
	 * @param to One or more recipient email addresses, delimited by commas. (see note on CC: and BCC:)
	 * @param subject
	 * @param body
	 * @return
	 * @throws KeyStoreException
	 * @throws NoSuchAlgorithmException
	 * @throws CertificateException
	 * @throws UnrecoverableEntryException
	 * @throws OperatorCreationException
	 * @throws MessagingException
	 * @throws IOException
	 * @throws SMIMEException
	 */
	public boolean sendSignedEmail(String from, String to, String subject, String body) 
			throws KeyStoreException, NoSuchAlgorithmException, CertificateException,  
	       	       UnrecoverableEntryException, OperatorCreationException, MessagingException, IOException, SMIMEException  {
		checkParams(to, subject, body);
		return sendSignedEmail(from, to.split(","), subject, body, null, null);
	}
	
	/**
	 * Send a signed email.  This will send a combination of plain text and HTML, with or without attachments.  
	 * The to parameter may include multiple email addresses.  Each address is assumed to be for the 
	 * TO: field in the email, unless prefixed with 'CC:' or 'BCC:', for which the address will be
	 * added to the CC: or BCC: field respectively.
	 * @param from
	 * @param to One or more recipient email addresses. (see note on CC: and BCC:)
	 * @param subject
	 * @param body
	 * @return
	 * @throws KeyStoreException
	 * @throws NoSuchAlgorithmException
	 * @throws CertificateException
	 * @throws UnrecoverableEntryException
	 * @throws OperatorCreationException
	 * @throws MessagingException
	 * @throws IOException
	 * @throws SMIMEException
	 */
	public boolean sendSignedEmail(String from, String[] to, String subject, String body) 
			throws KeyStoreException, NoSuchAlgorithmException, CertificateException,  
	       	       UnrecoverableEntryException, OperatorCreationException, MessagingException, IOException, SMIMEException  {
		return sendSignedEmail(from, to, subject, body, null, null);
	}
	
    /**
	 * Send a signed email.  This will send a combination of plain text and HTML, with or without attachments.  
	 * The to parameter may include multiple email addresses.  Each address is assumed to be for the 
	 * TO: field in the email, unless prefixed with 'CC:' or 'BCC:', for which the address will be
	 * added to the CC: or BCC: field respectively.
	 * @param from
	 * @param to One or more recipient email addresses, delimited by commas. (see note on CC: and BCC:)
	 * @param subject
	 * @param body
	 * @param attach File names of files to attach, delimited by commas (must be addressable on the local drive). 
	 * @return
	 * @throws KeyStoreException
	 * @throws NoSuchAlgorithmException
	 * @throws CertificateException
	 * @throws UnrecoverableEntryException
	 * @throws OperatorCreationException
	 * @throws MessagingException
	 * @throws IOException
	 * @throws SMIMEException
	 */
	public boolean sendSignedEmail(String from, String to, String subject, String body, String attach) 
			throws KeyStoreException, NoSuchAlgorithmException, CertificateException,  
	       	       UnrecoverableEntryException, OperatorCreationException, MessagingException, IOException, SMIMEException  {
		checkParams(to, subject, body);
		return sendSignedEmail(from, to.split(","), subject, body, attach == null ? null : attach.split(","), null);
	}
	
	/**
	 * Send a signed email.  This will send a combination of plain text and HTML, with or without attachments.  
	 * The to parameter may include multiple email addresses.  Each address is assumed to be for the 
	 * TO: field in the email, unless prefixed with 'CC:' or 'BCC:', for which the address will be
	 * added to the CC: or BCC: field respectively.
	 * @param from
	 * @param to One or more recipient email addresses, delimited by commas. (see note on CC: and BCC:)
	 * @param subject
	 * @param body
	 * @param attach File names of files to attach, delimited by commas (must be addressable on the local drive). 
	 * @param override Alternative file names, delimited by commas, to identify the attachments, if the original filenames aren't desired.  
	 * This must contain the same number of entries as contained in the attach parameter.
	 * @return
	 * @throws KeyStoreException
	 * @throws NoSuchAlgorithmException
	 * @throws CertificateException
	 * @throws UnrecoverableEntryException
	 * @throws OperatorCreationException
	 * @throws MessagingException
	 * @throws IOException
	 * @throws SMIMEException
	 */
	public boolean sendSignedEmail(String from, String to, String subject, String body, String attach, String override) 
			throws KeyStoreException, NoSuchAlgorithmException, CertificateException,  
	       	       UnrecoverableEntryException, OperatorCreationException, MessagingException, IOException, SMIMEException  {
		checkParams(to, subject, body);
		return sendSignedEmail(from, to.split(","), subject, body, attach == null ? null : attach.split(","), override == null ? null : override.split(","));
	}
	
	/**
	 * Send a signed email.  This will send a combination of plain text and HTML, with or without attachments.  
	 * The to parameter may include multiple email addresses.  Each address is assumed to be for the 
	 * TO: field in the email, unless prefixed with 'CC:' or 'BCC:', for which the address will be
	 * added to the CC: or BCC: field respectively.
	 * @param from
	 * @param to One or more recipient email addresses. (see note on CC: and BCC:)
	 * @param subject
	 * @param body
	 * @param attach File names of files to attach (must be addressable on the local drive). 
	 * @return
	 * @throws KeyStoreException
	 * @throws NoSuchAlgorithmException
	 * @throws CertificateException
	 * @throws UnrecoverableEntryException
	 * @throws OperatorCreationException
	 * @throws MessagingException
	 * @throws IOException
	 * @throws SMIMEException
	 */
	public boolean sendSignedEmail(String from, String[] to, String subject, String body, String[] attach) 
			throws KeyStoreException, NoSuchAlgorithmException, CertificateException, 
			       UnrecoverableEntryException, OperatorCreationException, MessagingException, IOException, SMIMEException  {
		return sendSignedEmail(from, to, subject, body, attach, null);
	}
	
	/**
	 * Send a signed email.  This will send a combination of plain text and HTML, with or without attachments.  
	 * The to parameter may include multiple email addresses.  Each address is assumed to be for the 
	 * TO: field in the email, unless prefixed with 'CC:' or 'BCC:', for which the address will be
	 * added to the CC: or BCC: field respectively.
	 * @param from
	 * @param to One or more recipient email addresses. (see note on CC: and BCC:)
	 * @param subject
	 * @param body
	 * @param attach File names of files to attach (must be addressable on the local drive. 
	 * @param override Alternative file names to identify the attachments, if the original filenames aren't desired.  
	 * This must contain the same number of entries as contained in the attach parameter.
	 * @return
	 * @throws KeyStoreException
	 * @throws NoSuchAlgorithmException
	 * @throws CertificateException
	 * @throws UnrecoverableEntryException
	 * @throws OperatorCreationException
	 * @throws MessagingException
	 * @throws IOException
	 * @throws SMIMEException
	 */
	@SuppressWarnings({ "rawtypes" })
	public boolean sendSignedEmail(String from, String[] to, String subject, String body, String[] attach, String[] override) 
			throws KeyStoreException, NoSuchAlgorithmException, CertificateException,  
		       	   UnrecoverableEntryException, OperatorCreationException, MessagingException, IOException, SMIMEException  {
		checkParams(to, subject, body);
		MimeMessage m = getMessage(Data.nvl(from, smtpFrom), to, subject);
		MimeMultipart mp = new MimeMultipart(); 
		PrivateKeyEntry pkEntry = getPrivateKey();
        // Create signer
        X509Certificate cert = (X509Certificate)pkEntry.getCertificate();
        List<X509Certificate> certList = new ArrayList<>();
        certList.add(cert);
        org.bouncycastle.util.Store certs = new JcaCertStore(certList);
        SMIMESignedGenerator gen = new SMIMESignedGenerator();
        ContentSigner sha1Signer = new JcaContentSignerBuilder("SHA1withRSA").build(pkEntry.getPrivateKey());
        gen.addSignerInfoGenerator(new JcaSignerInfoGeneratorBuilder(new JcaDigestCalculatorProviderBuilder().build()).build(sha1Signer, cert));
        gen.addCertificates(certs);
        // Add body
        if (bodyMode == BODY_HTML || bodyMode == BODY_BOTH) {
	        MimeBodyPart mbody = new MimeBodyPart();
	        if (body.contains(DETECT_CALENDAR)) {
	        	mbody.setHeader("Content-Class", "urn:content-classes:calendarmessage");
	        	mbody.setHeader("Content-ID", "calendar_message");
	        	//mbody.setDataHandler(new DataHandler(new ByteArrayDataSource(body, TEXT_CALENDAR)));
	        	mbody.setContent(body, TEXT_CALENDAR);
	        }
	        else
		        mbody.setContent(body, TEXT_HTML);
	        mp.addBodyPart(mbody);
        }
        if (bodyMode == BODY_PLAIN || bodyMode == BODY_BOTH) {
	        m.setText(toPlainText(body));
        }
        // Add attachments, if any
        if (attach != null)
        	addAttachments(mp, attach, override);
        m.setContent(mp);
        // Sign email
		mp = (MimeMultipart)gen.generate(m);
		// Reset contents with signed contents
		m.setContent(mp);
		Transport.send(m);
		return true;
	}
	
	// ================= TEXT (SMS) METHODS  =================
	
	/**
	 * Send a plain text email, or SMS message.  SMS messages are a plain text email where the
	 * to address is in the format "0000000000@carrier" (see Email.CARRIER_* for a list).
	 * Multiple to email addresses may be supplied by using a comma delimiter. 
	 * @param from
	 * @param to
	 * @param subject
	 * @param body
	 * @return
	 * @throws Exception
	 */
	public boolean sendText(String from, String to, String subject, String body) throws MessagingException {
		return sendText(Data.nvl(from, smtpFrom), to.split(","), subject, body);
	}
	
	/**
	 * Send a plain text email, or SMS message.  SMS messages are a plain text email where the
	 * to address is in the format "0000000000@carrier" (see Email.CARRIER_* for a list).
	 * @param from
	 * @param to
	 * @param subject
	 * @param body
	 * @return
	 * @throws Exception
	 */
	public boolean sendText(String from, String[] to, String subject, String body) throws MessagingException {
		MimeMessage m = getMessage(Data.nvl(from, smtpFrom), to, subject);
		m.setText(body);
		Transport.send(m);
		return true;
	}

	// ================= SUPPORT METHODS  =================
	
	/**
	 * Makes sure that the to, subject, and body parameters are not null.
	 * @param to
	 * @param subject
	 * @param body
	 * @throws IllegalArgumentException if any of the parameters are null.
	 */
	protected void checkParams(Object to, String subject, String body) {
		String message = "";
		if (to == null)
			message += "The 'to' parameter cannot be null (there must be at least one recipient).";
		if (subject == null)
			message += (message.isEmpty() ? "" : "\n") + "The 'subject' parameter cannot be null (spam filters don't like that much).";
		if (body == null)
			message += (message.isEmpty() ? "" : "\n") + "The 'body' parameter cannot be null (spam filters don't like that much).";
		if (!message.isEmpty())
			throw new IllegalArgumentException(message);
	}
	
	/**
	 * Gets a private key entry from a key store.
	 * @param filename The filename of the key store.
	 * @param password The password for the key store.
	 * @return
	 * @throws KeyStoreException 
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 * @throws CertificateException 
	 * @throws NoSuchAlgorithmException 
	 * @throws UnrecoverableEntryException 
	 * @throws Exception
	 */
	protected PrivateKeyEntry getPrivateKey() throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException, UnrecoverableEntryException  {
		// Get the keystore
        KeyStore keyStore = KeyStore.getInstance(keyStoreInstance);
        keyStore.load(new FileInputStream(keyStoreFileName), keyStorePassword.toCharArray());
        // Find the first private key entry, and return it
        Enumeration<String> es = keyStore.aliases();
        while (es.hasMoreElements()) {
        	String element = es.nextElement();
        	if (keyStore.isKeyEntry(element))
        		return (PrivateKeyEntry)keyStore.getEntry(element, new KeyStore.PasswordProtection(keyStorePassword.toCharArray()));
        }
        // If none found, return null
        return null;
	}
	
	/**
	 * Prepares the initial message for sending.  This part is the same regardless of whether 
	 * sending an email or signed email, with or without attachments, or sending a text.
	 * @param from
	 * @param to
	 * @param subject
	 * @return
	 * @throws MessagingException 
	 * @throws AddressException 
	 * @throws Exception
	 */
	protected MimeMessage getMessage(String from, String[] to, String subject) throws MessagingException {
		Properties p = new Properties();
		p.put("mail.smtp.host", smtpHost);
		p.put("mail.smtp.port", smtpPort);
		if (smtpUser != null)
			p.put("mail.smtp.auth", true);
		if (smtpMode == MODE_SSL) {
			p.put("mail.smtp.socketFactory.port", smtpPort);
	        p.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
	        p.put("mail.smtp.ssl.checkserveridentity", true);
		}
		else if (smtpMode == MODE_TLS) {
	        p.put("mail.smtp.starttls.enable", "true");
	        p.put("mail.smtp.ssl.trust", smtpHost);
		}
		Session s = smtpUser == null ? Session.getDefaultInstance(p, null)
				  : Session.getDefaultInstance(p, new Authenticator() {
					    @Override
					 	protected PasswordAuthentication getPasswordAuthentication() {
			                return new PasswordAuthentication(smtpUser, smtpPassword);
					 	}
				  	});
		MimeMessage m = new MimeMessage(s);
		// FROM
		m.setFrom(new InternetAddress(from));
		// TO / CC / BCC
		List<Address> recipTo = new ArrayList<>();
		List<Address> recipCc = new ArrayList<>();
		List<Address> recipBcc = new ArrayList<>();
		for (String t : to) {
			String recip = t.trim();
			if (t.startsWith("BCC:"))
				recipBcc.add(new InternetAddress(recip.substring(4)));
			else if (t.startsWith("CC:"))
				recipCc.add(new InternetAddress(recip.substring(3)));
			else
				recipTo.add(new InternetAddress(recip));
		}
		if (!recipTo.isEmpty())
			m.setRecipients(Message.RecipientType.TO, recipTo.toArray(new Address[recipTo.size()]));
		if (!recipCc.isEmpty())
			m.setRecipients(Message.RecipientType.CC, recipCc.toArray(new Address[recipCc.size()]));
		if (!recipBcc.isEmpty())
			m.setRecipients(Message.RecipientType.BCC, recipBcc.toArray(new Address[recipBcc.size()]));
		// SUBJECT
		m.setSubject(subject);
		// DATETIME
		ZonedDateTime sentDate = timeZone != null ? ZonedDateTime.now(ZoneId.of(timeZone)) : ZonedDateTime.now();
		int dstOffset = 0;
		if (applyDstToOffset && timeZone != null && !"UTC".equals(timeZone) && !"GMT".equals(timeZone) && 
			TimeZone.getTimeZone(ZoneId.of(timeZone)).inDaylightTime(new java.util.Date(sentDate.toInstant().toEpochMilli())))
			dstOffset = 60;
		sentDate = Data.dateAdd(sentDate, "minute", timeOffset + dstOffset);
		m.removeHeader("Date");
		m.setHeader("Date", Data.dateToStr(sentDate, SMTP_DATE_FORMAT));
		return m;
	}
	
	/**
	 * Adds attachments to an email.
	 * @param multipart
	 * @param attach
	 * @param override
	 * @throws MessagingException 
	 * @throws IOException 
	 * @throws Exception
	 */
	protected void addAttachments(Multipart multipart, String[] attach, String[] override) throws IOException, MessagingException {
		if ((override != null) && (override.length != attach.length))
			throw new IllegalArgumentException("If override[] is supplied, the length must match that of attach[].");
		for (int i = 0; i < attach.length; i++) {
			MimeBodyPart p = new MimeBodyPart();
			p.attachFile(attach[i]);
			if (override != null)
				p.setFileName(MimeUtility.encodeText(override[i]));
			multipart.addBodyPart(p);
		}
	}
	
	/**
	 * Converts the source to plain text.  This translates some characters and line breaks, and strips out HTML tags.
	 * @param source
	 * @return
	 */
	protected String toPlainText(String source) {
		if (source.contains(DETECT_CALENDAR)) {
			String[] parts = Data.splitLF(source);
			for (String p : parts)
				if (p.startsWith("DESCRIPTION:"))
					return p.substring(12).trim();
			return "";
		}
		else
			return source.replace("�", "\"")
			             .replace("�", "\"")
			             .replace("�", "'")
			             .replace("�", "'")
			             .replace("<br/>", "\n")
			             .replace("<br>", "\n")
			             .replace("</(p|div)>", "\n")
			             .replace("<[/|a-zA-Z][^>]*>", "");
	}
	
	/**
	 * Each message in the result should be closed after use.
	 * @param folderName
	 * @return
	 * @throws IOException
	 * @throws MessagingException
	 */
	public com.roth.comm.util.Message[] receiveEmail(String folderName) throws IOException, MessagingException {
		//create properties field
		Properties properties = new Properties();
		String prefix = "mail." + mailProtocol + ".";
		properties.put(prefix + "host", mailHost);
		properties.put(prefix + "port", mailPort);
		properties.put(prefix + "starttls.enable", Boolean.toString(mailMode == MODE_TLS));
	
		Session emailSession = Session.getInstance(properties);
		//create the POP3 store object and connect with the pop server
		Store store = emailSession.getStore(mailProtocol + (mailMode == MODE_SIMPLE ? "" : "s"));
		store.connect(mailHost, mailUser, mailPassword);
		
		// create the folder object and open it
		Folder folder = store.getFolder(folderName);
		folder.open(Folder.READ_ONLY);
		
		// retrieve the messages from the folder in an array and print it
		com.roth.comm.util.Message[] messages = convertMessages(folder.getMessages());
	
		// close the store and folder objects
		folder.close(false);
		store.close();
		return messages;
	}
	
	protected com.roth.comm.util.Message[] convertMessages(Message[] messages) throws IOException, MessagingException {
		if (messages == null)
			return null;
		com.roth.comm.util.Message[] result = new com.roth.comm.util.Message[messages.length];
		for (int i = 0; i < messages.length; i++) 
			result[i] = new com.roth.comm.util.Message(messages[i]);
		return result;
	}
}
