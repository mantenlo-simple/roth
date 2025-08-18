package com.roth.servlet.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;

import org.apache.catalina.ha.tcp.SimpleTcpCluster;
import org.apache.catalina.tribes.Channel;
import org.apache.catalina.tribes.ChannelException;
import org.apache.catalina.tribes.Member;
import org.apache.tomcat.util.modeler.BaseModelMBean;

import com.roth.base.log.Log;
import com.sun.jmx.interceptor.DefaultMBeanServerInterceptor;
import com.sun.jmx.mbeanserver.JmxMBeanServer;

public final class ChannelMessageUtil {

	private ChannelMessageUtil() {}
	
	public static final String SYSTEM = "[SYSTEM]";
	public static final String MESSAGE = "MESSAGE: ";
	
	private static Channel channel = null;
	
	/**
	 * Get the cluster communications channel.  This is not meant for direct use.
	 * @return
	 */
	public static Channel getChannel() {
		if (channel != null)
			return channel;
		
		String origin = "com.roth.sertlet.util.ChannelUtil.getChannel";
		
		ArrayList<MBeanServer> mbservers = MBeanServerFactory.findMBeanServer(null);
		MBeanServer mbserver = null;
		SimpleTcpCluster cluster = null;
		
		if (mbservers != null && !mbservers.isEmpty())
			mbserver = mbservers.get(0);
		
		if (mbserver == null)
			Log.log(MESSAGE, "MBeanServer not found.", origin, SYSTEM, false, null);
		else {
			Set<ObjectName> names = mbserver.queryNames(null, null);
			for (ObjectName name : names) {
				if ("Catalina:type=Cluster".equals(name.getCanonicalName())) {
					JmxMBeanServer jmxmbs = (JmxMBeanServer) mbserver;
					Field[] fields = jmxmbs.getClass().getDeclaredFields();
					for (Field f : fields) {
						try { 
							if (!"mbsInterceptor".equals(f.getName()))
								continue;
							f.setAccessible(true);
							DefaultMBeanServerInterceptor mbInterceptor = (DefaultMBeanServerInterceptor)f.get(jmxmbs); 
							f.setAccessible(false);
							Class<DefaultMBeanServerInterceptor> mbInterceptorClass = DefaultMBeanServerInterceptor.class;
							Method[] methods = mbInterceptorClass.getDeclaredMethods();
			      			for (Method method : methods) {
			      				if (!"getMBean".equals(method.getName()))
			      					continue;
			      				method.setAccessible(true);
			      				Object beanModelObject = method.invoke(mbInterceptor, name);
					            method.setAccessible(false);
					            BaseModelMBean baseModelMBean = (BaseModelMBean)beanModelObject;					      				
			      				cluster = (SimpleTcpCluster)baseModelMBean.getManagedResource();
			      			}
						}
						catch (Exception e) { com.roth.base.log.Log.logException(e, SYSTEM); }
					}
				}
			}
		}
		
		if (cluster != null) {
			Log.logDebug("This node is part of a cluster.", SYSTEM);
			channel = cluster.getChannel();
		}
		else {
			Log.logDebug("This node is not part of a cluster.", SYSTEM);
		}
		
		return channel;
	}
	
	private static boolean registered = false;
	private static Map<String, ChannelMessageLogEntry> messageLog;
	static {
		messageLog = new HashMap<>();
	}
	
	/**
	 * Register a channel listener for processing messages between cluster nodes.
	 * @param listener
	 */
	public static void registerListener() {
		if (!registered) {
			channel = getChannel();
			if (channel != null)
				channel.addChannelListener(new ChannelMessageListener());
			registered = true;
		}
	}
	
	public static Member[] getClusterNodes() {
		return channel == null ? null : channel.getMembers();
	}
	
	/**
	 * Send a message to all other cluster nodes.
	 * @param message
	 * @throws ChannelException
	 */
	public static void sendMessage(ChannelMessage message, boolean resending) throws ChannelException {
		if (channel != null) {
			channel.send(getClusterNodes(), message, Channel.SEND_OPTIONS_DEFAULT);
			if (!resending && !(message instanceof ChannelQuery))
				messageLog.put(message.getClass().getCanonicalName(), new ChannelMessageLogEntry(message, LocalDateTime.now()));
		}
	}
	
	/**
	 * Send a message to a specific cluster node.
	 * @param member
	 * @param message
	 * @throws ChannelException
	 */
	public static void sendMessage(Member member, ChannelMessage message) throws ChannelException {
		if (channel != null)
			channel.send(new Member[] { member }, message, Channel.SEND_OPTIONS_DEFAULT);
	}
	
	
	/**
	 * Check whether a message was sent before this node came online.
	 * @param message
	 * @throws ChannelException
	 */
	public static void queryMessage(ChannelMessage message) throws ChannelException {
		new ChannelQuery(message).sendMessage();
	}
	
	public static void processQuery(ChannelQuery query) {
		
	}
	
	public static void processResponse(ChannelResponse response) {
		
	}
}
