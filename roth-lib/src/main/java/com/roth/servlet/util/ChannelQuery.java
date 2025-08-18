package com.roth.servlet.util;

public final class ChannelQuery implements ChannelMessage {
	private static final long serialVersionUID = 7931160005333333843L;

	private ChannelMessage queryMessage;
	
	public ChannelQuery(ChannelMessage queryMessage) { this.queryMessage = queryMessage; }
	
	public ChannelMessage getCheckMessage() { return queryMessage; }
	
	@Override
	public void receiveMessage() {}
}
