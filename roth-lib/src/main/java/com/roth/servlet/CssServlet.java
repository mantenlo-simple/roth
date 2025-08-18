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
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.roth.base.log.Log;
import com.roth.base.util.Data;
import com.roth.servlet.util.Browser;
import com.roth.servlet.util.Condition;
import com.roth.servlet.util.Evaluator;

import io.bit3.jsass.CompilationException;
import io.bit3.jsass.Compiler;
import io.bit3.jsass.Options;
import io.bit3.jsass.Output;
import io.bit3.jsass.OutputStyle;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class CssServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setCharacterEncoding("UTF-8");
		Browser browser = new Browser(request.getHeader("User-Agent"));
		String path = request.getRequestURI().replace(request.getContextPath(), "")
											 .replace("CSS", "css");
		Log.logDebug(String.format("Processing path: %s", path), null, "CssServlet.compile");
		boolean compressed = !"false".equalsIgnoreCase(Data.nvl(Data.envl(request.getParameter("compressed")), "true"));
		boolean refresh = "true".equalsIgnoreCase(Data.nvl(Data.envl(request.getParameter("refresh")), "false"));
		response.getWriter().print(getFile(path, request.getServletContext(), browser, compressed, refresh));
	}
	
	private static Map<String,String> fileCache = new ConcurrentHashMap<>();
	public static String getCache(String key) { return fileCache.get(key); }
	public static void putCache(String key, String value) {
		if (!Data.getWebEnv("cssCache", true))
			return;
		fileCache.put(key, value); 
	}
	public static void removeCache(String key) { fileCache.remove(key); }
	public static void clearCache() { fileCache.clear(); }
	public static void clearContextCache(String contextRoot) {
		for (String key : fileCache.keySet())
			if (key.startsWith(contextRoot))
				removeCache(key);
	}

	public static String getFile(String filename, ServletContext context, Browser browser, boolean compressed, boolean refresh) throws IOException {
		return getFile(filename, context, browser, true, compressed, refresh);
	}
	
	private static String getFile(String filename, ServletContext context, Browser browser, boolean compiled, boolean compressed, boolean refresh) throws IOException {
		String realPath = context.getRealPath(filename);
		String cache = realPath == null || refresh || !compiled ? null : getCache(realPath);
		if (cache != null) {
			Log.logDebug(String.format("Returning cache for file: %s", filename), null, "CssServlet.getFile");
			return cache;
		}
		Log.logDebug(String.format("Opening file: %s", filename), null, "CssServlet.getFile");
		String source = new String(Files.readAllBytes(Paths.get(realPath)));
		if (!compiled)
			return source;
		String output = compile(processFile(source, browser, context, filename), compressed);
		if (realPath != null)
			putCache(realPath, output);
		return output;
	}
	
	public static String processFile(String source, Browser browser, ServletContext context, String path) {
		Evaluator evaluator = new Evaluator();
		String[] lines = Data.splitLF(source);
		StringBuilder result = new StringBuilder("");
		for (String line : lines) {
			line = processLine(line, browser, evaluator, context);
			if (evaluator.getCond() != null)
		    	Log.logDebug(browser.toString() + "\n    " + evaluator.getCond().toString() + "\n    Line: " + line, null, "CssServlet");
			if (line != null)
				result.append(line + "\n");
		}
		return result.toString();
	}
	
	public static String compile(String source, boolean compressed) {
		LocalDateTime start = LocalDateTime.now();
		Options options = new Options();
		options.setIndent("    ");
		options.setOutputStyle(compressed ? OutputStyle.COMPRESSED : OutputStyle.EXPANDED);
		try {
			Output output = new Compiler().compileString(source, null, null, options);
			return output.getCss();
		} catch (CompilationException e) {
			Log.logException(e, null);
			return "";
		} finally {
			LocalDateTime end = LocalDateTime.now();
			Log.logDebug(String.format("SCSS Compile Time: %.2f milliseconds.", Double.valueOf(Duration.between(start, end).getNano()) / 1_000_000), null, "CssServlet.compile");
		}
	}

	protected static String processLine(String source, Browser browser, Evaluator evaluator, ServletContext context) {
		boolean inBlock = evaluator.getCond() != null && 
						  evaluator.getCond().isBlock();
		boolean inValidBlock = inBlock && 
							   !evaluator.getStack().isEmpty() && 
							   !evaluator.getStack().get(evaluator.getStack().size() - 1).isEmpty();
		if (source.trim().startsWith("/*if ")) {
			// Condition
			int s = source.indexOf("/*");
			int e = source.indexOf("*/");
			evaluator.setCond(new Condition(source.substring(s, e + 2)));
			String line = source.substring(0, s) + source.substring(e + 2);
			if (evaluator.getCond().isBlock()) {
				// Start of block
				List<String> list = new ArrayList<>();
				evaluator.getStack().add(list);
				if (evaluator.getCond().satisfiesCondition(browser)) {
					list.add(line);
				}
				return null;
			} else
				return (evaluator.getCond().satisfiesCondition(browser)) ? line : null;
		} else if (evaluator.getCond() != null && evaluator.getCond().isBlock() && source.contains("/*}*/")) {
			// End of block
			evaluator.setCond(null);
			if (evaluator.getStack() == null) {
				return null;
			} else {
				List<String> list = evaluator.getStack().get(evaluator.getStack().size() - 1);
				evaluator.getStack().remove(evaluator.getStack().size() - 1);
				return list.isEmpty() ? null : Data.join(list.toArray(new String[list.size()]), "\n");
			}
		} else if (source.trim().startsWith("/*include ")) {
			// Include
			int s = source.indexOf("/*");
			int e = source.indexOf("*/");
			String includePath = source.substring(s + 9, e).trim();
			try {
				return processFile(new String(Files.readAllBytes(Paths.get(context.getRealPath(includePath)))), browser, context, includePath);
			} catch (IOException ex) {
				Log.logException(ex, null); 
				return "/* " + source + " */"; 
			}
		} else if (inValidBlock) {
			// If in block
			if (evaluator.getCond().satisfiesCondition(browser)) {
				evaluator.getStack().get(evaluator.getStack().size() - 1).add(source);
			}
			return null;
		} else if (inBlock) {
			return null;
		} else { 
			return source;
		}
	}
}
