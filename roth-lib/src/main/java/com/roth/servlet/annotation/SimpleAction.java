package com.roth.servlet.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Provides a quick way to:<br>
 * 1 - forward directly to a JSP<br>
 * 2 - forward to another action<br>
 * 3 - instantiate a request-scoped bean, post to it, and then call an action within it<br>
 * Option 3 requires that the sub-action name exists in the bean class, with proper @Action annotation.
 * 
 * (refer to @Forward documentation for more information) 
 * @param action (optional) the action to forward to
 * @param name (required) the name of the action
 * @param path (optional) the path to a JSP to forward to
 * @param beanClass (optional) the bean class to instantiate, if calling a bean action
 * @author jpayne
 * @see {@link @Forward}
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface SimpleAction {
	String action() default "";
	String name();
	String path() default "";
	String pathMobi() default "";
	Class<?> beanClass() default Object.class;
}