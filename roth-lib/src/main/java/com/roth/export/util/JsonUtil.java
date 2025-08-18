package com.roth.export.util;

import java.io.StringReader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.text.StringEscapeUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.roth.base.annotation.Ignore;
import com.roth.base.log.Log;
import com.roth.base.util.Data;
import com.roth.export.annotation.JsonCollection;
import com.roth.export.annotation.JsonEnum;
import com.roth.export.annotation.JsonExtension;
import com.roth.export.annotation.JsonExtensions;
import com.roth.export.annotation.JsonMap;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.json.JsonValue;

public class JsonUtil {
	// public static final String JSON_DATETIME = "yyyy-MM-dd'T'HH:mm:ss.SSSZZZ";
	public static final String JSON_DATETIME = "yyyy-MM-dd'T'HH:mm:ss";
	
	protected String newline;
	protected String brackets;
	protected String brackete;
	protected String braces;
	protected String bracee;
	protected String colon;
	protected String indent;
	
	protected static final int JSON_STRING = 0;
	protected static final int JSON_DATE = 1;
	protected static final int JSON_NUMBER = 2;
	protected static final int JSON_BOOLEAN = 3;
	protected static final int JSON_ARRAY = 4;
	protected static final int JSON_COLLECTION = 5;
	protected static final int JSON_MAP = 6;
	protected static final int JSON_POJO = 7;
	protected static final int JSON_RGBA = 8;
	protected static final int JSON_NOQUOTE = 9;
	protected static final int JSON_ENUM = 10;
	
	protected JsonUtil(boolean formatted) {
		newline = formatted ? "\n" : "";
		brackets = formatted ? "[ " : "[";
		brackete = formatted ? " ]" : "]";
		braces = formatted ? "{ " : "{";
		bracee = formatted ? " }" : "}";
		colon = formatted ? " : " : ":";
		indent = formatted ? "  " : "";
	}
	
	/**
	 * Determine the data type of the object supplied.  See constants JSON_* in this class.
	 * @param object
	 * @return
	 */
	protected int getType(Object object) {
		if (object.getClass().isArray())
			return JSON_ARRAY;
		else if (object instanceof Collection)
			return JSON_COLLECTION;
		else if (object instanceof Map) 
			return JSON_MAP;
		else if (object instanceof java.lang.Boolean)
			return JSON_BOOLEAN;
		else if (object.getClass().isPrimitive() || object instanceof java.lang.Number)
			return JSON_NUMBER;
		else if (object instanceof java.util.Date || object instanceof java.time.temporal.Temporal)
			return JSON_DATE;
		else if (object instanceof java.lang.String)
			return JSON_STRING;
		else if (object instanceof java.lang.Enum)
			return JSON_ENUM;
		//else if (object instanceof RGB)
		//	return JSON_RGBA;
		//else if (object instanceof ChartFunction)
		//	return JSON_NOQUOTE;
		else
			return JSON_POJO;
	}
	
	/**
	 * Quote a string or date for JSON output.
	 * @param source The string to be quoted.
	 * @return The quoted string.
	 */
	public static String quote(String source) {
		return "\"" + source + "\"";
	}
	
	/**
	 * Convert an object to a string for JSON output.  This supports atomics (primitives, class primitives, String, and descendants of 
	 * java.util.Date or java.lang.Number), arrays, collections (i.e. lists, sets, etc.), and maps. 
	 * @param object The object to be converted.
	 * @return The converted object.
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 */
	protected String objectToJson(Object object) throws InvocationTargetException, IllegalAccessException {
		if (object == null)
			return "{}";
		if (object instanceof JsonFormatter jsonFormatter)
			return jsonFormatter.toJson(newline.equals("\n"));
		int type = getType(object);
		return switch(type) { 
			case JSON_POJO -> pojoToJson(object);
			case JSON_ARRAY -> arrayToJson(object);
			case JSON_COLLECTION -> collectionToJson(object);
			case JSON_MAP -> mapToJson(object); 
			case JSON_DATE -> quote(Data.dateToStr(object, JSON_DATETIME)); 
			case JSON_STRING -> quote(StringEscapeUtils.escapeJson((String)object));
			case JSON_ENUM -> quote(object.toString());
			case JSON_RGBA -> quote(object.toString());
			/* type is one of: JSON_NUMBER, JSON_BOOLEAN, JSON_NOQUOTE */
			default -> object.toString();
		};
	}
	
	/**
	 * Tokenize
	 * @param name
	 * @param value
	 * @param numeric
	 * @param date
	 * @return
	 */
	protected String tokenize(String name, String value) {
		return quote(name) + colon + value;
	}
	
	public static String tokenize(String name, String value, boolean comma) {
		return value == null ? "" : quote(name) + ":" + value + (comma ? "," : "");
	}
	
	/**
	 * Convert a pojo to a JSON object.
	 * @param object The pojo to be converted.
	 * @return The converted object.
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 */
	protected String pojoToJson(Object object) throws InvocationTargetException, IllegalAccessException {
		StringBuilder result = new StringBuilder("");
		Method[] methods = object.getClass().getMethods();
		for (Method method : methods) {
			if (method.getAnnotation(Ignore.class) != null ||
				method.getAnnotation(JsonIgnore.class) != null)
				continue;
			if (method.getDeclaringClass().equals(Object.class))
				continue;
			String name = method.getName();
			if (name.startsWith("get") && method.getParameters().length == 0) {
				Object value = method.invoke(object);
				if (value != null) {
					name = ("" + name.charAt(3)).toLowerCase() + name.substring(4);
					result.append((result.isEmpty() ? "" : "," + newline) + indent + tokenize(name, objectToJson(value)));
				}
			}
		}
		return braces + newline + result.toString() + newline + bracee;
	}
	
	/**
	 * Convert an array to a JSON array.
	 * @param object
	 * @return
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 */
	protected String arrayToJson(Object object) throws InvocationTargetException, IllegalAccessException {
		StringBuilder result = new StringBuilder("");
		for (int i = 0; i < Array.getLength(object); i++) {
			result.append((result.isEmpty() ? "" : "," + newline) + indent + objectToJson(Array.get(object, i)));
		}
		return brackets + result.toString() + brackete;
	}
	
	/**
	 * Convert a Collection (List, Set, etc.) to a JSON array.
	 * @param object
	 * @return
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 */
	@SuppressWarnings("rawtypes")
	protected String collectionToJson(Object object) throws InvocationTargetException, IllegalAccessException {
		StringBuilder result = new StringBuilder("");
		Iterator o = ((Collection)object).iterator();
		while (o.hasNext()) {
		    Object obj = o.next();
			result.append((result.isEmpty() ? "" : "," + newline) + indent + objectToJson(obj));
		}
		return brackets + result.toString() + brackete;
	}
	
	/**
	 * Convert a map to 
	 * @param object
	 * @return
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 */
	@SuppressWarnings("rawtypes")
	protected String mapToJson(Object object) throws InvocationTargetException, IllegalAccessException {
		StringBuilder result = new StringBuilder("");
		Map map = (Map)object;
		Iterator o = map.keySet().iterator();
		while (o.hasNext()) {
			Object key = o.next();
		    Object obj = map.get(key);
			result.append((result.isEmpty() ? "" : "," + newline) + indent + tokenize(key.toString(), objectToJson(obj)));
		}
		return braces + result.toString() + bracee;
	}

	/**
	 * Convert an object to JSON notation.  This will generate compacted JSON notation.
	 * @param object
	 * @return
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 */
	public static String objToJson(Object object) throws InvocationTargetException, IllegalAccessException {
		return objToJson(object, false);
	}
	
	/** 
	 * Convert an object to JSON notation.  If formatted = true, then the generated notation is formatted 
	 * for readability, otherwise this will generate compacted JSON notation. 
	 * @param object
	 * @param formatted
	 * @return
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 */
	public static String objToJson(Object object, boolean formatted) throws InvocationTargetException, IllegalAccessException {
		if (object == null)
			throw new NullPointerException("Null cannot be converted to JSON.");
		return new JsonUtil(formatted).objectToJson(object);
	}
	
	/**
	 * Translate a JSON string into a Java object.  Use this method for atomics, POJOs, and arrays.
	 * @param <T>
	 * @param json
	 * @param objClass
	 * @return
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws NoSuchMethodException
	 */
	@SuppressWarnings("unchecked")
	public static <T> T jsonToObj(String json, Class<T> objClass) throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		if (Data.isEmpty(json) || json.equals("{}"))
			return null;
		try (JsonReader jsonReader = Json.createReader(new StringReader(json))) {
			JsonObject jsonObject = jsonReader.readObject();
			return (T)toObject(jsonObject, objClass, null, null);
		}
	}
	
	/**
	 * Translate a JSON string into a Java object.  Use this method for collections (lists, sets, etc.).
	 * @param <T>
	 * @param json
	 * @param objClass
	 * @param elementClass
	 * @return
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws NoSuchMethodException
	 */
	@SuppressWarnings("unchecked")
	public static <T> T jsonToObj(String json, Class<T> objClass, Class<?> elementClass) throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		if (Data.isEmpty(json) || json.equals("{}"))
			return null;
		try (JsonReader jsonReader = Json.createReader(new StringReader(json))) {
			JsonArray jsonArray = jsonReader.readArray();
			JsonCollection anno = new JsonCollection() {
				@Override
				public Class<? extends Annotation> annotationType() { return JsonCollection.class; }
				@Override
				public Class<? extends Object> elementClass() { return elementClass; }
			};
			return (T)toObject(jsonArray, objClass, null, anno);
		}
	}
	
	/**
	 * Translate a JSON string into a Java object.  Use this method for maps.
	 * @param <T>
	 * @param json
	 * @param objClass
	 * @param keyClass
	 * @param valueClass
	 * @return
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws NoSuchMethodException
	 */
	@SuppressWarnings("unchecked")
	public static <T> T jsonToObj(String json, Class<T> objClass, Class<?> keyClass, Class<?> valueClass) throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		if (Data.isEmpty(json) || json.equals("{}"))
			return null;
		try (JsonReader jsonReader = Json.createReader(new StringReader(json))) {
			JsonObject jsonObject = jsonReader.readObject();
			JsonMap anno = new JsonMap() {
				@Override
				public Class<? extends Annotation> annotationType() { return JsonMap.class; }
				@Override
				public Class<? extends Object> keyClass() { return keyClass; }
				@Override
				public Class<? extends Object> valueClass() { return valueClass; }
			};
			return (T)toObject(jsonObject, objClass, anno, null);
		}
	}
	
	private static String unquote(String source) {
		return source.startsWith("\"") ? source.substring(1, source.length() - 1) : source;
	}
	
	private static Number toNumber(String source, Class<?> cls) {
		return 
			switch (cls.getSimpleName()) {
				case "BigDecimal" -> new BigDecimal(source);
				case "BigInteger" -> new BigInteger(source);
				case "Byte" -> Byte.valueOf(source);
				case "Double" -> Double.valueOf(source);
				case "Float" -> Float.valueOf(source);
				case "Integer" -> Integer.valueOf(source);
				case "Long" -> Long.valueOf(source);
				case "Short" -> Short.valueOf(source);
				default -> throw new IllegalArgumentException("Improper class provided.");
			};
	}
	
	private static Object toDate(String source, Class<?> cls) {
		String newsource = unquote(source);
		return 
			switch (cls.getCanonicalName()) {
				case "java.util.Date" -> Data.strToDate(newsource);
				case "java.sql.Date" -> java.sql.Date.valueOf(Data.strToLocalDate(newsource, JSON_DATETIME));
				case "java.sql.Time" -> java.sql.Time.valueOf(Data.strToLocalTime(newsource, JSON_DATETIME));
				case "java.sql.Timestamp" -> java.sql.Timestamp.valueOf(Data.strToLocalDateTime(newsource, JSON_DATETIME));
				case "java.time.LocalDate" -> Data.strToLocalDate(newsource, Data.JSON_DATETIME); // LocalDate.parse(newsource, DateTimeFormatter.ISO_LOCAL_DATE);
				case "java.time.LocalDateTime" -> Data.strToLocalDateTime(newsource, Data.JSON_DATETIME); // LocalDateTime.parse(newsource, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
				case "java.time.LocalTime" -> Data.strToLocalTime(newsource, Data.JSON_DATETIME); // LocalTime.parse(newsource, DateTimeFormatter.ISO_LOCAL_TIME);
				case "java.time.OffsetDateTime" -> OffsetDateTime.parse(newsource, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
				case "java.time.OffsetTime" -> OffsetTime.parse(newsource, DateTimeFormatter.ISO_OFFSET_TIME);
				case "java.time.ZonedDateTime" -> ZonedDateTime.parse(newsource, DateTimeFormatter.ISO_ZONED_DATE_TIME);
				default -> throw new IllegalArgumentException("Improper class provided.");
			};
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static Object toEnum(String source, Class<?> cls) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		JsonEnum je = cls.getAnnotation(JsonEnum.class);
		if (je != null) {
			Method valueMethod = cls.getMethod(je.valueMethod(), String.class);
			return valueMethod.invoke(cls, source);
		}
		return Enum.valueOf((Class<Enum>)cls, source);
	}
	
	private static Object toKey(String source, Class<?> cls) throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		Object dummy = Data.newInstance(cls);
		if (dummy instanceof java.lang.String)
			return source;
		else if (dummy instanceof java.lang.Number)
			return toNumber(source, cls);
		else if (dummy instanceof java.util.Date || dummy instanceof java.time.temporal.Temporal)
			return toDate(source, cls);
		else
			throw new IllegalArgumentException("Keys may only be a string, number, or date.");
	}
	
	private static boolean isNumber(Class<?> cls) {
		return cls.getSuperclass() == Number.class;
	}
	
	private static boolean isDate(Class<?> cls) {
		return java.util.Date.class.isAssignableFrom(cls) || Temporal.class.isAssignableFrom(cls);
		/*
		if (cls == java.util.Date.class || cls.getSuperclass() == java.util.Date.class)
			return true;
		Class<?>[] interfaces = cls.getInterfaces();
		for (Class<?> iface : interfaces)
			if (iface == Temporal.class)
				return true;
		return false;
		*/
	}
	
	private static boolean isEnum(Class<?> cls) {
		return Enum.class.isAssignableFrom(cls);
		//return cls.getSuperclass() == Enum.class;
	}
	
	private static boolean isCollection(Class<?> cls) {
		return Collection.class.isAssignableFrom(cls);
		
		/*
		Class<?> check = cls;
		while (check != null) {
			if (Data.in(java.util.Collection.class, check.getInterfaces()))
				return true;
			check = check.getSuperclass();
		}
		return false;
		*/
	}
	
	private static boolean isMap(Class<?> cls) {
		return Map.class.isAssignableFrom(cls);
		/*
		Class<?> check = cls;
		while (check != null) {
			if (Data.in(java.util.Map.class, check.getInterfaces()))
				return true;
			check = check.getSuperclass();
		}
		return false;
		*/
	}
	
	private static Object toObject(JsonValue source, Class<?> cls, JsonMap mapAnno, JsonCollection colAnno) throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		if (cls == String.class)
			return unquote(source.toString());
		if (cls == Boolean.class || cls == boolean.class)
			return source.getValueType().equals(JsonValue.ValueType.TRUE);
		if (isNumber(cls))
			return toNumber(source.toString(), cls);
		if (isDate(cls))
			return toDate(source.toString(), cls);
		if (isEnum(cls))
			return toEnum(unquote(source.toString()), cls);
		if (isMap(cls))
			return toMap(source.asJsonObject(), cls, mapAnno);
		if (isCollection(cls))
			return toCollection(source, cls, colAnno);
		if (cls.isArray() || source instanceof JsonArray)
			return toArray(source.asJsonArray(), cls);
		return toPojo(source.asJsonObject(), cls);
	}
	
	private static <K,V> Map<K,V> createMap(Class<K> ktype, Class<V> vtype) { return new LinkedHashMap<>(); }
	
	@SuppressWarnings("unchecked")
	private static Object toMap(JsonObject source, Class<?> cls, JsonMap def) throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		if (def == null)
			throw new IllegalArgumentException("Map setters must have a JsonMap annotation.");
		Map<Object, Object> result = (Map<Object, Object>)createMap(def.keyClass(), def.valueClass());
		for (Entry<String, JsonValue> entry : source.entrySet()) {
			Object key = toKey(entry.getKey(), def.keyClass());
			Object value = toObject(entry.getValue(), def.valueClass(), null, null);
			result.put(key, value);
		}
		return result;
	}
	
	private static <T> List<T> createList(Class<T> type) { return new ArrayList<>(); }
	
	@SuppressWarnings("unchecked")
	private static Object toCollection(JsonValue source, Class<?> cls, JsonCollection def) throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		if (def == null)
			throw new IllegalArgumentException("Collection setters must have a JsonCollection annotation.");
		Log.logDebug("Collection element type: %s".formatted(def.elementClass().getCanonicalName()), null);
		Collection<Object> result = (Collection<Object>)createList(def.elementClass());
		for (int i = 0; i < source.asJsonArray().size(); i++) {
			Object value = source.asJsonArray().get(i).toString().equals("null")
				 ? null
				 : def.elementClass() == String.class 
				 ? source.asJsonArray().getJsonString(i).getString() 
				 : def.elementClass() == Long.class 
				 ? source.asJsonArray().getJsonNumber(i).numberValue().longValue()
				 : def.elementClass() == Integer.class 
				 ? source.asJsonArray().getJsonNumber(i).numberValue().intValue()
				 : def.elementClass() == Short.class 
				 ? source.asJsonArray().getJsonNumber(i).numberValue().shortValue()
				 : def.elementClass() == Byte.class 
				 ? source.asJsonArray().getJsonNumber(i).numberValue().byteValue()
				 : def.elementClass() == Double.class 
				 ? source.asJsonArray().getJsonNumber(i).numberValue().doubleValue()
				 : def.elementClass() == Float.class 
				 ? source.asJsonArray().getJsonNumber(i).numberValue().floatValue()
				 : toObject(source.asJsonArray().getJsonObject(i), def.elementClass(), null, null);
			result.add(value);
		}
		return result;
	}
	
	private static Object toArray(JsonArray source, Class<?> cls) throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		Object result = Array.newInstance(cls.getComponentType(), source.asJsonArray().size());
		for (int i = 0; i < source.asJsonArray().size(); i++) {
			Object value = toObject(source.asJsonArray().get(i), cls.getComponentType(), null, null);
			Array.set(result, i, value);
		}
		return result;
	}
	
	private static Map<String, Method> getSetters(Class<?> cls) {
		Method[] methods = cls.getMethods();
		Map<String, Method> result = new HashMap<>();
		for (Method method : methods)
			if (method.getName().startsWith("set") && method.getParameterCount() == 1)
				result.put(method.getName(), method);
		return result;
	}
	
	private static Object toPojo(JsonObject source, Class<?> cls) throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		Class<?> clazz = cls;
		JsonExtensions ext = cls.getAnnotation(JsonExtensions.class);
		if (ext != null)
			for (JsonExtension x : ext.value()) {
				String field = x.field();
				String value = source.get(field) == null ? null : source.getString(field);
				if (x.values().length == 0 || Data.in(value, x.values())) {
					clazz = x.extensionClass();
					break;
				}
			}
		Object result = Data.newInstance(clazz);
		Map<String, Method> setters = getSetters(clazz);
		for (Entry<String, JsonValue> entry : source.entrySet()) {
			String setterName = Data.getSetterName(entry.getKey());
			Method setter = setters.get(setterName);
			if (setter == null) {
				Log.logWarning("The setter name '" + setterName + "' for object (" + clazz.getCanonicalName() + ") was not found.", null);
				continue;
			}
			Class<?> paramClass = setter.getParameterTypes()[0];
			setter.invoke(result, toObject(entry.getValue(), paramClass, setter.getAnnotation(JsonMap.class), setter.getAnnotation(JsonCollection.class)));
		}
		return result;
	}
}
