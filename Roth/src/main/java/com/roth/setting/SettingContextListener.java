/*
Copyright 2010 James M. Payne

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */
package com.roth.setting;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Set;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;

import org.apache.catalina.ha.tcp.SimpleTcpCluster;
import org.apache.catalina.tribes.Channel;
import org.apache.tomcat.util.modeler.BaseModelMBean;

import com.roth.base.log.Log;
import com.sun.jmx.interceptor.DefaultMBeanServerInterceptor;
import com.sun.jmx.mbeanserver.JmxMBeanServer;

public class SettingContextListener implements ServletContextListener {

	public static Channel getChannel() {
		String origin = "com.roth.setting.SettingContextListener.getChannel";
		
		ArrayList<MBeanServer> mbservers = MBeanServerFactory.findMBeanServer(null);
		MBeanServer mbserver = null;
		SimpleTcpCluster cluster = null;
		
		if (mbservers != null && mbservers.size() > 0)
			mbserver = mbservers.get(0);
		
		if (mbserver == null)
			Log.log("MESSAGE: ", "MBeanServer not found.", origin, "[SYSTEM]", false, null);
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
						catch (Exception e) { com.roth.base.log.Log.logException(e, "[SYSTEM]"); }
					}
				}
			}
		}
		
		if (cluster != null) {
			Log.logDebug("This node is part of a cluster.", "[SYSTEM]");
			return cluster.getChannel();
		}
		else {
			Log.logDebug("This node is not part of a cluster.", "[SYSTEM]");
			return null;
		}
	}
	
	@Override
	public void contextInitialized(ServletContextEvent arg0) {
		String origin = "com.roth.setting.SettingContextListener.contextInitialized";
		String message = "SettingContextListener initializing.";
		Log.log("MESSAGE: ", message, origin, "[SYSTEM]", false, null);
		
		Channel channel = getChannel();
		
		if (channel != null) {
			channel.addChannelListener(new SettingChannelListener());
		}
	}

	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		String origin = "com.roth.setting.SettingContextListener.contextDestroyed";
		String message = "SettingContextListener destroyed.";
		Log.log("MESSAGE: ", message, origin, "[SYSTEM]", false, null);
	}

}