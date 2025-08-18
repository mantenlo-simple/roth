package com.roth.servlet.util;

import java.time.LocalDateTime;

public final class ChannelResponse implements ChannelMessage {
	private static final long serialVersionUID = 7931160005333333843L;

	private ChannelMessage message;
	private LocalDateTime timeSent;
	
	public ChannelResponse(ChannelMessage message, LocalDateTime timeSent) { this.message = message; this.timeSent = timeSent; }
	
	public ChannelMessage getCheckMessage() { return message; }
	public LocalDateTime getTimeSent() { return timeSent; }
	
	@Override
	public void receiveMessage() {}
}
