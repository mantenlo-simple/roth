package com.roth.jdbc.annotation;

import static java.lang.annotation.ElementType.METHOD;

import java.lang.annotation.Target;

@Target(METHOD)
public @interface StringValidate {
	/**
	 * The maximum number of characters allowed in a non-null string value.  Use 0 for no limit.
	 * @return
	 */
	
	int maxLength() default 0;
	/**
	 * The minimum number of characters to require in a non-null string value.  Use 0 for no minimum.
	 * @return
	 */
	int minLength() default 0;
	
	/**
	 * Whether the value can be null.
	 * @return
	 */
	boolean notNull() default false;
	
	/**
	 * 
	 * @return
	 */
	boolean throwsExcepiton() default false;
}
