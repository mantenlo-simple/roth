package com.roth.servlet.util;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.roth.base.util.Data;

public class GenericType implements Serializable {
	private static final long serialVersionUID = -3423826302651061284L;

	private String name;
	private GenericType[] params;
	
	/**
	 * Constructor using generic type name string.<br/><br/>
	 * Examples:<br/><br/>
	 * <code>
	 * java.lang.String<br/>
	 * com.sample.MyObject<br/>
	 * java.util.ArrayList&lt;java.lang.String&gt;<br/>
	 * java.util.ArrayList&lt;com.sample.MyObject&gt;<br/>
	 * java.util.LinkedHashMap&lt;java.lang.String, com.sample.MyObject&gt;<br/>
	 * java.util.LinkedHashMap&lt;java.lang.String, java.util.ListArray&lt;com.sample.MyObject&gt;&gt;<br/>
	 * </code>
	 * @param type
	 */
	private GenericType(String type) {
		constructor(type);
	}
	
	/**
	 * Constructor using method's return type (i.e. a getter method).
	 * @param method
	 */
	public GenericType(Method method) {
		constructor(method.getGenericReturnType().getTypeName());
	}
	
	private void constructor(String type) {
		int s = type.indexOf("<");
		name = s < 0 ? type : type.substring(0, s);
		params = new GenericType[2];
		
		String param1 = s < 0 ? null : type.substring(s + 1, type.lastIndexOf(">"));
		String param2 = null;
		if (param1 != null && (s = param1.indexOf(",")) > -1) {
			param2 = param1.substring(s + 1).trim();
			param1 = param1.substring(0, s);
		}
		
		if (param1 != null)
			params[0] = new GenericType(param1);
		if (param2 != null)
			params[1] = new GenericType(param2);
	}

	public String getName() { return name; }
	public void setName(String name) { this.name = name; }

	public GenericType[] getParams() { return params; }
	public void setParams(GenericType[] params) { this.params = params; }
	
	/**
	 * Initialize the type object.  This could be an atomic type or an array, collection, or map.
	 * @return
	 * @throws ClassNotFoundException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws SecurityException 
	 * @throws NoSuchMethodException 
	 * @throws InvocationTargetException 
	 * @throws IllegalArgumentException 
	 * @throws NegativeArraySizeException 
	 */
	public Object initType(String index) throws ClassNotFoundException, InstantiationException, IllegalAccessException, NegativeArraySizeException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		Class<?> c = Class.forName(name);
		Object o = c.isArray() ? Array.newInstance(Class.forName(name.replaceAll("\\[\\]", "")), Data.strToInteger(index)) : c.getConstructor().newInstance();
		if (c.isArray()) {
			
		}
		
		return o;
		// If there's an index, then that suggests that this is an Array, Collection, or Map.
		// Do something with it.
		
		// Also, a parameterized class can also be something else.  Figure out how to deal with it.
		// OK.  I think with a non-collection/map scenario, the parameter IS the one to instantiate.
		
	}
}
