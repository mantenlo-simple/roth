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
package com.roth.servlet;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.roth.base.log.Log;
import com.roth.base.util.Data;
import com.roth.base.util.Data.Pad;
import com.roth.export.util.JsonUtil;
import com.roth.jdbc.meta.model.Manifest;
import com.roth.jdbc.meta.model.Table;
import com.roth.jdbc.meta.model.TableInfoBean;
import com.roth.jdbc.meta.model.Template;
import com.roth.jdbc.meta.util.MetaUtil;
import com.roth.portal.model.BookBean;
import com.roth.portal.model.DesktopBean;
import com.roth.portal.model.PageBean;
import com.roth.portal.model.PortletBean;
import com.roth.portal.model.RoleBean;
import com.roth.servlet.ActionServlet.Action;
import com.roth.servlet.util.ChannelMessageUtil;
import com.roth.servlet.util.PortalConfig;
import com.roth.tags.el.Util;
import com.roth.tags.html.Portlet;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.ServletRegistration;

public class RothContextListener implements ServletContextListener {
	private static final String MESSAGE = "MESSAGE: ";
	private static final String SYSTEM = "[SYSTEM]";
	private static final String CONFIG_FILE = "/WEB-INF/roth-config.json";
	private static final String MANIFEST_FILE = "/WEB-INF/classes%s/_manifest.json";
	
	private static Map<String,Map<String,Template>> templates;
	static {
		templates = new ConcurrentHashMap<>();
	}
	
	private static boolean logoShown;
	private static final String LOGO = """
			
			                                                                                                                        .....:.                         
			                                                                                                                     ..:-+*#@@:.                        
			    VERSION                                                                                                         -*@@@@@@@@...                       
			                                                                                                                    ...=@@@@@@...                       
			                                                                                  .-.                               ....%@@@@@....                      
			                      ..........                             ......            .:*@*:.........             ......:. ....#@@@@@....       ........       
			          ......-++=+*%@@@@%#*=:...                  .-=++**##**+=:....      .=%@@@@@@@%%###*******++++++++**#%@%-   ...*@@@@@.....:-=++******+=-...    
			      .....:-+%@+.......-*@@@@@@%+:..         ...:=#%+-....:=*@@@@@@#-...   .-::::......:=*##+-::.     ..:-+%@@+..   ...+@@@@@+*#*=:. ..:=#@@@@@@@*..   
			    .=#%%@@@@@@-...    ....+@@@@@@@=..      ...+%@@*..      ...-#@@@@@%=...    ......-*%@#=...             ..%-.     ...=@@@@@@*:..     ...:%@@@@@@#..  
			     ..*@@@@@@#...       ...=@@@@@@@:..   ...=@@@@=..        ....=@@@@@@#:...  ....-%@@@-...                 :       ...-@@@@@#...       ...:@@@@@@@... 
			     ...=@@@@@#...        ...@@@@@@@=.......+@@@@*...          ...-@@@@@@#........*@@@@:...                          ...-@@@@@=...        ...*@@@@@@:.. 
			     ....#@@@@#...        ...#@@@@@@=......:@@@@@-...           ...=@@@@@@+......#@@@@+...                            ..-@@@@@:...         ..-@@@@@@:.. 
			      ...+@@@@#...        ...%@@@@@%.......#@@@@@...             ...%@@@@@@.....*@@@@@:...                            ..-@@@@@:..          ...@@@@@@... 
			      ...+@@@@#....     ...:%@@@*-...  ...:@@@@@@:..             ...+@@@@@@:...:@@@@@@:...                            ..-@@@@@:..          ...%@@@@#... 
			      ...+@@@@%..........:+@%*=....     ..:@@@@@@+...             ..=@@@@@#....:@@@@@@+...                            ..-@@@@@:..          ...%@@@@#... 
			      ...+@@@@@=----==+*#+-:.....       ...%@@@@@@:..             ..=@@@@@:.....%@@@@@@=...                           ..-@@@@@:..          ...@@@@@#... 
			      ...*@@@@@==+*@@@@@@@%+:....        ..=@@@@@@#:...          ...+@@@@:......=@@@@@@@+....               .        ...:@@@@@-..          ...@@@@@#... 
			      ...#@@@@%.....-#@@@@@@@*:....      ...#@@@@@@%:....        ...%@@#:...  ...+@@@@@@@%-....          ..+@#**+.   ...:@@@@@=..          ..:@@@@@*... 
			      ...%@@@@%.......:+%@@@@@@#=....     ...*@@@@@@@+.....      ..+@%-...     ...-%@@@@@@@%=.....    ....+@@@@+.    ...:@@@@@*...         ..-@@@@@#... 
			      ..:@@@@@@....  ....-*@@@@@@@#=:...   ...:+%@@@@@@#+-:....:-+#*-..          ...-#@@@@@@@@#+=-:..:-=*%@%+-..      ..-@@@@@%...         ..=@@@@@@:.. 
			       .=@@@@@@=..      ....-*%@@@@@@#+-:.   .....-+#%@@@@@@%#*+-.                  ...:=+*#%%@@@@@%%#*+-:...         ..=@@@@@@=.           .+@@%%%%#-::
			        :--:::::           .....:-======--:.     ...........                           ..............                   ==--:::.             ........   
			                                 ...                                                                                                                    
			""";
	private static void showLogo() {
		if (!logoShown) {
			Properties prop = new Properties();
		    String c = Portlet.class.getSimpleName() + ".class";
		    String cp = Portlet.class.getResource(c).toString();
		    String mp = cp.substring(0, cp.lastIndexOf("!") + 1) + "/META-INF/MANIFEST.MF";
		    try {
				prop.load(new URI(mp).toURL().openStream());
				//prop.load(Paths.get(mp).toUri().toURL().openStream());
			} catch (IOException | URISyntaxException e) {
				e.printStackTrace();
			}
		    String version = prop.getProperty("Extension-Name", "") + " ";
		    version += prop.getProperty("Implementation-Version", "0.0.0");
			String logo = LOGO.replace(Data.pad("VERSION", ' ', version.length(), Pad.RIGHT), version);
			Log.log("INIT: ", logo, "Roth", SYSTEM, false, null);
			logoShown = true;
		}
	}
	
	@Override
	public void contextInitialized(ServletContextEvent arg0) {
		showLogo();
		String origin = "com.roth.servlet.RothContextListener.contextInitialized";
		String message = "RothContextListener initializing.";
		ServletContext servletContext = arg0.getServletContext();
		String name = servletContext.getServletContextName();
		String contextPath = servletContext.getContextPath();
		templates.put(contextPath, new ConcurrentHashMap<>());
		message += " [" + name + "] " + contextPath;
		Log.log(MESSAGE, message, origin, SYSTEM, false, null);
		CssServlet.clearContextCache(servletContext.getRealPath(contextPath));
		Log.log(MESSAGE, String.format("CssServlet cache cleared ( %s )", contextPath), origin, SYSTEM, false, null);
		Util.registerPath(contextPath);
		Util.registerPath(contextPath + "/");
		
		Map<String, ? extends ServletRegistration> servlets = servletContext.getServletRegistrations();
		for (Entry<String, ? extends ServletRegistration> entry : servlets.entrySet()) {
			ServletRegistration reg = entry.getValue();
			Class<?> servletClass = null;
			try { servletClass = Class.forName(reg.getClassName(), false, servletContext.getClassLoader()); }
			catch (ClassNotFoundException e) { Log.logException(e, null); }
			
			if (servletClass != null)
				processMappings(contextPath, servletClass, servlets.get(entry.getKey()).getMappings());
		}
		// Register all other resource paths that are not hidden in META-INF or WEB-INF; this includes css, js, and jsp files (but not limited to)
		registerResourcePaths(servletContext, contextPath, "/");
		Log.log(MESSAGE, "Resource paths registered.", origin, SYSTEM, false, null);
		
		// Register configuration, if definition file is present.
		PortalConfig config = registerConfiguration(servletContext);
		Log.log(MESSAGE, "Portal configuration registered.", origin, SYSTEM, false, null);
		
		boolean stackTraceCurrent = Log.getHub().getLogStackTrace();
		Log.getHub().setLogStackTrace(true);
		
		if (config != null) {
			processTemplates(config.getMetaTemplatePath(), servletContext);
			processTables(config.getMetaTablePath(), config.getMetaInitPath(), servletContext);
		}
		
	//	servletContext.setAttribute(contextPath, arg0);
		
		Log.getHub().setLogStackTrace(stackTraceCurrent);
		
		ChannelMessageUtil.registerListener();
	}
	
	/**
	 * Process all servlet mappings in the context.
	 * @param contextPath
	 * @param servletClass
	 * @param mappings
	 */
	protected void processMappings(String contextPath, Class<?> servletClass, Collection<String> mappings) {
		String origin = "processMappings";
		for (String mapping : mappings) {
			if (mapping.equals("/") || !mapping.startsWith("/"))
				continue;
			String pMapping = Data.pad(mapping, ' ', 30, Pad.RIGHT);
			String pCanonicalName = Data.pad(servletClass.getCanonicalName(), ' ', 40, Pad.RIGHT);
			String message = String.format("    Registering path ( %s ) for class [ %s ]", pMapping, pCanonicalName);
			Log.log(MESSAGE, message, origin, SYSTEM, false, null);
			String servletPath = contextPath + mapping;
			// Register the servlet path without ending '/'
			Util.registerPath(servletPath.replace("/*", ""));
			// Register the servlet path with ending '/'
			Util.registerPath(servletPath.replace("*", ""));
			// Register all action methods in the servlet
			if (servletClass != null) {
				Method[] methods = servletClass.getMethods();
				for (Method m : methods) 
					if (m.getAnnotation(Action.class) != null) {
						String methodPath = servletPath.replace("*", m.getName());
						Util.registerPath(methodPath);
					}
			}
		}
	}
	
	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		String origin = "com.roth.servlet.RothContextListener.contextDestroyed";
		String message = "RothContextListener destroyed.";
		ServletContext servletContext = arg0.getServletContext();
		String name = servletContext.getServletContextName();
		String path = servletContext.getContextPath();
		message += " [" + name + "] " + path;
		Log.log(MESSAGE, message, origin, SYSTEM, false, null);
	}
	
	/**
	 * Recursively traverse the file tree from the web root of the servlet context to register all non-hidden files. 
	 * @param servletContext
	 * @param contextPath
	 * @param childPath
	 */
	protected void registerResourcePaths(ServletContext servletContext, String contextPath, String childPath) {
		Set<String> paths = servletContext.getResourcePaths(childPath);
		if (paths != null)
			for (String path : paths) {
				if ("/META-INF/".equals(path) || "/WEB-INF/".equals(path))
					continue;
				if (path.endsWith("/"))
					registerResourcePaths(servletContext, contextPath, path);
				else {
					String resourcePath = contextPath + path;
					Util.registerPath(resourcePath);
				}
			}
	}
	
	/**
	 * Register roles listed in WEB-INF/roth.csv, if file is found.<br/>
	 * Format expected:<br/>
	 * role,[role name],[role description]
	 * @param servletContext
	 */
	protected static PortalConfig registerConfiguration(ServletContext servletContext) {
		if (!Data.fileExists(servletContext, CONFIG_FILE)) {
			String origin = "registerConfiguration";
			Log.log(MESSAGE, "    Configuration file not found ( " + CONFIG_FILE + " ).", origin, SYSTEM, false, null);
			return null;
		}
		String configFile = Data.readTextFile(servletContext, CONFIG_FILE, StandardCharsets.UTF_8);
		if (configFile != null)
			try {
				RothContextUtil util = new RothContextUtil();
				PortalConfig config = JsonUtil.jsonToObj(configFile, PortalConfig.class);
				if (config != null)
					processConfig(config, util, servletContext);
				return config;
			} catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException | SQLException e) {
				Log.logException(e, null);
			}
		return null;
	}
	
	protected static void processConfig(PortalConfig config, RothContextUtil util, ServletContext servletContext) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SQLException {
		if (config.getRoles() != null)
			for (RoleBean role : config.getRoles())
				util.processBean(role);
		if (config.getPortlets() != null)
			for (PortletBean portlet : config.getPortlets())
				util.processBean(portlet);
		if (config.getBooks() != null)
			for (BookBean book : config.getBooks())
				util.processBean(book);
		if (config.getPages() != null)
			for (PageBean page : config.getPages())
				util.processBean(page);
		if (config.getThemes() != null)
			for (String theme : config.getThemes())
				util.processTheme(theme, servletContext);
		if (config.getDesktops() != null)
		for (DesktopBean desktop : config.getDesktops())
				util.processBean(desktop);
	}
	
	protected static void processTemplates(String templatePath, ServletContext servletContext) {
		if (templatePath == null)
			return;  
		String origin = "com.roth.servlet.RothContextListener.processTemplates";
		String contextPath = servletContext.getContextPath();
		// process all files in path (no specific order?)
		try {
			Set<String> files = Data.getFileList(servletContext, "\\WEB-INF\\classes" + templatePath);
			if (files != null)
				for (String filename : files) {
					String tableJson = Data.readTextFile(servletContext, "\\WEB-INF\\classes" + templatePath + "\\" + filename, StandardCharsets.UTF_8);
					try {
						Template template = JsonUtil.jsonToObj(tableJson, Template.class);
						templates.get(contextPath).put(template.getName(), template);
					} catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
						Log.logException(e, null);
					}
				}
			Log.log(MESSAGE, "Metadata templates processed.", origin, SYSTEM, false, null);
		} catch (IOException e) {
			Log.logException(e, null);
		}
	}
	
	protected static void processTables(String tablePath, String initPath, ServletContext servletContext) {
		if (tablePath == null)
			return;
		String origin = "com.roth.servlet.RothContextListener.processTables";
		String contextPath = servletContext.getContextPath();
		// Open _manifiest.json and process tables in order found there.
		String manifestFile = Data.readTextFile(servletContext,  String.format(MANIFEST_FILE, tablePath), StandardCharsets.UTF_8);
		try {
			Manifest manifest = JsonUtil.jsonToObj(manifestFile, Manifest.class);
			if (manifest == null)
				return;
			MetaUtil util = new MetaUtil(manifest.getJndiName()); 
			Map<String,TableInfoBean> tables = mapTables(util.getTables(manifest.getSchema()));
			if (manifest.getDrops() != null)
				for (String tableName : manifest.getDrops())
					if (tables.get(tableName) != null) {
						util.dropTable(tableName, true);
						Log.log(MESSAGE, String.format("    Metadata table dropped [%s].", tableName), origin, SYSTEM, false, null);
					}
			if (manifest.getTables() != null)
				for (String tableName : manifest.getTables()) {
					// Open JSON file for tableName (if it exists) and...
					String filename = String.format("%s.json", tableName);
					String tableJson = Data.readTextFile(servletContext, "\\WEB-INF\\classes" + tablePath + "\\" + filename, StandardCharsets.UTF_8);
					Table table = JsonUtil.jsonToObj(tableJson, Table.class);
					//   - Check to see if it references a template
					if (table.getTemplate() != null)
						table.getColumns().addAll(templates.get(contextPath).get(table.getTemplate()).getColumns());
					//   - if not exists, create table (easy)
					if (tables.get(tableName) == null) {
						util.createMetaTable(table);
						String initMsg = "";
						String init = Data.readTextFileQuiet(servletContext, "\\WEB-INF\\classes" + initPath + "\\" + filename.replace("json", "sql"), StandardCharsets.UTF_8);
						if (init != null) {
							util.initTable(init);
							initMsg = "and initialized ";
						}
						Log.log(MESSAGE, String.format("    Metadata table created %s[%s].", initMsg, tableName), origin, SYSTEM, false, null);
					}
					//   - if exists, alter table (complicated)
					else {
						// Check metadata to see if the table is different
						//  - if so, then alter the table and log an update
						// Log.log(MESSAGE, String.format("Meta table updated [%s].", tableName), origin, SYSTEM, false);
					}
				}
			Log.log(MESSAGE, "Metadata tables processed.", origin, SYSTEM, false, null);
		} catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException | SQLException e) {
			Log.logException(e, null);
		}
	}
	
	protected static Map<String,TableInfoBean> mapTables(List<TableInfoBean> tables) {
		Map<String,TableInfoBean> result = new HashMap<>();
		if (tables != null)
			for (TableInfoBean table : tables)
				result.put(table.getTableName(), table);
		return result;
	}
}