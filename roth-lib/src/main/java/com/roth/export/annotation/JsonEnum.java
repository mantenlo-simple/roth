package com.roth.export.annotation;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Use this annotation to provide information for JdbcUtil to convert from JSON to an Enum when the incoming value may be different from the name. 
 */
@Retention(RUNTIME)
@Target(TYPE)
public @interface JsonEnum {
	String valueMethod();
}
