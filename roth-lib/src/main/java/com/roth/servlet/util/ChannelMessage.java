package com.roth.servlet.util;

import java.io.Serializable;

import org.apache.catalina.tribes.ChannelException;

public interface ChannelMessage extends Serializable {
	/**
	 * Sends a ChannelMessage to other nodes in the cluster.
	 * @throws ChannelException
	 */
	public default void sendMessage() throws ChannelException {
		ChannelMessageUtil.sendMessage(this, false);
	}
	
	/**
	 * Receives a ChannelMessage that has been sent from another node in the cluster.
	 */
	public void receiveMessage();
	
	/**
	 * Checks for a message that may have been sent before the current node came online.
	 * @throws ChannelException
	 */
	public default void checkMessage() throws ChannelException {
		ChannelMessageUtil.queryMessage(this);
	}
}
