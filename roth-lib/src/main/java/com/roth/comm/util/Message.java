package com.roth.comm.util;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.mail.BodyPart;
import jakarta.mail.Flags;
import jakarta.mail.Flags.Flag;
import jakarta.mail.Header;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;

import com.roth.base.util.Data;
import com.roth.export.util.CsvRecord;

public class Message implements Serializable, Closeable {
	private static final long serialVersionUID = 3782117607153078244L;

	private List<Attachment> attachments;
	private String body;
	private String flags;
	private String from;
	private Map<String,List<String>> headers;
	private LocalDateTime receivedDate;
	private String recipients;
	private String replyTo;
	private LocalDateTime sentDate;
	private String subject;
	private String text;

	/**
	 * Create an empty message.
	 */
	public Message() {
		attachments = new ArrayList<>();
		headers = new HashMap<>();
	}
	
	/**
	 * Create a message from a jakarta.mail.Message.
	 * @param source
	 * @throws MessagingException
	 * @throws IOException
	 */
	public Message(jakarta.mail.Message source) throws MessagingException, IOException {
		this();
		processMessage(source);
	}

	/**
	 * Get the body of the message.  If the email contains and HTML part, that
	 * part is returned, otherwise the plain text will be returned.
	 * @return
	 */
	public String getBody() { return Data.nvl(body, Data.nvl(text)); }
	public void setBody(String body) { this.body = body; }

	/**
	 * Get a CSV list of flags found in the message.
	 * @return
	 */
	public String getFlags() { return flags; }
	public void setFlags(String flags) { this.flags = flags; }

	public String getFrom() { return from; }
	public void setFrom(String from) { this.from = from; }
	
	public Map<String,List<String>> getHeaders() { 
		return headers; 
	}
	
	public LocalDateTime getReceivedDate() { return receivedDate; }
	public void setReceivedDate(LocalDateTime receivedDate) { this.receivedDate = receivedDate; }

	public String getRecipients() { return recipients; }
	public void setRecipients(String recipients) { this.recipients = recipients; }

	public String getReplyTo() { return replyTo; }
	public void setReplyTo(String replyTo) { this.replyTo = replyTo; }

	public LocalDateTime getSentDate() { return sentDate; }
	public void setSentDate(LocalDateTime sentDate) { this.sentDate = sentDate; }

	public String getSubject() { return subject; }
	public void setSubject(String subject) { this.subject = subject; }
	
	/**
	 * Get the text of the message.  If the email contains a plain text part, 
	 * that part is returned, otherwise the plain text form of the HTML part is 
	 * returned.
	 * @return
	 * @see com.roth.base.util.Data#stripHtml(String source)
	 */
	public String getText() { return Data.nvl(text, Data.nvl(Data.stripHtml(body == null ? "" : body))); }
	public void setText(String text) { this.text = text; }

	public int getAttachmentCount() {
		return attachments.size();
	}
	
	public Iterator<Attachment> attachmentIterator() {
		return attachments.iterator();
	}
	
	public void addAttachment(Attachment attachment) {
		attachments.add(attachment);
	}
	
	public Attachment getAttachment(int index) {
		return attachments.get(index);
	}

	/**
	 * Removes any temporary files from the folder defined by the rothTemp context variable.
	 */
	@Override
	public void close() throws IOException {
		for (Attachment attachment : attachments)
			attachment.close();
	}
	
	// ====================================
	// Support Methods
	// ====================================
	
	protected void processMessage(jakarta.mail.Message source) throws MessagingException, IOException {
		mapHeaders(source.getAllHeaders());
		from = Arrays.stream(source.getFrom()).map(jakarta.mail.Address::toString).collect(Collectors.joining(","));
		replyTo = Arrays.stream(source.getReplyTo()).map(jakarta.mail.Address::toString).collect(Collectors.joining(","));
		recipients = Arrays.stream(source.getAllRecipients()).map(jakarta.mail.Address::toString).collect(Collectors.joining(","));
		subject = source.getSubject();
		sentDate = convertDts(source.getSentDate());
		receivedDate = convertDts(source.getReceivedDate());
		processFlags(source.getFlags());
		processContent(source.getContent(), source.getContentType(), null, null);
	}
	
	protected void mapHeaders(Enumeration<Header> headers) {
		while (headers.hasMoreElements()) {
			Header header = headers.nextElement();
			this.headers.computeIfAbsent(header.getName(), s -> new ArrayList<>()).add(header.getValue());
		}
	}
	
	protected LocalDateTime convertDts(Date source) {
		return source == null ? null : source.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
	}
	
	protected void processFlags(Flags flags) {
		CsvRecord rec = new CsvRecord(); 
		if (flags.getSystemFlags() != null)
			for (Flag flag : flags.getSystemFlags())
				rec.putString(flag.toString());
		if (flags.getUserFlags() != null)
			for (String flag : flags.getUserFlags())
				rec.putString(flag);
		this.flags = rec.toString();
	}
	
	protected void processContent(Object content, String contentType, String filename, InputStream input) throws MessagingException, IOException {
		if (contentType.toLowerCase().startsWith("text/html"))
			body = (String)content;
		else if (contentType.toLowerCase().startsWith("text/plain"))
			text = (String)content;
		else if (content instanceof Multipart)
			processMultipart((Multipart)content);
		else if (filename != null && input != null)
			attachments.add(new Attachment(filename, input));
	}
	
	protected void processMultipart(Multipart multi) throws MessagingException, IOException {
		for (int i = 0; i < multi.getCount(); i++) {
			BodyPart bodypart = multi.getBodyPart(i);
			processContent(bodypart.getContent(), bodypart.getContentType(), bodypart.getFileName(), bodypart.getInputStream());
		}
	}
}