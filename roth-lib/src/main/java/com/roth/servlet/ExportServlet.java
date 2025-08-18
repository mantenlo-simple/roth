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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.roth.base.log.Log;
import com.roth.base.util.Data;
import com.roth.export.util.CsvOutputStream;
import com.roth.export.util.CsvRecord;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class ExportServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setCharacterEncoding("UTF-8");
		Object data = getData(request);
		Map<String,String> columns = getColumns(request);
		String output = request.getParameter("output");
		String filename = getFilename(request);
		if (output.equals("csv"))
			putCsv(request, response, data, columns, filename);
		else // This is where we can put "xlsx", etc.
			putError(response);
	}
	
	protected Object getData(HttpServletRequest request) {
		// Parameter: "dataSource"
		String dataSource = request.getParameter("dataSource");
		String[] d = dataSource.split("\\.");
		String scope = (d.length == 2) ? d[0].replace("Scope", "") : "session";
		String name = d[d.length - 1];

		if (scope.equals("request")) return request.getAttribute(name);
		else if (scope.equals("session")) return request.getSession().getAttribute(name);
		else if (scope.equals("application")) return request.getSession().getServletContext().getAttribute(name);
		else return null;
	}
	
	protected String getFilename(HttpServletRequest request) {
		String filename = request.getParameter("filename");
		if (filename == null) {
			filename = request.getParameter("dataSource");
			int i = filename.indexOf('.');
			while (i > -1) { 
				filename = filename.substring(i + 1);
				i = filename.indexOf('.');
			}
			filename += "." + request.getParameter("output");
		}
		return filename;
	}

	protected Map<String,String> getColumns(HttpServletRequest request) {
		// Parameter: "columns"
		String columns = request.getParameter("columns");
		String[] c = columns.split(",");
		Map<String,String> map = new LinkedHashMap<>();
		for (int i = 0; i < c.length; i++) {
			String[] col = c[i].split("\\|");
			if (col[2].equals("true")) 
				map.put(col[0], col[1]);
		}
		return map;
	}
	
	protected void putError(HttpServletResponse response) {
		try { response.sendError(500); }
		catch (Exception e) { Log.logException(e, null); }
	}
	
	protected void setFileName(HttpServletResponse response, String filename) {
		String contentType = switch (filename.substring(filename.lastIndexOf(".") + 1)) {
		case "csv" -> "text/csv";
		default -> "text/plain";
		};
		response.setContentType(contentType);
		response.addHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
	}
	
	protected void putCsv(HttpServletRequest request, HttpServletResponse response, Object data, Map<String,String> columns, String filename) {
		try (ByteArrayOutputStream output = new ByteArrayOutputStream(); CsvOutputStream csv = new CsvOutputStream(output)) {
			// Generate header row
			CsvRecord header = new CsvRecord();
			for (Entry<String,String> entry : columns.entrySet())
				header.putString(entry.getValue());
			csv.write(header);
			// Generate data rows
			for (int i = 0; i < Data.sizeOf(data); i++)
				csv.write(toCsvRecord(Data.itemOf(data, i), columns, request));
			// Output file
			setFileName(response, filename);
			response.getWriter().print(output.toString());
		}
		catch (Exception e) { Log.logException(e, null); }
	}
	
	@SuppressWarnings("unchecked")
	protected CsvRecord toCsvRecord(Object item, Map<String,String> columns, HttpServletRequest request) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
		CsvRecord line = new CsvRecord();
		for (String name : columns.keySet())
			line.putString(Data.obj2Str(Data.fieldOf(item, name), (Map<String,String>)request.getSession().getAttribute("formats")));
		return line;
	}

}
