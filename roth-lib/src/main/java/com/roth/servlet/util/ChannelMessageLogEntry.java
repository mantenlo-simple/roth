package com.roth.servlet.util;

import java.time.LocalDateTime;

public class ChannelMessageLogEntry {
	private ChannelMessage message;
	private LocalDateTime sent;
	
	public ChannelMessageLogEntry(ChannelMessage message, LocalDateTime sent) {
		this.message = message;
		this.sent = sent;
	}
	
	public ChannelMessage getMessage() { return message; }
	public LocalDateTime getSent() { return sent; }
}
