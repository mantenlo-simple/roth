package com.roth.portal.util;

import java.io.File;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Paths;
import java.util.Scanner;

import com.roth.base.log.Log;
import com.roth.base.util.Data;
import com.roth.tags.html.Portlet;

public class StaticTheme implements Serializable {
	private static final long serialVersionUID = 8976450244967941351L;

	public static String getTheme(String servletPath, String hostname) {
		String theme = null;
		String dd = System.getProperty("os.name").toUpperCase().contains("WINDOWS") ? "\\" : "/";
		String filename = trimServletPath(servletPath, dd) + dd + "conf.custom" + dd + "default.thm";
		if (Files.exists(Paths.get(filename), LinkOption.NOFOLLOW_LINKS))
			theme = readExternalFile(filename);
		if (theme != null) {
			String override = getOverride(theme, hostname, filename);
			if (override != null)
				theme = override;
		}
		else
			theme = Data.readTextFile(Portlet.class, "/com/roth/tags/html/resource/default.thm");
		return theme;
	}
	
	protected static String trimServletPath(String servletPath, String dd) {
		String result = servletPath.substring(0, servletPath.lastIndexOf(dd, servletPath.length() - 2));
		return result.substring(0, result.lastIndexOf(dd, result.length() - 2));
	}
	
	public static String readExternalFile(String filename) {
		try (Scanner s = new Scanner(new File(filename))) {
			return s.useDelimiter("\\Z").next();
		}
		catch (Exception e) {
			Log.logException(e, null);
			return null;
		}
	}
	
	protected static String getOverride(String theme, String hostname, String filename) {
		String[] overrides = theme.substring(theme.lastIndexOf("{BREAK}") + 6).trim().replaceAll("\r\n", "\n").split("\n");
		for (String o : overrides) {
			String[] x = o.split("=");
			if (x[0].equalsIgnoreCase(hostname) && x.length > 1)
				return readExternalFile(filename.replace("default.thm", x[1].split("\\|")[0].toLowerCase() + ".thm"));
		}
		return null;
	}
}
