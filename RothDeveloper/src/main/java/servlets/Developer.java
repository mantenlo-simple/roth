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
package servlets;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import jakarta.servlet.annotation.WebServlet;

import com.roth.base.log.Log;
import com.roth.base.util.Data;
import com.roth.developer.model.GenModel;
import com.roth.developer.model.JndiSettingBean;
import com.roth.developer.model.SqlPost;
import com.roth.developer.util.AuthUtil;
import com.roth.developer.util.DeveloperUtil;
import com.roth.jdbc.meta.model.ColumnInfoBean;
import com.roth.jdbc.meta.model.IndexInfoBean;
import com.roth.jdbc.meta.model.TableInfoBean;
import com.roth.jdbc.util.JndiUtil;
import com.roth.jdbc.util.TableUtil;
import com.roth.servlet.ActionServlet;
import com.roth.servlet.annotation.ActionServletSecurity;
import com.roth.servlet.annotation.Navigation;
import com.roth.servlet.annotation.SimpleAction;

@WebServlet(urlPatterns = "/Developer/*")
@ActionServletSecurity(roles = {"SystemAdmin", "Developer"})
@Navigation(simpleActions = { @SimpleAction(name = "openDsn", action = "begin") })
public class Developer extends ActionServlet {
	private static final long serialVersionUID = 1L;

	@Action(forwards = { @Forward(name = SUCCESS, path = "index.jsp", pathMobi = "index_mobi.jsp"),
			             @Forward(name = "dialog", path = "_index.jsp"),
			             @Forward(name = "openDsn", path = "_opendsn.jsp") })
	@MethodSecurity(methods = GET)
	public String begin(ActionConnection conn) {
		try {
			List<JndiSettingBean> settings = new DeveloperUtil("roth").getJndiNameSettings();
			putBean(settings, "jndiSettings", "session", conn);
			Map<String,String> jndiNames = new LinkedHashMap<>();
			
			for (JndiSettingBean bean : settings)
				if (conn.hasRole("SystemAdmin") || bean.getAvailable().equals("Y"))
					jndiNames.put(bean.getJndiName(), bean.getJndiName());
			
			putBean(jndiNames, "jndiNames", conn);
			String firstJndiName = jndiNames.get(jndiNames.keySet().iterator().next());
			putBean(firstJndiName, "firstJndiName", conn);
			Map<String,String> schemas = new DeveloperUtil(firstJndiName).getSchemas().stream().sorted().collect(Collectors.toMap(v -> v, v -> v, (u, v) -> {
		        throw new IllegalStateException(String.format("Duplicate key %s", u));
		    }, 
		    LinkedHashMap::new));
			putBean(schemas, "schemas", conn);
		}
		catch (Exception e) {
			Log.logException(e, conn.getUserName());
			conn.putError("Error retrieving JNDI Names:<br/>" + e.getMessage());
		}
		putBean(conn.getString("dailog") != null, "dialog", "request", conn);
		return isCallingActionName("openDsn", conn) ? "openDsn" : conn.getString("dialog") != null ? "dialog" : SUCCESS;
	}
	
	@Action(responses = { @Response(name = SUCCESS) })
	@MethodSecurity(methods = GET)
	public String getSchemaOptions(ActionConnection conn) throws SQLException {
		String jndiName = conn.getString("jndiName");
		Map<String,String> schemas = new DeveloperUtil(jndiName).getSchemas().stream().sorted().collect(Collectors.toMap(v -> v, v -> v, (u, v) -> {
	        throw new IllegalStateException(String.format("Duplicate key %s", u));
	    }, 
	    LinkedHashMap::new));
		conn.printJson(schemas);
		return SUCCESS;
	}
	
	@Action(forwards = { @Forward(name = SUCCESS, path = "_index.jsp") })
	@MethodSecurity(methods = GET)
	public String load(ActionConnection conn) throws Exception {
		return SUCCESS;
	}
	
	@Action(responses = { @Response(name = SUCCESS, message = "") })
	@MethodSecurity(methods = POST)
	public String save(ActionConnection conn) throws Exception {
		return SUCCESS;
	}
	
	private JndiSettingBean getSettings(String jndiName) throws SQLException {
		List<JndiSettingBean> settings = new DeveloperUtil("roth").getJndiNameSettings();
		JndiSettingBean key = new JndiSettingBean();
		key.setJndiName(jndiName);
		int x = Collections.binarySearch(settings, key);
		return x < 0 ? null : settings.get(x);	
	}
	
	private static final String[] QUERY_KEYWORDS = {"SELECT", "SHOW", "EXPLAIN", "DESCRIBE", "WITH"};
	
	@Action(responses = { @Response(name = SUCCESS),
			              @Response(name = FAILURE, httpStatusCode = 500),
			              @Response(name = "unauthorized", httpStatusCode = 403, message = "The password you entered is invalid.") })
	@MethodSecurity(methods = POST)
	public String exec(ActionConnection conn) throws SQLException {
		String jndiname = conn.getString("jndiname");
		JndiSettingBean settings = getSettings(jndiname);
		
		if (settings == null || (!conn.hasRole("SystemAdmin") && !settings.getAvailable().equals("Y")))
			throw new SQLException("The requested data source is not found or access is denied.");
		
		Integer limit = conn.getInteger("limit");
		Integer maxlen = conn.getInteger("maxlen");
		String schema = conn.getString("schema");
		
		//String name = conn.getRequest().getParameter("name");
		String statement = conn.getString("statement");
		// Reverse the statement value.  Its characters are posted in reverse order to keep firewalls from thinking it's SQL injection.
		String rev = "";
		for (int i = statement.length() - 1; i >= 0; i--)
			rev += statement.charAt(i);
		statement = rev;
		if (statement.contains("\n")) 
			statement = statement.replaceAll("\r", "");
		String execType = "";
		//Date start = new Date();
		if (statement != null) {
			int pos = statement.indexOf(";");
			if (pos > -1) statement = statement.substring(0, pos);
			statement = statement.trim();
			pos = statement.indexOf(" ");
			execType = statement.substring(0, pos).trim().toUpperCase();
		}
		try {
			if (!Data.in(execType, QUERY_KEYWORDS)) { //(!execType.equals("SELECT") && !execType.equals("SHOW") && !execType.equals("DESC") && !execType.contentEquals("WITH")) {
				String password = conn.getRequest().getParameter("password");
				if (!new AuthUtil().isAuthorized(getUserName(conn), password)) 
					return "unauthorized";
			}
		
			DeveloperUtil util = new DeveloperUtil(jndiname);
			util.setSchema(schema);
			// Evaluate any present environment variable references.
			statement = util.evalEnv(statement);
			// Evaluate any present role property value references.
			statement = util.evalRpv(statement, conn.getUserName());
			statement = util.evalParams(statement, conn.getString("params"));
			conn.getResponse().setContentType("text/html");
			conn.getResponse().setCharacterEncoding("UTF-8");
			conn.println("<pre>");
			if (Data.in(execType, QUERY_KEYWORDS)) //(execType.equals("SELECT"))
				util.execQuery(statement, limit, maxlen, conn.getResponse().getWriter());
			else {
				if (!conn.hasRole("SystemAdmin") && settings.getReadonly().equals("Y"))
					throw new SQLException("Write access to the selected data source is denied.");
				conn.println(statement + "\n\n" + 
						     util.execUpdate(statement) + " rows " + execType.toLowerCase() + (execType.equals("INSERT") ? "e" : "") + "d.");
			}
			conn.println("</pre>");
			/*
			Date end = new Date();
            AdhocHistBean hist = new AdhocHistBean();
            hist.setJndiname(jndiname);
            hist.setRowLimit(limit);
            hist.setMaxLength(maxlen);
            hist.setDescription(name);
            hist.setStatement(statement);
            hist.setExecBy(getUserName(conn));
            hist.setExecDts(start);
            hist.setEndDts(end);
            new TableUtil("roth").save(hist);
            */ 
		}
		catch (Exception e) {
			conn.getResponse().reset();
			return returnLogException(e, conn, FAILURE); 
		}
		return SUCCESS;
	}
	
	@Action(forwards = { @Forward(name = "schemas", path = "_schemas.jsp", pathMobi = "_schemas_mobi.jsp"),
			             @Forward(name = "tables", path = "_tables.jsp"),
			             @Forward(name = "columns", path = "_columns.jsp"), 
			             @Forward(name = "indexes", path = "_indexes.jsp"), 
			             @Forward(name = "foreignkeys", path = "_foreignkeys.jsp"), 
			             @Forward(name = "triggers", path = "_triggers.jsp", pathMobi = "_triggers_mobi.jsp") },
			responses = { @Response(name = FAILURE, httpStatusCode = 500),
			              @Response(name = "unknown", httpStatusCode = 500, message = "Unknown infoType parameter received.") })
	@MethodSecurity(methods = GET)
	public String getDatasourceInfo(ActionConnection conn) throws SQLException {
		String infoType = conn.getString("infoType");
		String jndiname = conn.getString("jndiname");
		
		if (infoType.equals("S")) {
			putBean(new DeveloperUtil(jndiname).getSchemas(), "schemas", "request", conn);
			return "schemas";
		}
		else if (infoType.equals("L")) {
			putBean(new DeveloperUtil(jndiname).getTables(conn.getString("schema")), "tables", "request", conn);
    		return "tables";
    	}
    	else if (infoType.equals("C")) {
    		putBean(new DeveloperUtil(jndiname).getColumns(conn.getString("tableId")), "columns", "request", conn);
    		return "columns";
    	}
    	else if (infoType.equals("I")) {
    		putBean(new DeveloperUtil(jndiname).getIndexes(conn.getString("tableId")), "indexes", "request", conn);
    		return "indexes";
    	}
    	else if (infoType.equals("D")) {
    		conn.println(conn.getString("tableName"));
    		//putBean(new AdhocUtil(jndiname).getData(conn.getString("tableId")), "data", "request", conn);
    		return FAILURE;
    	}
    	else if (infoType.equals("F")) {
    		putBean(new DeveloperUtil(jndiname).getForeignKeys(conn.getString("tableId")), "foreignkeys", "request", conn);
    		return "foreignkeys";
    	}
    	else if (infoType.equals("T")) {
    		putBean(new DeveloperUtil(jndiname).getTriggers(conn.getString("tableId")), "triggers", "request", conn);
    		return "triggers";
    	}
	    return "unknown";
	}
	
	@Action(responses = { @Response(name = SUCCESS),
			              @Response(name = FAILURE, httpStatusCode = 500) })
	@MethodSecurity(methods = GET)
	public String getScript(ActionConnection conn) {
		try {
			String jndiname = conn.getString("jndiname");
			String tableId = conn.getString("tableId");
			String schema = conn.getString("schema");
			String tableName = conn.getString("tableName");
			List<ColumnInfoBean> columns = new DeveloperUtil(jndiname).getColumns(tableId);
			List<IndexInfoBean> indexes = new DeveloperUtil(jndiname).getIndexes(tableId);
			String script = "<pre style=\"width: 100%; height: 100%; overflow: auto;\">CREATE TABLE " + schema + "." + tableName + "\n   (";
			for (ColumnInfoBean c : columns) {
				if (c.getColumnSequence() > 1) 
					script += ",\n    ";
				script += c.getColumnName() + " " + c.getColumnType() + (c.getNullConstraint().equals("NULL") ? "" : " " + c.getNullConstraint());
				if (c.getColumnDefault() != null)
					script += " DEFAULT '" + c.getColumnDefault() + "'";
			}
			String idx = "";
			for (IndexInfoBean i : indexes) {
				if (i.getIndexName().equals("PRIMARY"))
					script += ",\n    PRIMARY KEY (" + i.getColumns() + ")";
				else
					idx += "\nCREATE " + i.getUniqueConstraint() + " INDEX " + schema + "." + i.getIndexName() + 
					        " ON " + schema + "." + tableName + " (" + i.getColumns() + ");";
			}
			script += ");" + idx + "</pre>";
			conn.println(script);
			return SUCCESS;
		}
		catch (Exception e) {
			return returnLogException(e, conn, FAILURE);
		}
	}
	
	@Action(forwards = { @Forward( name = SUCCESS, path = "createPojo.jsp") })
	@MethodSecurity(methods = GET)
	public String createPojo(ActionConnection conn) {
		putBean(new SqlPost(), "sqlPost", "request", conn);
		putBean(JndiUtil.getJndiNames(), "jndiNames", "request", conn);
		return SUCCESS;
	}
	
	@Action(forwards = { @Forward( name = SUCCESS, path = "abbreviated.jsp") })
	@MethodSecurity(methods = GET)
	public String abbreviated(ActionConnection conn) {
		SqlPost bean = new SqlPost();
		bean.setJndiName(conn.getString("jndiname"));
		bean.setSchemaName(Data.trim(conn.getString("schema")));
		bean.setTableName(Data.trim(conn.getString("tableName")));
		bean.setStatement(conn.getString("statement"));
        Log.logDebug("PojoGen statement: " + conn.getString("statement"), conn.getUserName());		
		try {
			if (conn.getString("tableId") != null) {
				List<IndexInfoBean> indexes = new DeveloperUtil(bean.getJndiName()).getIndexes(conn.getString("tableId"));
				for (IndexInfoBean i : indexes)
					if (i.getPrimaryKey().equals("Y"))
						bean.setPrimaryKey(i.getColumns().replaceAll(",", ""));
			}
		}
		catch (Exception e) {
			Log.logException(e, conn.getUserName());
		}
		putBean(bean, "sqlPost", "request", conn);
		return SUCCESS;
	}
	
    @Action(responses = { @Response( name = SUCCESS ) })
	@Post(beans = { @Bean(name = "sqlPost", beanClass = SqlPost.class, scope = "request") })
    @MethodSecurity(methods = POST)
	public String genModel(ActionConnection conn) throws Exception {
		SqlPost post = (SqlPost)conn.getRequest().getAttribute("sqlPost");
		if (post.getStatement() != null) {
			String rev = "";
			for (int i = post.getStatement().length() - 1; i >= 0; i--)
				rev += post.getStatement().charAt(i);
			post.setStatement(rev);
		}
		if (!post.getWholeSchema()) {
    		conn.getResponse().setContentType("application/octet-stream");
    		conn.getResponse().addHeader("Content-Disposition", "attachment; filename=\"" + post.getClassName() + ".java\"");
    		conn.getResponse().getOutputStream().print(GenModel.ParsePost(post));
    		conn.getResponse().flushBuffer();
		}
		else {
			String temp = Data.getWebEnv("rothTemp", "/temp/");
			String name = post.getClassName();
			List<TableInfoBean> tables = new DeveloperUtil(post.getJndiName()).getTables(post.getSchemaName());
			ArrayList<String> fileNames = new ArrayList<>();
			for (TableInfoBean table : tables) {
				Log.logDebug("Generating model for table: " + table.getTableName(), conn.getUserName(), "genModel");
				String tableName = table.getTableName();
				String className = name.replaceAll("\\*", Matcher.quoteReplacement(Data.upcaseFirst(GenModel.camelcase(table.getTableName()))));
				String fileName = className + ".java";
				fileNames.add(fileName);
				try (PrintStream out = new PrintStream(new FileOutputStream(temp + fileName))) {
					post.setTableName(tableName);
					post.setClassName(className);
					
					try {
						List<IndexInfoBean> indexes = new DeveloperUtil(post.getJndiName()).getIndexes(table.getTableId());
						if (indexes != null)
							for (IndexInfoBean i : indexes)
								if (i.getPrimaryKey().equals("Y"))
									post.setPrimaryKey(i.getColumns().replaceAll(",", ""));
						
						out.print(GenModel.ParsePost(post));
					}
					catch (Exception e) {
						Log.logException("GenModel failed for table: " + tableName, conn.getUserName(), e);
						new File(temp + fileName).delete();
					}
				}
			}
			
			conn.getResponse().setContentType("application/octet-stream");
    		conn.getResponse().addHeader("Content-Disposition", "attachment; filename=\"" + post.getSchemaName() + ".zip\"");
			ZipOutputStream out = new ZipOutputStream(conn.getResponse().getOutputStream());
			for (String fileName: fileNames) {
				ZipEntry e = new ZipEntry(fileName);
				out.putNextEntry(e);
				
				FileInputStream file = new FileInputStream(temp + fileName);
				
				byte[] bytes = new byte[1024];
				int len;
				while((len = file.read(bytes, 0, 1024)) > 0){
				    out.write(bytes, 0, len);
				}
				file.close();
				new File(temp + fileName).delete();
				
				out.closeEntry();
			}
			out.close();
			conn.getResponse().flushBuffer();
		}
    	return SUCCESS;
	}
    
    @Action(forwards = { @Forward( name = SUCCESS, path = "_settings.jsp" ) })
    @MethodSecurity(methods = GET)
	public String loadSettings(ActionConnection conn) throws Exception {
    	List<JndiSettingBean> settings = new DeveloperUtil("roth").getJndiNameSettings();
    	putBean(settings, "settings", "request", conn);
    	return SUCCESS;
    }
    
    @Action(responses = { @Response( name = SUCCESS ) })
    @Post(beans = { @Bean( name = "settings", beanClass = JndiSettingBean.class, typeClass = ArrayList.class) })
    @MethodSecurity(methods = POST)
	public String saveSettings(ActionConnection conn) throws Exception {
    	List<JndiSettingBean> settings = new DeveloperUtil("roth").getJndiNameSettings();
    	ArrayList<JndiSettingBean> changes = getBean(0, conn);
    	ArrayList<JndiSettingBean> updates = new ArrayList<>();
    	
    	for (int i = 0; i < settings.size(); i++) {
    		JndiSettingBean s = settings.get(i);
    		JndiSettingBean c = changes.get(i);
    		if (s.getJndiSettingId().equals(c.getJndiSettingId())) {
    			if (!s.getAvailable().equals(c.getAvailable()) || !s.getReadonly().equals(c.getReadonly())) {
	    			s.setAvailable(c.getAvailable());
	    			s.setReadonly(c.getReadonly());
	    			s.setUpdatedBy(conn.getUserName());
	    			updates.add(s);
	    		}
    		}
    		else
    			throw new Exception("A data corruption has been detected.");
    	}
    	
    	if (updates.size() > 0)
    		new TableUtil("roth").save(updates);
    	conn.getResponse().setContentType("text/plain");
    	conn.println("Settings updated successfully.");
    	return SUCCESS;
    }
}
