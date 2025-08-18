package com.roth.export.annotation;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Use two or more of these annotations to describe extension classes to 
 * instantiate when given values are present.  The getter for the specified
 * field is used to check for the values.<br/>
 * Example:<br/>
 * <code>
 * &commat;JsonExtension(extensionClass = BExtendsA.class, field = "type", values = { "one", "three", "eight" })<br/>
 * &commat;JsonExtension(extensionClass = CExtendsA.class, field = "type", values = { "two", "four", "five", "six", "seven" })<br/>
 * &commat;JsonExtension(extensionClass = DExtendsA.class, field = "type", values = { })<br/>
 * public abstract class A { ...<br/>
 * </code>
 * In this example, when class A is encountered by JsonUtil, the "type" field will be checked in the JSON map,
 * and if the value is one of "one", "three", or "eight", then BExtendsA will be instantiated.  If the value is
 * one of "two", "four", "five", "six", or "seven", then CExtendsA will be instantiated.  Otherwise all other
 * values will result in DExtendsA being instantiated (an empty values array is considered as 'all other values'). 
 */
@Repeatable(JsonExtensions.class)
@Retention(RUNTIME)
@Target(TYPE)
public @interface JsonExtension {
	Class<?> extensionClass();
	String field();
	String[] values() default {};
}
