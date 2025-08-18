package com.roth.tags.html;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.JspTagException;
import jakarta.servlet.jsp.tagext.BodyTagSupport;

import com.roth.base.log.Log;
import com.roth.base.util.Data;
import com.roth.servlet.CssServlet;
import com.roth.servlet.util.Browser;

public class Style extends BodyTagSupport {
	private static final long serialVersionUID = -8697706475277404113L;

	public void setCompressed(boolean compressed) { setValue("compressed", compressed); }
	public void setFilename(String filename) { setValue("filename", filename); }
	
	@Override
	public int doStartTag() throws JspException {
		String filename = (String)getValue("filename");
		return filename == null ? EVAL_BODY_BUFFERED : SKIP_BODY;
	}
	
	@Override
	public int doEndTag() throws JspException {
		HttpServletRequest request = (HttpServletRequest)pageContext.getRequest();
		Browser browser = new Browser(request.getHeader("User-Agent"));
		ServletContext context = request.getSession().getServletContext();
		String filename = (String)getValue("filename");
		String realPath = realPath(filename);
		boolean compressed = Data.nvl((Boolean)getValue("compressed"), true);
		String source = null;
		String error = null;
		String cache = null;
		if (realPath != null) {
			cache = CssServlet.getCache(realPath);
			if (cache == null) {
				try {
					source = new String(Files.readAllBytes(Paths.get(realPath)));
				} catch (IOException e) {
					Log.logException(e, null);
					error = "The specified CSS file or files could not be found.";
				}
			}
		} else
			source = getBodyContent().getString();
		if (source != null) {
			String output = CssServlet.compile(CssServlet.processFile(source, browser, context, realPath), compressed);
			println("<style type=\"text/css\">\n" + output + "\n</style>");
			if (realPath != null)
				CssServlet.putCache(realPath, output);
		} else if (cache != null) {
			println("<style type=\"text/css\">\n" + cache + "\n</style>");
		} else
			println("<div style=\"font-weight: bold; color: red;\">" + Data.nvl(error, "The style content is not valid.") + "</div>");
		release();
		return super.doEndTag();
	}
	
	protected String realPath(String filename) {
		HttpServletRequest request = (HttpServletRequest)pageContext.getRequest();
		ServletContext context = request.getSession().getServletContext();
		if (filename == null)
			return null;
		if (filename.charAt(0) == '/')
			return context.getRealPath(filename);
		String uri = request.getRequestURI();
		return context.getRealPath(uri.substring(context.getContextPath().length(), uri.lastIndexOf("/") + 1) + filename);
	}
	
	// Output Functions
	protected void print(String output) throws JspTagException {
		try { pageContext.getOut().print(output); }
        catch (IOException e) { throw new JspTagException(e.getMessage()); }
	}
	
	protected void println(String output) throws JspTagException {
		try { pageContext.getOut().println(output); }
        catch (IOException e) { throw new JspTagException(e.getMessage()); }
	}
}
