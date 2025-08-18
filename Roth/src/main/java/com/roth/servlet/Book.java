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

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import jakarta.servlet.annotation.WebServlet;

import com.roth.base.log.Log;
import com.roth.base.util.Data;
import com.roth.jdbc.util.TableUtil;
import com.roth.portal.db.PortalUtil;
import com.roth.portal.model.BookBean;
import com.roth.portal.model.PageBean;
import com.roth.servlet.annotation.ActionServletSecurity;
import com.roth.servlet.annotation.Navigation;
import com.roth.servlet.annotation.SimpleAction;

@WebServlet(urlPatterns = "/Book/*")
@ActionServletSecurity(roles = "PortalAdmin")
@Navigation(contextPath = "/configuration",
	        simpleActions = { @SimpleAction(name = "begin", action = "load") })
public class Book extends ActionServlet {
	private static final long serialVersionUID = 1L;

	@Action(forwards = { @Forward(name = "success", path = "/configuration/index.jsp"),
                         @Forward(name = "ajax", path = "_list.jsp") },
            responses = { @Response(name = "failure", httpStatusCode = 500) })
    public String load(ActionConnection conn) {
		conn.getRequest().setAttribute("tabpage", "book");
		try { 
			ArrayList<BookBean> books = new PortalUtil().getList(BookBean.class, null, "get_book_sort(book_id)", null);
			for (int i = 1; i < books.size(); i++)
			    if (books.get(i).getLevel() > 0)
			    	for (int j = 0; j < i; j++)
			    		if (books.get(j).getLevel() == 0)
			    			books.get(j).addChild(books.get(i));
			
			conn.getRequest().setAttribute("books", books); 
		} 
		catch (Exception e) { 
			Log.logException(e, conn.getUserName());
			conn.println("An error occurred while gathering data:<br/>");
			conn.println(e.getMessage());
			return "failure";
		}
		//return method.equalsIgnoreCase("ajax") ? "ajax" : "success";
		return isCallingActionName(BEGIN, conn) ? SUCCESS : "ajax";
	}
	
	@Action(forwards = { @Forward(name = "success", path = "_edit.jsp") })
	public String edit(ActionConnection conn) {
		BookBean bean = new BookBean();
		Long bookId = Data.strToLong(conn.getRequest().getParameter("bookId"));
		String bookName = conn.getParameter("bookName");
		String params = conn.getParameter("_params");
		Long parentBookId = (params == null) ? null : Data.strToLong(Data.parseEncodedParam(params, "bookId"));
		if (parentBookId != null) bean.setParentBookId(parentBookId);
		try {
			PortalUtil util = new PortalUtil();
			if (bookId != null)
				bean = new TableUtil("roth").get(BookBean.class, util.applyParameters("book_id = {1}", bookId));
			else if (bookName != null)
				bean = new TableUtil("roth").get(BookBean.class, util.applyParameters("book_name = {1}", bookName));
			String filter = (bean.getBookId() == null) 
				      ? null 
				      : "book_id != " + bean.getBookId() + " AND " +
				        "lineage NOT LIKE CONCAT('% ', " + bean.getBookId() + ", ' %') ";
			conn.getRequest().setAttribute("books", util.getList(BookBean.class, filter, "get_book_sort(book_id)", null));
		}
		catch (SQLException e) { e.printStackTrace(); }
		putBean(bean, "book", "request", conn);
		return "success";
	}
	
	@Action(forwards = { @Forward(name = "success", action = "load") })
	@Post(beans = { @Bean(name = "book", scope = "request", beanClass = BookBean.class) })
	public String save(ActionConnection conn) {
		BookBean book = getBean(0, conn);
		book.setUpdatedBy(getUserName(conn));
		try { new PortalUtil().saveBook(book); }
		catch (Exception e) { e.printStackTrace(); }
		return "success";
	}
	
	@Action(forwards = { @Forward(name = "success", action = "load"),
                         @Forward(name = "return", path = "/configuration/index.jsp") })
	public String delete(ActionConnection conn) {
		BookBean book = new BookBean();
		book.setBookId(Data.strToLong(conn.getRequest().getParameter("bookId")));
		try { new PortalUtil().delete(book); }
		catch (SQLException e) { e.printStackTrace(); }
		return "success";
	}
	
	@Action(forwards = { @Forward(name = "success", path = "__contents.jsp") })
	public String getContents(ActionConnection conn) {
		Long bookId = Data.strToLong(conn.getRequest().getParameter("bookId"));
		try {
			if (bookId != null)
				conn.getRequest().setAttribute("bookContents", new PortalUtil().getBookContents(bookId));
		}
		catch (SQLException e) { e.printStackTrace(); }
		return "success";
	}
	
	@Action(forwards = { @Forward(name = "success", path = "__contentedit.jsp") })
	public String editPage(ActionConnection conn) {
		Long bookId = Data.strToLong(conn.getRequest().getParameter("bookId"));
		Long portletId = Data.strToLong(conn.getRequest().getParameter("tableId"));
		try {
			PageBean page = new PageBean();
			page.setBookId(bookId);
			page.setPortletId(portletId);

			if (portletId == null) {
				LinkedHashMap<String,String> portlets = new LinkedHashMap<String,String>(); 
				portlets.put("", "");
				portlets.putAll(new PortalUtil().getPortletsNotInBook(bookId));
				conn.getRequest().setAttribute("portlets", portlets);
			}
			else
				page = new PortalUtil().get(PageBean.class, "book_id = {bookId} AND portlet_id = {portletId}", page); 
				
			conn.getRequest().setAttribute("page", page);
		}
		catch (SQLException e) { e.printStackTrace(); }
		return "success";
	}
	
	@Action(forwards = { @Forward(name = "success", action = "getContents") })
	@Post(beans = { @Bean(name = "page", scope = "request", beanClass = PageBean.class) })
	public String savePage(ActionConnection conn) {
		PageBean page = getBean(0, conn);
		page.setUpdatedBy(getUserName(conn));
		try { new PortalUtil().save(page); }
		catch (SQLException e) { e.printStackTrace(); }
		return "success";
	}
	
	@Action(forwards = { @Forward(name = "success", action = "getContents") })
	public String deletePage(ActionConnection conn) {
		PageBean page = new PageBean();
		Long bookId = Data.strToLong(conn.getRequest().getParameter("bookId"));
		Long portletId = Data.strToLong(conn.getRequest().getParameter("tableId"));
		page.setBookId(bookId);
		page.setPortletId(portletId);
		try { new PortalUtil().deletePage(page); }
		catch (SQLException e) { e.printStackTrace(); }
		return "success";
	}
	
	@Action(forwards = { @Forward(name = "success", action = "getContents") })
	public String movePage(ActionConnection conn) {
		Long bookId = Data.strToLong(conn.getRequest().getParameter("bookId"));
		Long sequence = Data.strToLong(conn.getRequest().getParameter("sequence"));
		String direction = conn.getRequest().getParameter("direction");
		int dir = (direction.equalsIgnoreCase("up")) ? -1 : 1;
		try { new PortalUtil().movePage(bookId, sequence, dir); }
		catch (SQLException e) { e.printStackTrace(); }
		return "success";
	}
}
