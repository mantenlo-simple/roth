package com.roth.servlet.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies that an ActionServlet can only be accessed if the user has
 * one or more of the roles specified.  Otherwise a 401 or 403 will result.<br>
 * <b>roles</b> (required) defines an array of role names.
 * @author jpayne
 *
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface ActionServletSecurity {
	String[] roles();
}
