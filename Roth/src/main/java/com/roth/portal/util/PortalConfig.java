package com.roth.portal.util;

import java.io.Serializable;

import com.roth.portal.model.BookBean;
import com.roth.portal.model.DesktopBean;
import com.roth.portal.model.PageBean;
import com.roth.portal.model.PortletBean;
import com.roth.portal.model.RoleBean;

public class PortalConfig implements Serializable {
	private static final long serialVersionUID = 9125095860836935616L;
	
	private RoleBean[] roles;
	private PortletBean[] portlets;
	private BookBean[] books;
	private PageBean[] pages;
	private String[] themes;
	private DesktopBean[] desktops;
	
	public RoleBean[] getRoles() { return roles; }
	public void setRoles(RoleBean[] roles) { this.roles = roles; }
	
	public PortletBean[] getPortlets() { return portlets; }
	public void setPortlets(PortletBean[] portlets) { this.portlets = portlets; }
	
	public BookBean[] getBooks() { return books; }
	public void setBooks(BookBean[] books) { this.books = books; }
	
	public PageBean[] getPages() { return pages; }
	public void setPages(PageBean[] pages) { this.pages = pages; }
	
	public String[] getThemes() { return themes; }
	public void setThemes(String[] themes) { this.themes = themes; }
	
	public DesktopBean[] getDesktops() { return desktops; }
	public void setDesktops(DesktopBean[] desktops) { this.desktops = desktops; }
}
