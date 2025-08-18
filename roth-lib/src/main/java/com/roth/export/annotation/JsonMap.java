package com.roth.export.annotation;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Use this annotation to provide information for JdbcUtil to convert from JSON to a map of values or objects. 
 */
@Retention(RUNTIME)
@Target(METHOD)
public @interface JsonMap {
	Class<? extends Object> keyClass();
	Class<? extends Object> valueClass();
}
