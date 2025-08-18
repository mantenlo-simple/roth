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
package com.roth.base.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.naming.CannotProceedException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.tika.Tika;
import org.apache.tomcat.util.http.fileupload.IOUtils;

import com.google.common.base.Charsets;
import com.roth.base.log.Log;
import com.roth.jdbc.util.DbmsType;
import com.roth.tags.el.Util;

import jakarta.servlet.ServletContext;

public final class Data {
	public static final String ISO_DATE = "yyyy-MM-dd";
	
	public static final String ISO_DATETIME = "yyyy-MM-dd HH:mm:ss";
	public static final String ISO_DATETIME_MIN = "yyyy-MM-dd HH:mm";
	public static final String ISO_DATETIME_ZONE = "yyyy-MM-dd HH:mm:ss z";
	public static final String ISO_DATETIME_OFFSET = "yyyy-MM-dd HH:mm:ss Z";
	
	public static final String ISO_TIME = "HH:mm:ss";
	public static final String ISO_TIME_MINS = "HH:mm";
	public static final String ISO_TIME_ZONE = "HH:mm:ss z";
	public static final String ISO_TIME_OFFSET = "HH:mm:ss Z";
	
	public static final String ISO_TEMPLATE = "2000-01-01 00:00:00";
	
	public static final String FMT_DATE = "date";
	public static final String FMT_TIME = "time";
	public static final String FMT_DATETIME = "datetime";
	
	public static final String JSON_DATETIME = "yyyy-MM-dd'T'HH:mm:ss";
	
	public static final String ROTH_TEMP = "rothTemp";
	
	private Data() {}
	
	/**
	 * Create a new instance of the specified class.<br/>
	 * Use this method if the class has a constructor with no parameters.
	 * @param clazz
	 * @return
	 * @throws IllegalArgumentException
	 * @throws SecurityException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws NoSuchMethodException
	 */
	public static <T> T newInstance(Class<T> clazz) throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		try {
			return newInstance(clazz, new Class[0], new Object[0]);
		}
        catch (Exception e) {
        	Log.logDebug(String.format("Instantiation failed for %s; the class does not contain a zero parameter constructor.", clazz.getCanonicalName()), null);
        	throw e;
        }
    }
    
    /**
     * Create a new instance of the specified class, given the specified constructor parameters.<br/>
     * Use this method if the class has a constructor that takes parameters.
     * @param clazz
     * @param paramClazzes The parameter types in the order that they appear.
     * @param params The parameter values in corresponding order of the parameter types.
     * @return
     * @throws IllegalArgumentException
     * @throws SecurityException
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws NoSuchMethodException
     */
	public static <T> T newInstance(Class<T> clazz, Class<?>[] paramClazzes, Object[] params) throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		return clazz.getConstructor(paramClazzes).newInstance(params);
	}
	
	/**
	 * Get the named declared method from the class.  If it doesn't exist, get the named non-bridge inherited method.
	 * @param object
	 * @param name
	 * @return
	 * @throws NoSuchMethodException
	 */
	public static Method getDeclaredMethod(Class<?> clazz, String name) throws NoSuchMethodException {
		Method[] methods = clazz.getDeclaredMethods();
		
		for (int i = 0; i < methods.length; i++)
			if (methods[i].getName().equalsIgnoreCase(name))
				return methods[i];
		
		// If not found in the declared methods, look for any inherited methods.
		methods = clazz.getMethods();
		
		for (int i = 0; i < methods.length; i++)
			if (methods[i].getName().equalsIgnoreCase(name) && !methods[i].isBridge())
				return methods[i];
		
		throw new NoSuchMethodException();
	}
	
	public static int getModifiers(Executable source) {
		return source instanceof Constructor constructor ? constructor.getModifiers() 
			 : source instanceof Method method ? method.getModifiers()
			 : 0;
	}
	public static boolean isPublic(Executable source) { return (getModifiers(source) & Modifier.PUBLIC) == Modifier.PUBLIC; }
	public static boolean isProtected(Executable source) { return (getModifiers(source) & Modifier.PROTECTED) == Modifier.PROTECTED; }
	public static boolean isPrivate(Executable source) { return (getModifiers(source) & Modifier.PRIVATE) == Modifier.PRIVATE; }
	public static boolean isStatic(Executable source) { return (getModifiers(source) & Modifier.STATIC) == Modifier.STATIC; }
	public static boolean isSyncrhonized(Executable source) { return (getModifiers(source) & Modifier.SYNCHRONIZED) == Modifier.SYNCHRONIZED; }
	public static boolean isAbstract(Executable source) { return (getModifiers(source) & Modifier.ABSTRACT) == Modifier.ABSTRACT; }

	
	/**
	 * Get an env-entry value defined in a context or application web.xml. 
	 * @param name
	 * @return
	 */
	public static String getContextEnv(String name) {
		try {
			Context env = (Context)new InitialContext().lookup("java:comp/env");
			return (String)env.lookup(name);
		}
		catch (Exception e) {
			return null;
		}
	}

	/**
	 * Make directory if it doesn't already exist, including any necessary parent directories.
	 * @param path
	 */
	public static void mkdir(String path) {
		File dir = new File(path);
		if (!dir.exists())
			dir.mkdirs();
	}

	/**
     * Generate a UUID and FileOutputStrewam for a UUID file at the specified file path.
	 */
	public static UuidOutput getUuidFileStream(String filepath) throws FileNotFoundException {
		String uuid = UUID.randomUUID().toString();
		String path = String.format("%s/%s", filepath.replaceAll("/$", ""), uuid);
		return new UuidOutput(uuid, new FileOutputStream(new File(path)));
	}
	
	/**
	 * Write a file from input stream with a generated UUID as the filename.  Returns the generated UUID.
	 * @param filepath
	 * @param inputStream
	 * @return
	 * @throws IOException
	 */
	public static String writeUuidFile(String filepath, InputStream inputStream) throws IOException {
		String uuid = UUID.randomUUID().toString();
		writeFile(String.format("%s/%s", filepath.replaceAll("/$", ""), uuid), inputStream);
		return uuid;
	}
	
	/**
	 * Write a file from input stream.
	 * @param filepath
	 * @param inputStream
	 * @throws IOException
	 */
	public static void writeFile(String filepath, InputStream inputStream) throws IOException {
		mkdir(filepath.substring(0, filepath.lastIndexOf('/')));
		File targetFile = new File(filepath);
	    try (OutputStream outputStream = new FileOutputStream(targetFile)) {
		    byte[] buffer = new byte[8 * 1024];
		    int bytesRead;
		    while ((bytesRead = inputStream.read(buffer)) != -1)
		    	outputStream.write(buffer, 0, bytesRead);
		    IOUtils.closeQuietly(outputStream);
	    }
	}
	
	/**
	 * Writes a file to the temp folder defined by the ROTH_TEMP env-entry value (or "/temp" if rothTemp is not set) (see getContextEnv function).
	 * Note: this function does not close the input stream. 
	 * @param filename
	 * @param inputStream
	 * @throws IOException
	 */
	public static void writeTempFile(String filename, InputStream inputStream) throws IOException {
		String tempFolder = nvl(getContextEnv(ROTH_TEMP), "/temp");
		if (!tempFolder.endsWith("/"))
			tempFolder += "/";
		writeFile(tempFolder + filename, inputStream);
	}
	
	/**
	 * Read a file to output stream.
	 * @param filepath
	 * @param outputStream
	 * @throws IOException
	 */
	public static void readFile(String filepath, OutputStream outputStream) throws IOException {
		if (Files.notExists(Paths.get(filepath), LinkOption.NOFOLLOW_LINKS))
			return;
		byte[] buffer = new byte[8 * 1024];
	    int bytesRead;
	    try (InputStream inputStream = new FileInputStream(new File(filepath))) {
		    while ((bytesRead = inputStream.read(buffer)) != -1)
		    	outputStream.write(buffer, 0, bytesRead);
		    IOUtils.closeQuietly(inputStream);
	    }
	}
	
	/**
	 * Reads a file from the temp folder defined by the ROTH_TEMP env-entry value (or "/temp" if rothTemp is not set) (see getContextEnv function).
	 * Note: this function does not close the output stream.
	 * @param filename
	 * @param outputStream 
	 * @throws IOException
	 */
	public static void readTempFile(String filename, OutputStream outputStream) throws IOException {
		String tempFolder = nvl(getContextEnv(ROTH_TEMP), "/temp");
		String filepath = tempFolder + filename;
		readFile(filepath, outputStream);
	}
	
	/**
	 * Determines whether the value is empty.  The source parameter is considered empty if
	 * it is null, or if one of the following conditions apply:<br/>
	 * - If source is an array or CharSequence, it is considered empty if its length = 0.<br/>
	 * - If source is a string, collection (List, Set, etc.), map, or Optional, the result of that object's isEmpty 
	 * method is returned.<br/>
	 * @param source
	 * @return True if empty, false if not.
	 */
	public static boolean isEmpty(Object source) {
        return (source == null) || 
               ((source instanceof CharSequence cSource) && cSource.length() == 0) ||
               ((source instanceof String cSource) && cSource.isEmpty()) ||
               ((source instanceof Collection cSource) && cSource.isEmpty()) ||
               ((source instanceof Map cSource) && cSource.isEmpty()) ||
               ((source instanceof Object[] cSource) && cSource.length == 0) ||
               ((source instanceof Optional cSource) && cSource.isEmpty());
    }
	
	/**
	 * Enforces that a String is not empty.
	 * @param source
	 * @param value
	 * @return source if not empty, value otherwise.
	 */
	public static String evl(String source, String ... value) {
		if (value == null)
			throw new IllegalArgumentException("The value parameter cannot be null.");
		String result = source;
		int i = 0;
		while(isEmpty(result) && i < value.length)
			result = value[i++];
		if (isEmpty(result))
			throw new IllegalArgumentException("The final value parameter cannot be null or empty.");
		return result;
	}
	
	/**
	 * Enforces a string that is not null.
	 * @param source
	 * @return source if not null, an empty string otherwise.
	 */
	public static String nvl(String source) {
		return nvl(source, "");
	}
	
	/**
	 * Enforces an object that is not null.  Note: all potential value parameters MUST be the same type (if not null).
	 * @param <T>
	 * @param source
	 * @param value
	 * @return
	 */
	@SafeVarargs
	public static <T> T nvl(T source, T ... value) {
		if (value == null)
			throw new IllegalArgumentException("The value parameter cannot be null.");
		String c = null;
		for (T v : value) {
			if (c == null && v != null)
				c = v.getClass().getCanonicalName();
			if (c != null && !v.getClass().getCanonicalName().equals(c))
				throw new IllegalArgumentException("All value parameters must be the same type.");
		}
		T result = source;
		int i = 0;
		while(result == null && i < value.length)
			result = value[i++];
		if (result == null)
			throw new IllegalArgumentException("The final value parameter cannot be null.");
		return result;
	}
	
	/**
	 * Enforces an object that is either not empty or null.
	 * @param source
	 * @return source if not empty, otherwise null.
	 */
    public static <T> T envl(T source) {
        return isEmpty(source) ? null : source;
    }

    /**
     * If source starts with the string specified in end, then source is returned,
     * otherwise start concatenated with source is returned. 
     * @param source
     * @param start
     * @return
     */
    public static String enforceStart(String source, String start) {
    	return source == null ? null : source.startsWith(start) ? source : start + source;
    }
    
    /**
     * If source ends with the string specified in end, then source is returned,
     * otherwise source concatenated with end is returned. 
     * @param source
     * @param end
     * @return
     */
    public static String enforceEnd(String source, String end) {
    	return source == null ? null : source.endsWith(end) ? source : source + end;
    }
    
    /**
     * Performs a null-safe trim.
     * @param source
     * @return
     */
    public static String trim(String source) {
    	return source == null ? null : source.trim();
    }
    
    /**
	 * Compares two objects of the same type.  True is returned if both
	 * values are null, or both values are equal.  Otherwise, false is returned.
	 * @param <T>
	 * @param a
	 * @param b
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> boolean nullSafeMatch(Comparable<T> a, Comparable<T> b) {
		if (a == null && b == null)
			return true;
		if ((a == null && b != null) || (a != null && b == null))
			return false;
		return a.compareTo((T)b) == 0;
	}
	
	/**
	 * Compares two objects of the same type.  True is returned if both
	 * values are null, or both values are equal.  Otherwise, false is returned.
	 * @param <T>
	 * @param a
	 * @param b
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> boolean nullSafeMatch(Comparable<T>[] a, Comparable<T>[] b) {
		if (a == null && b == null)
			return true;
		if ((a == null && b != null) || (a != null && b == null))
			return false;
		if (a.length != b.length)
			return false;
		for (int i = 0; i < a.length; i++)
			if (a[i].compareTo((T)b[i]) != 0)
				return false;
		return true;
	}
    
    public static final int PAD_LEFT = -1;
    public static final int PAD_RIGHT = 1;
    public enum Pad {
    	LEFT,
    	RIGHT
    }
    
    /**
     * Performs a null-safe pad.  If the source is null, the result is a string 
     * entirely consisting of the padding character.  Local constants PAD_LEFT (-1)
     * and PAD_RIGHT (1) are supplied for more readable code.
     * @param source
     * @param padding
     * @param length The final length of the return value, if source is shorter.
     * @param side The side of the string to pad.
     * @return
     */
    public static String pad(String source, char padding, int length, Pad side) {
    	StringBuilder result = new StringBuilder(source == null ? "" : source);
    	while (result.length() < length)
    		if (side == Pad.LEFT)
    			result.insert(0, padding);
    		else
    			result.append(padding);
    	return result.toString();
    }
    
	/**
	 * Checks to see if the source object is found in the array. 
	 * @param value
	 * @param array
	 * @return
	 */
	public static <T> boolean in(T value, T[] array) {
		return value != null && array != null && Arrays.stream(array).anyMatch(v -> v.equals(value));
	}
	
	/**
	 * Checks to see if the source object is found in the array. 
	 * @param value
	 * @param array
	 * @return
	 */
	public static <T> boolean in(T value, Collection<T> collection) {
		return value != null && collection != null && collection.stream().anyMatch(v -> v.equals(value));
	}

	/**
	 * Checks to see if the source object is found in the map. 
	 * @param value
	 * @param map
	 * @return
	 */
	public static <T> boolean in(T value, Map<?,T> map) {
		return value != null && map != null && map.entrySet().stream().anyMatch(v -> v.getValue().equals(value)); 
	}
	
	/**
	 * Checks to see if the source object is found in the array. 
	 * @param value
	 * @param array
	 * @return
	 */
	public static boolean inIgnoreCase(String value, String[] array) {
		return value != null && array != null && Arrays.stream(array).anyMatch(v -> v.equalsIgnoreCase(value));
	}
	
	/**
	 * Gets the index of the value in the given array.
	 * @param <T>
	 * @param value
	 * @param array
	 * @return the values index if present, otherwise -1.
	 */
	public static <T> int indexOf(T value, T[] array) {
		return value == null || array == null ? -1 : IntStream.range(0, array.length).filter(i -> array[i].equals(value)).findFirst().orElse(-1);
	}
	
	/**
	 * Gets the index of the value in the given Collection.
	 * @param <T>
	 * @param value
	 * @param collection
	 * @return the values index if present, otherwise -1.
	 */
	public static <T> int indexOf(T value, Collection<T> collection) {
		return indexOf(value, collection.toArray());
	}
	
	/**
	 * Splits a string at line feeds (LF).  Carriage Returns (CR) and CRLF are replaced by LF before splitting. 
	 * @param source
	 * @return an array of Strings 
	 */
	public static String[] splitLF(String source) {
    	return (source == null) ? null  : nvl(source).trim().replace("\r\n", "\n").replace("\r", "\n").split("\n");
    }
    
	/**
	 * Gets the string from array at index.  This is index-safe, meaning that it will not
	 * throw an index out of bounds exception. 
	 * @param array
	 * @param index
	 * @return null if the array is null or if the index is out of bounds, otherwise the indexed item in the array
	 */
    public static String get(String[] array, int index) {
    	return ((array == null) || (index < 0) || (array.length <= index)) ? null : envl(array[index].trim());
    }
    
    /**
	 * Determines whether char c is an alpha character.
	 * @param c
	 * @return true if c is an alpha character, false otherwise
	 */
	public static boolean isAlpha(char c) {
		return "%s".formatted(c).matches("[A-Za-z]");
		//return ((c >= 'a') && (c <= 'z')) ||
		//      ((c >= 'A') && (c <= 'Z'));
	}
	
	/**
	 * Determines whether the source string represents a numeric value
	 * @param source
	 * @return
	 */
	public static boolean isNumeric(String source) {
		return source == null ? false : source.matches("^(-|)[0-9]+(.[0-9]+|)$");
		/*
		try {
			@SuppressWarnings("unused")
			double x = Double.parseDouble(source);
			return true;
		}
		catch (Exception e) {
			return false;
		}
		*/
	}
	
	/**
	 * Gets a case-insensitive form of the source regex expression.<br>
	 * Example: getULRegEx("&lt;blue/&gt;") returns "&lt;[b|B][l|L][u|U][e|E]/&gt;".
	 * @param source
	 * @return the case-insensitive form of source
	 */
	public static String getULRegEx(String source) {
		StringBuilder result = new StringBuilder("");
		
		for (int i = 0; i < source.length(); i++) {
			if (isAlpha(source.charAt(i)))
			    result.append("[" + source.substring(i, i + 1).toLowerCase() + "|"
			                  + source.substring(i, i + 1).toUpperCase() + "]");
			else
				result.append(source.charAt(i));
		}
		
		return result.toString();
	}
	
	/**
	 * <b>upcaseFirst</b><br><br>
	 * Returns source with the first letter upper-cased.
	 * @param source - The string to upper-case the first letter of.
	 * @return the string with the first letter upper-cased.
	 */
	public static String upcaseFirst(String source) {
		return source.isEmpty() ? "" : source.substring(0, 1).toUpperCase() + source.substring(1);
	}
	
	/**
	 * Convert database column name to camel case.
	 * @param source
	 * @return
	 */
	public static String camelcase(String source) {
		StringBuilder result = new StringBuilder("");
		int last = 0;
		String seg;
		
		for (int i = 0; i < source.length(); i++)
			if (source.charAt(i) == '_') {
				seg = source.substring(last, i);
				result.append((result.length() == 0) ? seg : upcaseFirst(seg));
				last = i + 1;
			}
			else if (source.charAt(i) == '$') {
				seg = source.substring(last, i);
				result.append(((result.length() == 0) ? seg : upcaseFirst(seg)) + '$');
				last = i + 1;
			}
		
		seg = source.substring(last);
		result.append((result.length() == 0) ? seg : upcaseFirst(seg));
		return result.toString();
	}
	
	/**
	 * Get the getter or setter name for a given field name.  The field name can be either 
	 * camelcase (e.g. "firstName") or underscored (e.g. "first_name"), which will be 
	 * converted automatically to camelcase.
	 * @param prefix
	 * @param name
	 * @return
	 */
	public static String getEncapMethodName(String prefix, String fieldName) {
		return prefix + upcaseFirst(camelcase(fieldName));
	}
	
	public static String getGetterName(String fieldName) { return getEncapMethodName("get", fieldName); }
	public static String getSetterName(String fieldName) { return getEncapMethodName("set", fieldName); }
	
	/**
	 * Return the size of a List or Array.  If neither, then null is returned.
	 * @param obj
	 * @return
	 */
	public static Integer sizeOf(Object obj) {
		if (obj == null) return 0;
		return (obj instanceof List cObj)
		     ? cObj.size()
		     : (obj.getClass().isArray())
		     ? ((Object[])obj).length
		     : null;
	}
	
	/**
	 * Return the indexed item of a List or Array.  If neither, then return null.
	 * @param obj
	 * @param i
	 * @return
	 */
	public static Object itemOf(Object obj, int i) {
		if (obj == null || (i < 0) || (i > (sizeOf(obj) - 1))) return null;
		return (obj instanceof List list)
	     ? list.get(i)
	     : (obj.getClass().isArray())
	     ? ((Object[])obj)[i]
	     : null;
	}
	
	/**
	 * Return the named field value via its getter from a POJO.
	 * @param obj
	 * @param name
	 * @return
	 * @throws NoSuchMethodException
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 */
	public static Object fieldOf(Object obj, String name) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
		if (obj == null || name.startsWith("_na")) return null;
		String getterName = "get" + upcaseFirst(name);
		Method m = obj.getClass().getMethod(getterName);
		return m.invoke(obj);
	}
	
	/**
     * Escapes a string so that it is SQL-safe.  If like is true, then
     * "%" and "_" characters are escaped so that they will be treated
     * as literal characters.  Please note: in order for the escape to 
     * function properly in a LIKE expression, then the expression
     * should be written as follows: <b><code>column_name LIKE {2} ESCAPE '\'</code></b>
     * @param source The string to escape.
     * @param like Whether the string will be used in a LIKE expression.
     * @return The SQL-safe version of the string.
     */
    public static String sqlEscape(String source, boolean like) {
        if (source == null)
            return null;
        
        String result = source.replace("'", "''");
        
        if (like) // If it's a like, then escape "%" and "_". 
            result = result.replace("%", "\\\\%").replace("_", "\\\\_");
        
        return result;
    }
    
    public static String sqlEscape(DbmsType dbName, String source, boolean like) {
    	if (source == null)
    		return null;
    	String result = sqlEscape(source, like);
    	return dbName == DbmsType.INFORMIX 
    		 ? result.replace("\r", "' || chr(13) || '").replace("\n", "' || chr(10) || '") 
    		 : result;
    }
    
    /**
     * Escapes HTML (or XML) for safe display. 
     * @param source
     * @return
     */
    public static String htmlEscape(String source) {
        return (source == null) ? null
             : source.replace("&", "&amp;")
                     .replace("#", "&#35;")
                     .replace("<", "&lt;")
                     .replace(">", "&gt;")
                     .replace("\"", "&quot;")
                     .replace("{", "&#123;")
                     .replace("}", "&#125;")
                     .replace("$", "&#36;")
                     .replace("%", "&#37;");
    }

    /**
     * Un-escapes escaped HTML (or XML). 
     * @param source
     * @return
     */
    public static String htmlUnescape(String source) {
        return (source == null) ? null
             : source.replace("&amp;", "&")
                     .replace("&#35;", "#")
                     .replace("&lt;", "<")
                     .replace("&gt;", ">")
                     .replace("&quot;", "\"")
                     .replace("&#123;", "{")
                     .replace("&#125;", "}")
                     .replace("&#36;", "$")
                     .replace("&#37;", "%");
    }
    
    /**
     * Converts an Object to a String.  This is null-safe, meaning that if
     * the object is null, then null will be returned.  No special formatting
     * is performed by this method.
     * @param source
     * @return The String form of the object if not null, otherwise null.
     */
	public static String obj2Str(Object source) {
		return (source == null) ? null : source.toString();
	}
	
	/**
     * Converts an Object to a String.  This is null-safe, meaning that if
     * the object is null, then null will be returned.  No special formatting
     * is performed by this method.
     * @param source
     * @return The String form of the object if not null, otherwise null.
     */
	public static String obj2Str(Object source, Map<String,String> formats) {
		return obj2Str(source, formats, null, null, null);
	}
	
	/**
     * Converts an Object to a String.  This is null-safe, meaning that if
     * the object is null, then null will be returned.  No special formatting
     * is performed by this method.
     * @param source
     * @return The String form of the object if not null, otherwise null.
     */
	public static String obj2Str(Object source, Map<String,String> formats, String formatName, String pattern, Locale locale) {
		if (source == null)
			return null;
		if (source.getClass().isArray() || source instanceof Collection) {
			@SuppressWarnings("unchecked")
			Stream<Object> stream = source.getClass().isArray() ? Arrays.stream((Object[])source) : ((Collection<Object>)source).stream();
			return "[%s]".formatted(stream.map(o -> obj2Str(o, formats, formatName, pattern, locale)).collect(Collectors.joining(", ")));
		}
		if (source instanceof java.lang.Number && pattern != null) {
			DecimalFormat formatter = new DecimalFormat(pattern, new DecimalFormatSymbols(locale));
			return formatter.format(source);
		}
		return (source instanceof java.sql.Date cSource) ? dateToStr(cSource, formats.get(FMT_DATE))
		     : (source instanceof java.sql.Time cSource) ? dateToStr(cSource, formats.get(FMT_TIME))
		     : (source instanceof java.sql.Timestamp cSource) ? dateToStr(cSource, formats.get(formatName == null ? FMT_DATETIME : formatName))
		     : (source instanceof java.util.Date cSource) ? dateToStr(cSource, formats.get(formatName == null ? FMT_DATETIME : formatName))
		     : (source instanceof java.time.LocalDate cSource) ? dateToStr(cSource, formats.get(formatName == null ? FMT_DATE : formatName))
		     : (source instanceof java.time.LocalDateTime cSource) ? dateToStr(cSource, formats.get(formatName == null ? FMT_DATETIME : formatName))
		     : (source instanceof java.time.LocalTime cSource) ? dateToStr(cSource, formats.get(formatName == null ? FMT_TIME : formatName))
		     : source.toString();
	}

	/**
	 * Converts an object to a SQL-friendly string.  If the object is null, then the SQL keyword
	 * "NULL" is returned.  All objects return their string form, with the exception of the following:
	 * Strings add single quotes to the ends (example: <b><code>"hello"</code></b>
	 * becomes <b><code>"'hello'"</code></b>).  Date, Time and Timestamp objects use 
	 * String com.roth.base.util.Util.dateToSQLStr(java.util.Date),
	 * String com.roth.base.util.Util.timeToSQLStr(java.sql.Time), and 
	 * String com.roth.base.util.Util.dtsToSQLStr(java.util.Date), respectively. 
	 * @param source
	 * @param like whether to escape the source as a like expression
	 * @return
	 */
	public static String obj2SQLStr(DbmsType dbName, Object source, boolean like) {
		if (source == null)
			return "NULL";
		if (source.getClass().isArray() || source instanceof Collection) {
			@SuppressWarnings("unchecked")
			Stream<Object> stream = source.getClass().isArray() ? Arrays.stream((Object[])source) : ((Collection<Object>)source).stream();
			return "(%s)".formatted(stream.map(o -> obj2SQLStr(dbName, o, false)).collect(Collectors.joining(", ")));
		}
		return (source.getClass().isEnum()) ? "'" + source.toString() + "'"
			 : (source instanceof String) ? "'" + sqlEscape(dbName, source.toString(), like) + "'" 
			 : (source instanceof java.sql.Date cSource) ? dateToSQLStr(dbName, cSource)
			 : (source instanceof java.sql.Time cSource) ? timeToSQLStr(dbName, cSource)
			 : (source instanceof java.sql.Timestamp cSource) ? dtsToSQLStr(dbName, cSource)
             : (source instanceof java.util.Date cSource) ? dtsToSQLStr(dbName, cSource) 
             : (source instanceof LocalDate cSource) ? dtsToSQLStr(dbName, cSource.atStartOfDay())
             : (source instanceof LocalDateTime cSource) ? dtsToSQLStr(dbName, cSource)
             : (source instanceof LocalTime cSource) ? timeToSQLStr(dbName, cSource)
             : source.toString();
	}
	
	/**
	 * Converts an object to a SQL-friendly string.  If the object is null, then the SQL keyword
	 * "NULL" is returned.  All objects return their string form, with the exception of the following:
	 * Strings add single quotes to the ends (example: <b><code>"hello"</code></b>
	 * becomes <b><code>"'hello'"</code></b>).  Date, Time and Timestamp objects use 
	 * String com.roth.base.util.Util.dateToSQLStr(java.util.Date),
	 * String com.roth.base.util.Util.timeToSQLStr(java.sql.Time), and 
	 * String com.roth.base.util.Util.dtsToSQLStr(java.util.Date), respectively. 
	 * @param source
	 * @return
	 */
	public static String obj2SQLStr(DbmsType dbName, Object source) { return obj2SQLStr(dbName, source, false); }
	
	/**
	 * Converts an array of objects to a SQL-friendly list string.
	 * @param dbName
	 * @param source
	 * @return
	 */
    public static String arr2SQLStr(DbmsType dbName, Object[] source ) {
    	return col2SQLStr(dbName, Arrays.asList(source));
    }
	
	/**
	 * Converts a collection of objects to a SQL-friendly list string.
	 * @param dbName
	 * @param source
	 * @return
	 */
    public static String col2SQLStr(DbmsType dbName, Collection<Object> source ) {
    	return source.stream().map(x -> obj2SQLStr(dbName, x)).collect(Collectors.joining(","));
    }
    
    /**
     * Creates a SQL-friendly string ready for use with the LIKE operator.  
     * @param source
     * @param sourceMod "contains", "starts", "ends" or null (any other value equates to null).
     * @return
     */
    public static String str2SQLLike(String source, String sourceMod) {
        return (isEmpty(source)) ? null
                : "contains".equals(sourceMod) ? "%" + sqlEscape(source, true) + "%"
                : "starts".equals(sourceMod) ? sqlEscape(source, true) + "%"
                : "ends".equals(sourceMod) ? "%" + sqlEscape(source, true)
                : sqlEscape(source, true);
    }
    
    /**
     * Convert a date and/or time value to a string using the supplied format.<br/>  
     * Compatible with java.time.ZonedDateTime/OffsetDateTime/LocalDateTime/LocalDate/LocalTime,
     * java.util.Date, java.sql.Date/Time/Timestamp.<br/>
     * Note: not all formats work with all types.  For example, a format with time zone will only work
     * with ZonedDateTIme.
     * @param <T>
     * @param source
     * @param format
     * @return
     */
    public static <T> String dateToStr(T source, String format) {
    	if (source == null)
    		return null;
    	else if (format == null)
    		throw new IllegalArgumentException("The format argument cannot be null.");
    	DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
    	if (source instanceof ZonedDateTime cSource)
    		return cSource.format(formatter);
    	else if (source instanceof OffsetDateTime cSource)
    		return cSource.format(formatter); 
    	else if (source instanceof LocalDateTime cSource)
    		return cSource.atZone(ZoneId.systemDefault()).format(formatter);
    	else if (source instanceof LocalDate cSource)
    		return cSource.atStartOfDay().atZone(ZoneId.systemDefault()).format(formatter); 
    	else if (source instanceof LocalTime cSource)
    		return cSource.atDate(LocalDate.now()).atZone(ZoneId.systemDefault()).format(formatter); 
    	else if (source instanceof java.util.Date cSource)
    		return dateToStr(Instant.ofEpochMilli(cSource.getTime()).atZone(ZoneId.systemDefault()).toLocalDateTime(), format);
    	else if (source instanceof java.sql.Date cSource)
    		return dateToStr(Instant.ofEpochMilli(cSource.getTime()).atZone(ZoneId.systemDefault()).toLocalDateTime(), format);
    	else if (source instanceof java.sql.Time cSource)
    		return dateToStr(Instant.ofEpochMilli(cSource.getTime()).atZone(ZoneId.systemDefault()).toLocalDateTime(), format);
    	else if (source instanceof java.sql.Timestamp cSource)
    		return dateToStr(Instant.ofEpochMilli(cSource.getTime()).atZone(ZoneId.systemDefault()).toLocalDateTime(), format);
    	else
    		throw new IllegalArgumentException("The source argument must be a valid date and/or time object.");
    }
    
    /**
     * Convert a date and/or time value to a string using the ISO format.<br/>
     * "yyyy-MM-dd", "yyyy-MM-dd HH:mm_ss", or "HH:mm:ss".
     * @param <T>
     * @param source
     * @return
     */
    public static <T> String dateToStr(T source) {
    	if (source == null)
    		return null;
    	String format = ISO_DATETIME;
    	if (source instanceof java.time.LocalTime || source instanceof java.sql.Time)
    		format = ISO_TIME;
    	else if (source instanceof java.time.LocalDate || source instanceof java.sql.Date)
    		format = ISO_DATE;
    	return dateToStr(source, format);
    }
    
    /**
     * Convert a date-time value to a string using the ISO format.<br/>
     * This is technically an alias of dateToStr(T source).
     * @param <T>
     * @param source
     * @return
     */
    public static <T> String dtsToStr(T source) {
    	return dateToStr(source);
    }
    
    private static final String DATETIME = "DATETIME(";
    
    public static <T> String dateToSQLStr(DbmsType dbName, T source) {
    	String format = dbName == DbmsType.ORACLE
		              ? "dd-MMM-yyyy"
		              : dbName == DbmsType.SQL_SERVER
		              ? "yyyyMMdd"
		              : ISO_DATE;
	  	// Notes: MySQL, DB2, and Informix all use the ISO date 
	  	//        format of yyyy-MM-dd.
	  	//        MS SQL Server uses yyyyMMdd.
	  	return dbName == DbmsType.INFORMIX
	  	     ? DATETIME + dateToStr(source, format) + ") YEAR TO DAY"
	  	     : "'" + dateToStr(source, format) + "'";
    }
    
    public static <T> String timeToSQLStr(DbmsType dbName, T source) {
    	return dbName == DbmsType.INFORMIX
      		 ? DATETIME + dateToStr(source, ISO_TIME) + ") HOUR TO SECOND"
      		 : "'" + dateToStr(source, ISO_TIME) + "'";
    }
    
    public static <T> String dtsToSQLStr(DbmsType dbName, T source) {
    	String format = ISO_DATETIME;
    	// See notes in dateToSQLStr for date format info.
        return dbName == DbmsType.ORACLE
             ? "TO_DATE('" + dateToStr(source, format) + "', 'YYYY-MM-DD HH24:MI:SS')"
             : dbName == DbmsType.INFORMIX
			 ? DATETIME + dateToStr(source, format) + ") YEAR TO SECOND"
			 : "'" + dateToStr(source, format) + "'";
    }
    
    @SuppressWarnings("unchecked")
	public static final <T> T dateAdd(T source, String unit, Integer count) {
		if (source == null)
			return null;
		else if (unit == null || count == null)
			throw new IllegalArgumentException("The unit and count arguments cannot be null.");
		ChronoUnit chronoUnit; 
		switch (unit) {
			case "year": chronoUnit = ChronoUnit.YEARS; break;
			case "month": chronoUnit = ChronoUnit.MONTHS; break;
			case "day": chronoUnit = ChronoUnit.DAYS; break;
			case "hour": chronoUnit = ChronoUnit.HOURS; break;
			case "minute": chronoUnit = ChronoUnit.MINUTES; break;
			case "second": chronoUnit = ChronoUnit.SECONDS; break;
			case "milli": chronoUnit = ChronoUnit.MILLIS; break;
			case "micro": chronoUnit = ChronoUnit.MICROS; break;
			case "nano": chronoUnit = ChronoUnit.NANOS; break;
			default: throw new IllegalArgumentException("Invalid unit.  Must be one of 'year', 'month', 'day', 'hour', 'minute', 'second', 'milli', 'micro', or 'nano'.");
		}
		if (source instanceof ZonedDateTime)
			return (T)((ZonedDateTime)source).plus(count, chronoUnit);
		else if (source instanceof OffsetDateTime)
			return (T)((OffsetDateTime)source).plus(count, chronoUnit);
		else if (source instanceof LocalDateTime)
			return (T)((LocalDateTime)source).plus(count, chronoUnit);
		else if (source instanceof LocalDate)
			return (T)((LocalDate)source).atStartOfDay().plus(count, chronoUnit).toLocalDate();
		else if (source instanceof LocalTime)
			return (T)((LocalTime)source).atDate(LocalDate.of(1900, 1, 1)).plus(count, chronoUnit).toLocalTime();
		else if (source instanceof java.sql.Date)
			return (T)java.sql.Date.valueOf(LocalDateTime.ofInstant((new java.util.Date(((java.sql.Date)source).getTime())).toInstant(), ZoneId.systemDefault()).plus(count, chronoUnit).toLocalDate());
		else if (source instanceof java.sql.Time)
			return (T)java.sql.Time.valueOf(LocalDateTime.ofInstant((new java.util.Date(((java.sql.Time)source).getTime())).toInstant(), ZoneId.systemDefault()).plus(count, chronoUnit).toLocalTime());
		else if (source instanceof java.sql.Timestamp)
			return (T)java.sql.Timestamp.valueOf(LocalDateTime.ofInstant((new java.util.Date(((java.sql.Timestamp)source).getTime())).toInstant(), ZoneId.systemDefault()).plus(count, chronoUnit));
		else if (source instanceof java.util.Date)
			return (T)java.util.Date.from(LocalDateTime.ofInstant(((java.util.Date)source).toInstant(), ZoneId.systemDefault()).plus(count, chronoUnit).atZone(ZoneId.systemDefault()).toInstant());
		else
			throw new IllegalArgumentException("The source argument must be an instance of TemporalAccessor or java.util.Date.");
	}
    
    public static LocalDate strToLocalDate(String source, String format) {
    	LocalDateTime result = strToLocalDateTime(source, format);
		return result == null ? null : result.toLocalDate();
    }
    
    public static LocalDate strToLocalDate(String source) {
    	return strToLocalDate(source, ISO_DATETIME);
    }
    
    public static LocalDateTime strToLocalDateTime(String source, String format) {
    	if (isEmpty(source))
    		return null;

    	if (source.contains("T"))
    		source = source.replace("T", " ");
    	String pattern = JSON_DATETIME.equals(format) ? ISO_DATETIME
    			: format.length() >= 10 
    			? format + ISO_DATETIME.substring(format.length()) 
                : ISO_DATETIME.substring(0, ISO_DATETIME.length() - source.length()) + format;

    	
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
		String fmt = format.length() >= 10 ? source + ISO_TEMPLATE.substring(source.length()) : ISO_TEMPLATE.substring(0, ISO_TEMPLATE.length() - source.length()) + source;
    	return LocalDateTime.parse(fmt, formatter);
    }
    
    public static LocalDateTime strToLocalDateTime(String source) {
		return strToLocalDateTime(source, ISO_DATETIME);
    }
    
    public static LocalTime strToLocalTime(String source, String format) {
    	LocalDateTime result = strToLocalDateTime(source, format);
		return result == null ? null : result.toLocalTime();
    }
    
    public static LocalTime strToLocalTime(String source) {
    	return strToLocalTime(source, ISO_TIME);
    }
    
    // strToDate - Evaluate a string to a date, date-time, or time using "format".
    public static Date strToDate(String source, String format) {
    	LocalDateTime result = strToLocalDateTime(source, format);
		return result == null ? null : Date.from(result.atZone(ZoneId.systemDefault()).toInstant());
    }
    
    // strToDate - Evaluate a string to a date using the default format.
    public static Date strToDate(String source) {
		return strToDate(source, ISO_DATE);
    }
    
    // strToDts - Evaluate a string to a date-time using the default format.
    public static Date strToDts(String source) {
		return strToDate(source, ISO_DATETIME);
    }
    
    /**
     * Truncates the given date, removing the time portion.<br/>
     * Example:<br/>
     * 2014-06-25 14:52:31 -> 2014-06-25 00:00:00 
     * @param source
     * @return
     */
    public static Date truncDate(Date source) {
    	return truncDate(source, Calendar.DATE);
    }
    
    private static final String[] TRUNC_FORMATS = { "yyyy-01-01 00:00:00", "yyyy-MM-01 00:00:00", "yyyy-MM-dd 00:00:00", "yyyy-MM-dd HH:00:00", "yyyy-MM-dd HH:mm:00" };
    
    /**
     * Truncates the given date to the given resolution.<br/>
     * Examples:<br/>
     * 2014-06-25 14:52:31, Calendar.MINUTE -> 2014-06-25 14:52:00<br/>
     * 2014-06-25 14:52:31, Calendar.HOUR -> 2014-06-25 14:00:00<br/>
     * 2014-06-25 14:52:31, Calendar.DATE -> 2014-06-25 00:00:00<br/>
     * 2014-06-25 14:52:31, Calendar.MONTH -> 2014-06-01 00:00:00<br/>
     * 2014-06-25 14:52:31, Calendar.YEAR -> 2014-01-01 00:00:00
     * @param source
     * @param resolution Use Calendar.MINUTE, Calendar.HOUR, Calendar.DATE, Calendar.MONTH, or Calendar.YEAR
     * @return
     */
    public static Date truncDate(Date source, int resolution) {
    	if (source == null)
    		return null;
    	int index = resolution / 2;
    	if (!in(index, new Integer[] {0, 1, 2, 5, 6}))
    		throw new IllegalArgumentException("The resolution argument must be one of Calendar.[MINUTE|HOUR|DATE|MONTH|YEAR].");
    	return strToDate(dateToStr(source, TRUNC_FORMATS[index < 3 ? index : index - 2]), ISO_DATETIME);
    }
    
    /**
     * Returns the ordinal position of the day of the week.  Sunday = 0, Monday = 1, ...
     * @param date
     * @return
     */
    public static int weekday(LocalDate date) {
		return weekday(date.atStartOfDay());
	}
    
    /**
     * Returns the ordinal position of the day of the week.  Sunday = 0, Monday = 1, ...
     * @param date
     * @return
     */
    public static int weekday(LocalDateTime date) {
		int wd = date.getDayOfWeek().getValue();
		return (wd == 7) ? 0 : wd;
	}
    
    /**
     * Returns the ordinal position of the day of the week.  Sunday = 0, Monday = 1, ...
     * @param date
     * @return
     */
    public static int weekday(Date date) {
    	return weekday(Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault()).toLocalDateTime());
	}
    
    protected static LocalDateTime doOpOnSeg(LocalDateTime source, char op, String seg) throws ParseException {
    	LocalDateTime result = source;
    	if (op == ' ') {
			char c = seg.charAt(0);
			if (c == 'N') // Now
				{ /* Do nothing */ }
			else if (c == 'T') // Today
				result = result.truncatedTo(ChronoUnit.DAYS);
			else if (c == 'F') { // First of month
				Integer x = Data.strToInteger(Data.envl(seg.substring(1)));
				result = LocalDateTime.of(result.getYear(), result.getMonth(), 1, 0, 0);
				if (x != null)
					result = result.withMonth(x);
			}
		}
		else if ((op == '+') || (op == '-')) {
			char c = seg.charAt(seg.length() - 1);
			try {
				int x = strToInteger(op + seg.substring(0, seg.length() - 1));
				switch (c) {
					case 'Y': result = result.plusYears(x); break;
					case 'M': result = result.plusMonths(x); break;
					case 'D': result = result.plusDays(x); break;
					case 'h': result = result.plusHours(x); break;
					case 'm': result = result.plusMinutes(x); break;
					case 's': result = result.plusSeconds(x); break;
					default: throw new ParseException("Invalid interval: " + c, 0);
				}
			}
			catch (NumberFormatException e) {
				throw new ParseException("Invalid interval amount: " + seg.substring(0, seg.length() - 1), 0);
			}
		}
		else 
			throw new ParseException("Invalid operator: " + op, 0);
		return result;
    }
    
    /**
     * Returns a date based on an expression or a literal.  If a literal (using ISO format) is used, then
     * it is converted as-is to a date or datetime.  An expression is made up of an origin date or date-time,
     * and intervals delimted by either a plus or minus.
     * The Origin is either a 'T' for today (without time), 'N' for now (with time), or 'F' for the first 
     * day of the current month.  'F' can be optionally followed by a number indicating the intended month.
     * The intervals are a number followed by one of: 'Y' year, 'M' month, 'D' day, 'h' hour, 'm' minute, 
     * or 's' second.<br/>
     * <br/>
     * Examples:<br/>
     * T --> today<br/>
     * T+1D --> tomorrow<br/>
     * T-1D --> yesterday<br/>
     * T-7D --> one week ago<br/>
     * T-1Y-6M --> one year and six months ago<br/>
     * N-5h-30m --> five an a half hours ago<br/>
     * F --> the first of the current month<br/>
     * F4 --> the first of April of the current year<br/>
     * <br/>
     * Accepted literal formats:<br/>
     * yyyy-MM-dd<br/>
     * yyyy-MM-dd HH:mm<br/>
     * yyyy-MM-dd HH:mm:ss<br/>
     * @param expression
     * @param source
     * @return
     * @throws Exception
     */
    public static LocalDateTime expToDts(String expression, LocalDateTime source) throws ParseException {
    	return expToDts(expression, source, null);
    }
    
    private static String[] fmts(Map<String,String> formats) { 
    	if (formats == null)
    		return new String[] { ISO_TIME, ISO_DATE, ISO_DATETIME_MIN, ISO_DATETIME };
    	else
    		return new String[] { 
       			formats.get(FMT_TIME),
    			formats.get(FMT_DATE),
    			formats.get(FMT_DATETIME).substring(0, formats.get(FMT_DATETIME).length() - 3), 
    			formats.get(FMT_DATETIME)
    		};
    }
    
    private static Integer formatIndex(int length) {
    	return !in(length, new Integer[] {8, 10, 16, 19}) ? null : length / 6 - (length < 10 ? 1 : 0);
    }
    
    /**
     * Returns a date based on an expression or a literal.  If a literal (using ISO format) is used, then
     * it is converted as-is to a date or datetime.  An expression is made up of an origin date or date-time,
     * and intervals delimted by either a plus or minus.
     * The Origin is either a 'T' for today (without time), 'N' for now (with time), or 'F' for the first 
     * day of the current month.  'F' can be optionally followed by a number indicating the intended month.
     * The intervals are a number followed by one of: 'Y' year, 'M' month, 'D' day, 'h' hour, 'm' minute, 
     * or 's' second.<br/>
     * <br/>
     * Examples:<br/>
     * T --> today<br/>
     * T+1D --> tomorrow<br/>
     * T-1D --> yesterday<br/>
     * T-7D --> one week ago<br/>
     * T-1Y-6M --> one year and six months ago<br/>
     * N-5h-30m --> five an a half hours ago<br/>
     * F --> the first of the current month<br/>
     * F4 --> the first of April of the current year<br/>
     * <br/>
     * Accepted literal formats:<br/>
     * yyyy-MM-dd<br/>
     * yyyy-MM-dd HH:mm<br/>
     * yyyy-MM-dd HH:mm:ss<br/>
     * @param expression
     * @param source
     * @param formats
     * @return
     * @throws Exception
     */
    public static LocalDateTime expToDts(String expression, LocalDateTime source, Map<String,String> formats) throws ParseException {
    	if (expression == null) return null;
    	Integer index = formatIndex(expression.length());
        char c = expression.charAt(0);
        if (c >= '0' && c <= '9')
        	return index == null ? null : strToLocalDateTime(expression, fmts(formats)[index]);
		StringBuilder seg = new StringBuilder("");
		char op = ' ';
		LocalDateTime result = source != null ? source : LocalDateTime.now();
		for (int i = 0; i < expression.length(); i++) {
			c = expression.charAt(i);
			if ((c == '+') || (c == '-')) {
				result = doOpOnSeg(result, op, seg.toString());
				op = c;
				seg = new StringBuilder("");
			}
			else seg.append(c);
		}
		result = doOpOnSeg(result, op, seg.toString());
		return result;
	}
    
    /**
     * Returns a date based on an expression or a literal.  If a literal (using ISO format) is used, then
     * it is converted as-is to a date or datetime.  An expression is made up of an origin date or date-time,
     * and intervals delimted by either a plus or minus.
     * The Origin is either a 'T' for today (without time), 'N' for now (with time), or 'F' for the first 
     * day of the current month.  'F' can be optionally followed by a number indicating the intended month.
     * The intervals are a number followed by one of: 'Y' year, 'M' month, 'D' day, 'h' hour, 'm' minute, 
     * or 's' second.<br/>
     * <br/>
     * Examples:<br/>
     * T --> today<br/>
     * T+1D --> tomorrow<br/>
     * T-1D --> yesterday<br/>
     * T-7D --> one week ago<br/>
     * T-1Y-6M --> one year and six months ago<br/>
     * N-5h-30m --> five an a half hours ago<br/>
     * F --> the first of the current month<br/>
     * F4 --> the first of April of the current year<br/>
     * <br/>
     * Accepted literal formats:<br/>
     * yyyy-MM-dd<br/>
     * yyyy-MM-dd HH:mm<br/>
     * yyyy-MM-dd HH:mm:ss<br/>
     * @param expression
     * @param source
     * @return
     * @throws Exception
     */
    public static Date expToDts(String expression, Date source) throws ParseException {
    	return expToDts(expression, source, null);
    }
    
    /**
     * Returns a date based on an expression or a literal.  If a literal (using ISO format) is used, then
     * it is converted as-is to a date or datetime.  An expression is made up of an origin date or date-time,
     * and intervals delimted by either a plus or minus.
     * The Origin is either a 'T' for today (without time), 'N' for now (with time), or 'F' for the first 
     * day of the current month.  'F' can be optionally followed by a number indicating the intended month.
     * The intervals are a number followed by one of: 'Y' year, 'M' month, 'D' day, 'h' hour, 'm' minute, 
     * or 's' second.<br/>
     * <br/>
     * Examples:<br/>
     * T --> today<br/>
     * T+1D --> tomorrow<br/>
     * T-1D --> yesterday<br/>
     * T-7D --> one week ago<br/>
     * T-1Y-6M --> one year and six months ago<br/>
     * N-5h-30m --> five an a half hours ago<br/>
     * F --> the first of the current month<br/>
     * F4 --> the first of April of the current year<br/>
     * <br/>
     * Accepted literal formats:<br/>
     * yyyy-MM-dd<br/>
     * yyyy-MM-dd HH:mm<br/>
     * yyyy-MM-dd HH:mm:ss<br/>
     * @param expression
     * @param source
     * @param formats
     * @return
     * @throws Exception
     */
    public static Date expToDts(String expression, Date source, Map<String,String> formats) throws ParseException {
    	LocalDateTime lsource = Instant.ofEpochMilli(source.getTime()).atZone(ZoneId.systemDefault()).toLocalDateTime();
    	return Date.from(expToDts(expression, lsource, formats).atZone(ZoneId.systemDefault()).toInstant());
	}
    
/*
    private static <T> T convert(Object source, Class<T> type) {
    	if (source == null)
    		return null;
    	if (type == String.class)
    		return (T)source.toString();
    	if (type == Byte.class)
    		return (T)((Number)source).byteValue();
    		
    }
  */  
    public static String toString(Object source) { 
    	return source == null ? null : source.getClass().isPrimitive() ? String.valueOf(source) : source.toString(); 
    }
    
    public static Byte toByte(Object source) {
    	if (source == null)
    		return null;
    	if (source instanceof Number number)
    		return number.byteValue();
    	if (source instanceof String string)
    		return Byte.valueOf(string);
    	throw new IllegalArgumentException("Source is an invalid type.");
    }
    
    public static Short toShort(Object source) {
    	if (source == null)
    		return null;
    	if (source instanceof Number number)
    		return number.shortValue();
    	if (source instanceof String string)
    		return Short.valueOf(string);
    	throw new IllegalArgumentException("Source is an invalid type.");
    }
    
    public static Integer toInteger(Object source) {
    	if (source == null)
    		return null;
    	if (source instanceof Number number)
    		return number.intValue();
    	if (source instanceof String string)
    		return Integer.valueOf(string);
    	throw new IllegalArgumentException("Source is an invalid type.");
    }
    
    public static Long toLong(Object source) {
    	if (source == null)
    		return null;
    	if (source instanceof Number number)
    		return number.longValue();
    	if (source instanceof String string)
    		return Long.valueOf(string);
    	throw new IllegalArgumentException("Source is an invalid type.");
    }
    
    public static Float toFloat(Object source) {
    	if (source == null)
    		return null;
    	if (source instanceof Number number)
    		return number.floatValue();
    	if (source instanceof String string)
    		return Float.valueOf(string);
    	throw new IllegalArgumentException("Source is an invalid type.");
    }
    
    public static Double toDouble(Object source) {
    	if (source == null)
    		return null;
    	if (source instanceof Number number)
    		return number.doubleValue();
    	if (source instanceof String string)
    		return Double.valueOf(string);
    	throw new IllegalArgumentException("Source is an invalid type.");
    }
    
    public static String integerToStr(Integer source) {
    	return source == null ? null : source.toString();
    }
    
    public static Integer strToInteger(String source) {
        return isEmpty(source) ? null : Integer.parseInt(source.replace(",", ""));
    }
    
    public static String longToStr(Long source) {
    	return source == null ? null : source.toString();
    }
    
    public static Long strToLong(String source) {
    	return isEmpty(source) ? null : Long.parseLong(source.replace(",", ""));
    }
    
    public static String floatToStr(Float source) {
    	return source == null ? null : source.toString();
    }
    
    public static Float strToFloat(String source) {
    	return isEmpty(source) ? null : Float.parseFloat(source.replace(",", ""));
    }
    
    public static String doubleToStr(Double source) {
    	return source == null ? null : source.toString();
    }
    
    public static Double strToDouble(String source) {
    	return isEmpty(source) ? null : Double.parseDouble(source.replace(",", ""));
    }
    
    public static String bigDecimalToStr(BigDecimal source) {
    	return source == null ? null : source.toString();
    }
    
    public static BigDecimal strToBigDecimal(String source) {
        return isEmpty(source) ? null : new BigDecimal(source.replace(",", ""));
    }
    
    public static Object objToStrNvl(Object source, String defaultValue) {
    	return (source == null) ? defaultValue : obj2Str(source);
    }
    
    public static String join(String[] source, String delimiter) {
		return join(source, delimiter, false);
	}
	
    public static String join(String[] source, String delimiter, boolean parameterize) {
		if (source == null) return null;
		StringBuilder result = new StringBuilder("");
		for (int i = 0; i < source.length; i++)
			result.append(((i > 0) ? delimiter : "") + ((!parameterize) ? source[i] : "{" + /*source[i].replace("_", "")*/camelcase(source[i]) + "}"));
		return result.toString();
	}
	
    public static String exJoin(String[] source, String delimiter, boolean numeric) {
    	if (source == null) return null;
		StringBuilder result = new StringBuilder("");
		for (int i = 0; i < source.length; i++)
			result.append(((i > 0) ? delimiter : "") + source[i] + " = {" + (numeric ? i + 1 : camelcase(source[i])) + "}");
		return result.toString();
    }
    
    public static String exJoin(String[] source, String delimiter) {
		if (source == null) return null;
		StringBuilder result = new StringBuilder("");
		for (int i = 0; i < source.length; i++)
			result.append(((i > 0) ? delimiter : "") + source[i] + " = {" + /*source[i].replace("_", "")*/camelcase(source[i]) + "}");
		return result.toString();
	}
	
    public static String[] minus(String[] source, String[] subset) {
    	Arrays.sort(subset);
		String[] result = new String[source.length - subset.length];
		int x = 0;
		for (int i = 0; i < source.length; i++) {
			if (Arrays.binarySearch(subset, source[i]) < 0)
				result[x++] = source[i];
		}
		return result;
	}
    
	public static int complexHashCode(Object ... values) {
		int result = 0;
		for (Object value : values)
			if (value != null)
				result = result ^ value.hashCode();
		return result;
	}
    
    public static Map<String,String> stringsToMap(String[] source) {
    	if (source == null)
    		return Collections.emptyMap();
    	Map<String,String> result = new LinkedHashMap<>();
    	for (String s : source)
    		result.put(s,s);
    	return result;
    }
    
    public static Map<String,String> stringsToMap(Collection<String> source) {
    	if (source == null)
    		return Collections.emptyMap();
    	return stringsToMap(source.toArray(new String[source.size()]));
    }
    
    public static String hexString(byte source) {
		String result = Integer.toHexString(0xFF & source);
		if (result.length() < 2) result = "0" + result;
		return result;
	}
	
	public static String toHexString(int i, int length) {
		StringBuilder result = new StringBuilder(Integer.toHexString(i));
		while (result.length() < length) 
			result.insert(0, "0");
		return result.toString();
	}
    
	private static final Random RANDOM = new Random();
	
    public static String randChar() {
		if (RANDOM.nextInt(10) > 8) return "" + (char)(RANDOM.nextInt(94) + 32);
		String result = Integer.toString(RANDOM.nextInt(Character.MAX_RADIX), Character.MAX_RADIX);
		return (RANDOM.nextInt(10) > 6) ? result.toUpperCase() : result;
	}
	
	public static String shuffle(String source, int times) {
		StringBuilder result = new StringBuilder("");
		int l = source.length() / 2 - 1;
		for (int i = 0; i <= l; i++)
			result.append("" + source.charAt(l - i) + source.charAt(l + 1 + i));
		return (times > 1) ? shuffle(result.toString(), times - 1) : result.toString();
	}
	
	public static String unshuffle(String source, int times) {
		StringBuilder resulta = new StringBuilder("");
		StringBuilder resultb = new StringBuilder("");
		for (int i = 0; i < (source.length() / 2); i++) {
			resulta.insert(0, "" + source.charAt(i * 2));
			resultb.append("" + source.charAt(i * 2 + 1));
		}
		String result = resulta.toString() + resultb.toString();
		return (times > 1) ? unshuffle(result, times - 1) : result;
	}
	
	public static String shift(String source, int length) {
		StringBuilder result = new StringBuilder("");
		for (int i = 0; i < source.length(); i++) {
			int c = source.charAt(i);
			c += length;
			if (c > 126) c = 32 + (c - 126);
			if (c < 32) c = 126 - (32 - c);
			result.append("" + (char)c); 
		}
		return result.toString();
	}
	
	public static String encrypt(String source, long seed) {
		if (source == null)
			return null;
		StringBuilder result = new StringBuilder(toHexString(source.length(), 2) + source);
		while(result.length() < 80) 
			result.append(randChar());
		java.util.Random r = new java.util.Random(seed);
		int times = r.nextInt(96);
		int length = r.nextInt(96);
		return shift(shuffle(result.toString(), times), length);
	}
	
	public static String decrypt(String source, long seed) {
		if (source == null || source.length() < 80)
			return null;
		java.util.Random r = new java.util.Random(seed);
		int times = r.nextInt(96);
		int length = r.nextInt(96);
		String result = unshuffle(shift(source, -length), times);
		int l = Integer.parseInt(result.substring(0, 2), 16);
		return result.substring(2, 2 + l);
	}
	
	/**
	 * Get a list of files found at the specified path.
	 * @param path
	 * @return
	 * @throws IOException
	 */
	public static Set<String> getFileList(String path) throws IOException {
	    try (Stream<Path> stream = Files.list(Paths.get(path))) {
	        return stream
	          .filter(file -> !Files.isDirectory(file))
	          .map(Path::getFileName)
	          .map(Path::toString)
	          .collect(Collectors.toSet());
	    }
	}
	
	/**
	 * Get a list of files found at the specified path.
	 * @param path
	 * @return
	 * @throws IOException
	 */
	public static Set<String> getFileList(ServletContext context, String path) throws IOException {
		return getFileList(context.getRealPath(path));
	}
	
	/**
	 * Read the contents of a text file, assuming the UTF-8 charset.
	 * @param filename the name (and path) of the text file
	 * @return the contents of the file
	 * @throws IOException 
	 */
	public static String readTextFile(String filename) throws IOException {
        return readTextFile(filename, Charsets.UTF_8);
    }
	
	/**
	 * Read the contents of a text file using the given charset.
	 * @param filename the name (and path) of the text file
	 * @param charset the charset the file is expected to use.
	 * @return the contents of the file
	 * @throws IOException 
	 */
	public static String readTextFile(String filename, Charset charset) throws IOException {
        return Files.readString(Path.of(filename), charset);
    }
	
	
	/**
	 * Read the contents of a text file from the given offset, assuming the UTF-8 charset.
	 * @param filename
	 * @param offset
	 * @return A RandomReadString which contains the string from offset to the end of the file, and the total bytes read.
	 * @throws IOException
	 */
	public static RandomReadString readTextFile(String filename, long offset) throws IOException {
		try (RandomAccessFile file = new RandomAccessFile(filename, "r")) {
			file.seek(offset);
			byte[] buffer = new byte[8192];
			long totalBytesRead = 0;
			int bytesRead;
			StringBuilder content = new StringBuilder("");
			while ((bytesRead = file.read(buffer)) != -1) {
				content.append(new String(buffer, "UTF-8"));
				totalBytesRead += bytesRead;
			}
			return new RandomReadString(content.toString(), totalBytesRead, offset + totalBytesRead);
		}
	}
	
	/**
	 * Read the contents of a text file in the class loader containing class "c".
	 * The class defined by parameter "c" can be any class, as long as it resides 
	 * in the same class loader as the file to be retrieved.
	 * @param c the class that acts as class loader reference
	 * @param filename the name of the text file
	 * @return the contents of the file
	 */
	@SuppressWarnings("resource")
	public static String readTextFile(Class<?> c, String filename) {
        return new Scanner(c.getResourceAsStream(filename)).useDelimiter("\\A").next();
    }
	
	/**
	 * Read the contents of a text file in the servlet context.
	 * @param context
	 * @param filename
	 * @return
	 */
	public static String readTextFile(ServletContext context, String filename, Charset charset) {
		String filepath = context.getRealPath(filename);
		try {
			return filepath == null ? null : Files.readString(Path.of(filepath), charset);
		} catch (IOException e) {
			Log.logException(e, null);
			return null;
		}
	}
	
	/**
	 * Read the contents of a text file in the servlet context.
	 * @param context
	 * @param filename
	 * @return
	 */
	public static String readTextFileQuiet(ServletContext context, String filename, Charset charset) {
		String filepath = context.getRealPath(filename);
		try {
			return filepath == null ? null : Files.readString(Path.of(filepath), charset);
		} catch (IOException e) {
			// Eat the exception
			return null;
		}
	}

	/**
	 * Write a string to a file.
	 * @param filename
	 * @param content
	 * @throws IOException
	 */
	public static void writeTextFile(String filename, String content) throws IOException {
		writeTextFile(filename, content, Charsets.UTF_8, false);
	}
	
	/**
	 * Write a string to a file.
	 * @param filename
	 * @param content
	 * @param append
	 * @throws IOException
	 */
	public static void writeTextFile(String filename, String content,  boolean append) throws IOException {
		writeTextFile(filename, content, Charsets.UTF_8, append);
	}
	
	/**
	 * Write a string to a file.
	 * @param filename
	 * @param content
	 * @param charset
	 * @throws IOException
	 */
	public static void writeTextFile(String filename, String content, Charset charset) throws IOException {
		writeTextFile(filename, content, charset, false);
	}
	
	/**
	 * Write a string to a file.
	 * @param filename
	 * @param content
	 * @param charset
	 * @param append
	 * @throws IOException
	 */
	public static void writeTextFile(String filename, String content, Charset charset,  boolean append) throws IOException {
		Files.writeString(Path.of(filename), content, charset, append ? StandardOpenOption.APPEND : StandardOpenOption.CREATE);
	}

	/**
	 * Write a string to a file within the context.
	 * @param context
	 * @param filename
	 * @param content
	 * @throws IOException
	 */
	public static void writeTextFile(ServletContext context, String filename, String content) throws IOException {
		writeTextFile(context, filename, content, false);
	}
	
	/**
	 * Write a string to a file within the context.
	 * @param context
	 * @param filename
	 * @param content
	 * @param append
	 * @throws IOException
	 */
	public static void writeTextFile(ServletContext context, String filename, String content, boolean append) throws IOException {
		writeTextFile(context.getRealPath(filename), content, append);
	}
	
	/**
	 * Write a string to a file within the context.  
	 * @param context
	 * @param filename
	 * @param content
	 * @param charset
	 * @throws IOException
	 */
	public static void writeTextFile(ServletContext context, String filename, String content, Charset charset) throws IOException {
		writeTextFile(context, filename, content, charset, false);
	}
	
	/**
	 * Write a string to a file within the context.  
	 * @param context
	 * @param filename
	 * @param content
	 * @param charset
	 * @param append
	 * @throws IOException
	 */
	public static void writeTextFile(ServletContext context, String filename, String content, Charset charset, boolean append) throws IOException {
		writeTextFile(context.getRealPath(filename), content, charset, append);
	}
	
	/**
	 * Check to see if a file exists in the servlet context.
	 * @param context
	 * @param filename
	 * @return
	 */
	public static boolean fileExists(ServletContext context, String filename) {
		String filepath = context.getRealPath(filename);
		File f = new File(filepath);
		return f.exists();
	}
	
	/**
	 * Detect mime type for file. Don't rely on file extension.
	 * @param file
	 * @return
	 * @throws IOException
	 */
	public static String getMimeType(File file) throws IOException {
		String result = new Tika().detect(file);
		if ("application/x-tika-ooxml".equals(result)) {
			String ext = file.getName().substring(file.getName().lastIndexOf(".") + 1);
			switch (ext) {
			case "xlsx": return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
			case "docx": return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
			case "pptx": return "application/vnd.openxmlformats-officedocument.presentationml.presentation";
			default: return result;
			}
		}
		return result;
	}
	
	/**
	 * Detect mime type for file. Don't rely on file extension.
	 * @param file
	 * @return
	 * @throws IOException
	 */
	public static String getMimeType(byte[] file) {
		return new Tika().detect(file);
	}
	
	/** Read the object from Base64 string. */
	@SuppressWarnings("unchecked")
    public static <T> T deserialize(String source) throws IOException, ClassNotFoundException {
        byte [] data = Base64.getDecoder().decode(source);
        GZIPInputStream gzip = new GZIPInputStream(new ByteArrayInputStream(data));
        ObjectInputStream input = new ObjectInputStream(gzip);
        Object object  = input.readObject();
        input.close();
        gzip.close();
        return (T)object;
    }

    /** Write the object to a Base64 string. */
    public static String serialize(Serializable object) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        GZIPOutputStream gzip = new GZIPOutputStream(bytes);
        ObjectOutputStream output = new ObjectOutputStream(gzip);
        output.writeObject(object);
        output.close();
        gzip.close();
        return Base64.getEncoder().encodeToString(bytes.toByteArray()); 
    }
    
    /**
	 * <b>parseDate</b><br><br>
	 * Parses a string to a java.util.Date.
	 * @param date - The string to parse.
	 * @return the parsed date.
	 * @throws ParseException
	 */
	public static java.util.Date parseDate(String date, Map<String,String> formats) throws ParseException {
		try { return expToDts(date, new java.util.Date(), formats); }
		catch (ParseException e) {
			try { return strToDate(date, FMT_DATE); }
			catch (DateTimeParseException e2) {
				return strToDate(date, FMT_DATE);
			}
		}
	}
	
	/**
	 * Null-safe convert of a java.util.Date to java.sql.Date.
	 * @param dts
	 * @return
	 */
	public static java.sql.Date dtsToDate(Date dts) {
		return dts == null ? null : new java.sql.Date(dts.getTime());
	}
	
	/**
	 * Null-safe convert of a java.util.Date to java.sql.Time.
	 * @param dts
	 * @return
	 */
	public static java.sql.Time dtsToTime(Date dts) {
		return dts == null ? null : new java.sql.Time(dts.getTime());
	}
	
	/**
	 * Null-safe convert of a java.util.Date to java.sql.Timestamp.
	 * @param dts
	 * @return
	 */
	public static java.sql.Timestamp dtsToTimestamp(Date dts) {
		return dts == null ? null : new java.sql.Timestamp(dts.getTime());
	}
	
	/**
	 * <b>getParamvalue</b><br><br>
	 * Translates a string to the requested class.
	 * @param clazz - The class to translate to.
	 * @param param - The string to translate.
	 * @return the translated class.  If the class is not recognized, it will return a String.
	 * @throws ParseException
	 * @throws SecurityException 
	 * @throws NoSuchMethodException 
	 * @throws InvocationTargetException 
	 * @throws IllegalArgumentException 
	 * @throws IllegalAccessException 
	 * @throws NumberFormatException 
	 */
	public static Object getParamValue(Class<?> c, String value, Map<String,String> formats) throws ParseException, NumberFormatException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		if ((c == null) || (isEmpty(value))) return null;
		if (c.equals(boolean.class) && (value.equals("on"))) value = "true";
		return (c.equals(boolean.class)) ? Boolean.parseBoolean(value)
			 : (c.equals(byte.class)) ? Byte.parseByte(value.replace(",", ""))
			 : (c.equals(double.class)) ? Double.parseDouble(value.replace(",", ""))
			 : (c.equals(float.class)) ? Float.parseFloat(value.replace(",", ""))
			 : (c.equals(int.class)) ? Integer.parseInt(value.replace(",", ""))
			 : (c.equals(long.class)) ? Long.parseLong(value.replace(",", ""))
			 : (c.equals(short.class)) ? Short.parseShort(value.replace(",", ""))
			 : (c.equals(java.lang.Boolean.class)) ? Boolean.parseBoolean(value)
			 : (c.equals(java.lang.Byte.class)) ? Byte.parseByte(value.replace(",", ""))
			 : (c.equals(java.lang.Double.class)) ? Double.parseDouble(value.replace(",", ""))
			 : (c.equals(java.lang.Float.class)) ? Float.parseFloat(value.replace(",", ""))
			 : (c.equals(java.lang.Integer.class)) ? Integer.parseInt(value.replace(",", ""))
			 : (c.equals(java.lang.Long.class)) ? Long.parseLong(value.replace(",", ""))
			 : (c.equals(java.lang.Short.class)) ? Short.parseShort(value.replace(",", ""))
			 : (c.equals(java.math.BigDecimal.class)) ? new java.math.BigDecimal(value.replace(",", ""))
			 : (c.equals(java.sql.Date.class)) ? dtsToDate(expToDts(value, new java.util.Date(), formats))
			 : (c.equals(java.sql.Time.class)) ? dtsToTime(expToDts(value, new java.util.Date(), formats))
		     : (c.equals(java.sql.Timestamp.class)) ? dtsToTimestamp(expToDts(value, new java.util.Date(), formats))
		     : (c.equals(java.util.Date.class)) ? expToDts(value, new java.util.Date(), formats)
		     : (c.equals(java.time.LocalDate.class)) ? expToDts(value, LocalDateTime.now(), formats).toLocalDate()
		     : (c.equals(java.time.LocalDateTime.class)) ? expToDts(value, LocalDateTime.now(), formats)
		     : (c.equals(java.time.LocalTime.class)) ? expToDts(value, LocalDateTime.now(), formats).toLocalTime()
		     : (c.isEnum()) ? c.getMethod("valueOf", String.class).invoke(null, value)
		     : value; // Defaults to return the string.
	}
	
	public static String parseEncodedParam(String encodedParam, String paramName) {
		if (encodedParam == null) return null;
		String[] params = encodedParam.split("&");
		for (int i = 0; i < params.length; i++) {
			String[] p = params[i].split("=");
			if (p[0].equals(paramName)) return p[1];
		}
		return null;
	}
	
	/**
	 * Strip all HTML tags from the source string.
	 * @param source
	 * @return
	 */
	public static String stripHtml(String source) {
		return source == null ? null
			 : source.replaceAll("<(script|style)[\\s\\S]*?>[\\s\\S]*?<\\/(script|style)>", "")
					 .replaceAll("<[^>]*?>", "");
	}
	
	public static String digest(String source, String method) {
		try {
			byte[] s = MessageDigest.getInstance(method).digest(source.getBytes());
			StringBuilder h = new StringBuilder();
			for (int i = 0; i < s.length; i++) h.append(hexString(s[i]));
			return h.toString();
		}
		catch (Exception e) { Log.logException(e, null); }
		return null;
	}
	
	public static String slowDigest(String source, String method, int passes) {
		if (passes < 1)
			throw new IllegalArgumentException("Passes must be greater than 0.");
		String result = digest(source, method);
		int pass = 1;
		while (pass < passes && result != null) {
			result = digest(result, method);
			pass++;
		}
		return result;
	}
	
	public static Object getWebEnv(String name) {
		try {
			Context ctx = new InitialContext();
		    Context env = (Context) ctx.lookup("java:comp/env");
		    return env.lookup(name);
		}
		catch (NamingException e) {
			return null;
		}
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T getWebEnv(String name, T defaultValue) {
		Object value = getWebEnv(name);
		return value == null ? defaultValue : (T)value;
	}
	
	public static String sanitize(String source, boolean path) throws MalformedURLException, FileNotFoundException {
		if (source == null)
			return null;
		if (path && malformedURL(source))
			throw new MalformedURLException("The specified URL contains invalid characters.");
		if (path && invalidURL(source))
			throw new FileNotFoundException("The specified URL is not found.");
		return path ? source : recontructParamString(source);
	}
	
	protected static final boolean malformedURL(String source) {
		return source.contains("'") || source.contains("<");
	}
	
	protected static final boolean invalidURL(String source) {
		int pos = source.indexOf("?");
		return !Util.isRegisteredPath(pos < 0 ? source : source.substring(0, pos));
	}
	
	protected static final String recontructParamString(String source) {
		String[] params = source.split("&");
		StringBuilder result = new StringBuilder("");
		for (String p : params) {
			if (!p.contains("=") || p.contains("'") || p.contains("<"))
				continue;
			result.append((result.isEmpty() ? "" : "&") + p);
		}
		return result.toString();
	}
	
	private static final String[] EXCLUDE_CONTEXTS = {"docs", "examples", "manager", "host-manager"};
	
	public static Iterable<String> getDeployedApps() throws MalformedObjectNameException, CannotProceedException {
        final Set<String> result = new HashSet<>();
        ObjectName oname = new ObjectName("*:j2eeType=WebModule,*,J2EEApplication=none,J2EEServer=none");
        final Set<ObjectName> instances = findServer("Catalina").queryNames(oname, null);
        for (ObjectName each : instances) {
        	String name = each.getKeyProperty("name");
        	name = name.substring(name.lastIndexOf('/') + 1);
        	if (!Data.in(name, EXCLUDE_CONTEXTS))
        		result.add(name);  
        }
        return result;
	}

	private static MBeanServer findServer(String serverName) throws CannotProceedException {
	    ArrayList<MBeanServer> servers = MBeanServerFactory.findMBeanServer(null);
	    for (MBeanServer server : servers) 
	        for (String domain : server.getDomains()) 
	            if (domain.equals(serverName)) 
	                return server;
	    throw new CannotProceedException(String.format("MBeanServer with domain '%s' not found.", serverName));
	}
	
	/**
	 * Invoke a setter method in an atomic object.
	 * @param method
	 * @param value
	 * @throws InvocationTargetException 
	 * @throws IllegalAccessException 
	 */
	public static void invokeSetter(Method method, Object value) throws IllegalAccessException, InvocationTargetException {
		method.invoke(value);
	}
	
	/**
	 * Invoke a setter method in an array element.
	 * @param method
	 * @param obj
	 * @param value
	 * @throws InvocationTargetException 
	 * @throws IllegalAccessException 
	 */
	public static void invokeSetter(Method method, Object obj, Object value) throws IllegalAccessException, InvocationTargetException {
		method.invoke(obj, value);
	}
	
	/**
	 * Check whether two objects are equal.  This inclusive of null to null comparison.
	 * @param a
	 * @param b
	 * @return
	 */
	public static boolean eq(Object a, Object b) {
		if (a == null && b == null)
			return true;
		if (a == null || b == null || !a.getClass().getCanonicalName().equals(b.getClass().getCanonicalName()))
			return false;
		return a.equals(b);
	}
	
	/**
	 * Combine two arrays of the same type.
	 * @param <T>
	 * @param array1
	 * @param array2
	 * @return
	 */
	public static <T> T[] concatArrays(T[] array1, T[] array2) {
	    T[] result = Arrays.copyOf(array1, array1.length + array2.length);
	    System.arraycopy(array2, 0, result, array1.length, array2.length);
	    return result;
	}
}
