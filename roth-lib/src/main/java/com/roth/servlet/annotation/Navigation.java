package com.roth.servlet.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Provides navigation information for an ActionServlet.  "contextPath"
 * (optional) defines the path that the action processor of an ActionServlet
 * will expect to find when processing the "path" of a @Forward or @SimpleAction
 * annotation.  "simpleActions" (optional) defines an array of @SimpleAction
 * annotations.
 * @author jpayne
 * @see {@link @SimpleAction}
 * @see {@link @Forward}
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface Navigation {
	String contextPath() default "";
	SimpleAction[] simpleActions() default {};
}