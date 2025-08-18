package com.roth.jdbc.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Map;

import com.roth.base.log.Log;
import com.roth.base.util.Data;
import com.roth.compiler.MemoryJavaCompiler;
import com.roth.jdbc.annotation.PermissiveBinding;

public final class BinderGenerator {
	/**
	 * This class is not instantiated.
	 */
	private BinderGenerator() { }
	
	/**
	 * Get the package name for the binder source file.
	 * @param objectClass
	 * @return
	 */
	public static String getBinderPackageName(Class<?> objectClass) {
		return objectClass.getPackage().getName().replace(".model", ".binder");
	}
	
	/**
	 * Get the binder class name. 
	 * @param objectClass
	 * @param metaData
	 * @return
	 * @throws SQLException
	 */
	public static String getBinderClassName(Class<?> objectClass, ResultSetMetaData metaData) throws SQLException {
		String hash = getMetaDataHash(metaData);
		return "%s.%sBinder%s".formatted(getBinderPackageName(objectClass), objectClass.getSimpleName(), hash);
	}
	
	/**
	 * Get the binder class source file path and name.
	 * @param objectClass
	 * @param metaData
	 * @return
	 * @throws SQLException
	 */
	public static String getBinderFilePath(Class<?> objectClass, ResultSetMetaData metaData) throws SQLException {
		return "%s.java".formatted(getBinderClassName(objectClass, metaData).replace(".", "/"));
	}
	
	/**
	 * Get the binder class simple name.
	 * @param objectClass
	 * @param metaData
	 * @return
	 * @throws SQLException
	 */
	public static String getBinderSimpleClassName(Class<?> objectClass, ResultSetMetaData metaData) throws SQLException {
		String result = getBinderClassName(objectClass, metaData);
		return result.substring(result.lastIndexOf('.') + 1);
	}
	
	
	/**
	 * Get the condition to check the getter if it is null.  For some bindings, if this
	 * is not checked, then the calling action will generate a NullPointerException.
	 * @param i
	 * @return
	 */
	private static String getIfObjectNotNull(int i) {
		return """
					if (resultSet.getObject(%d) != null)
			""".formatted(i);
	}
	
	/**
	 * Get the source that calls the setter, and supplies the result called by the getter.
	 * @param setter
	 * @param getter
	 * @return
	 */
	private static String getBeanSetter(String setter, String getter) {
		return getBeanSetter(setter, "", getter);
				/*""" 
					bean.%s(resultSet.%s);
			""".formatted(setter, getter);*/
	}
	
	/**
	 * Get the source that calls the setter, and supplies the result called by the getter.
	 * @param setter
	 * @param getter
	 * @param prefix
	 * @return
	 */
	private static String getBeanSetter(String setter, String prefix, String getter) {
		return """
				bean.%s(%sresultSet.%s);
			""".formatted(setter, prefix, getter);
	}
	
	/**
	 * Get the source that calls the setter, and supplies the result called by the getter.
	 * @param setter
	 * @param getter
	 * @return
	 */
	private static String getBeanSetterEnum(String setter, String getter, String simpleName) {
		return """
				bean.%s(%s.valueOf(resultSet.%s));
			""".formatted(setter, simpleName, getter);
	}
	
	private static final String[] primitives = {"boolean", "byte", "double", "float", "int", "long", "short"};
	
	
	private static String getIfNotNullBind(String setter, String prefix, String getterTemplate, int i) {
		return "%s\t\t%s".formatted(getIfObjectNotNull(i), getBeanSetter(setter, prefix, getterTemplate.formatted(i)));
	}
	
	
	private static final Map<Object, String> BIND_TEMPLATES;
	static {
		BIND_TEMPLATES = Map.of(
			"java.util.Date", "new java.util.Date(|getTimestamp(%d).getTime())",
			"java.time.LocalDate", "|getTimestamp(%d).toLocalDateTime().toLocalDate()",
			"java.time.LocalDateTime", "|getTimestamp(%d).toLocalDateTime()",
			"java.time.LocalTime", "|getTimestamp(%d).toLocalDateTime().toLocalTime()"
		);
	}
	
	/**
	 * Generate the bind code for a result set column.
	 * @param i
	 * @param setter
	 * @param paramClass
	 * @param suppressBinary
	 * @return
	 */
	private static String generateBind(int i, String setter, Class<?> paramClass, boolean suppressBinary) {
		String cName = paramClass.getCanonicalName().replace("[]", "");
		String getter = null;
		// Primitives
		if (Data.in(cName, primitives) && !paramClass.isArray())
			getter = "get%s(%d)".formatted(Data.upcaseFirst(cName), i);
		// Byte Array
		else if (cName.equals("byte") && paramClass.isArray() && !suppressBinary)
			getter = "getBytes(%d)".formatted(i);
		if (getter != null)
			return getBeanSetter(setter, getter);
		// Enum Types
		if (paramClass.isEnum())
			return "%s\t\t%s".formatted(getIfObjectNotNull(i), getBeanSetterEnum(setter, "getString(%d)".formatted(i), paramClass.getSimpleName()));
		// Various Date Types; see BIND_TEMPLATES; this may expand past date types.
		String template = BIND_TEMPLATES.get(cName);
		if (template != null) {
			String[] t = template.split("\\|");
			return getIfNotNullBind(setter, t[0], t[1], i); 
		}
		// 
		return getIfNotNullBind(setter, "", "%s(%d)".formatted(getDataSourceColumn(cName), i), i);
	}
	
	/**
	 * Get the package clause for the source.
	 * @param packageName
	 * @return
	 */
	private static String getPackage(String packageName) {
		return """
			package %s;
			
			""".formatted(packageName);
	}
	
	/**
	 * Get the import line for the given canonical class name.
	 * @param className
	 * @return
	 */
	private static String getImport(String className) {
		return """
			import %s;
			""".formatted(className);
	}
	
	/**
	 * Get the start of the class declaration.  This includes the method start for the bindObject method.
	 * @param className
	 * @param metaDataHash
	 * @return
	 */
	private static String getClassStart(String className, String metaDataHash) {
		return 
			"""
			public class %sBinder%s implements Binder {
				@Override
				public void bindObject(Object object, ResultSet resultSet) throws SQLException {
					%s bean = (%s) object;
			""".formatted(className, metaDataHash, className, className);
	}
	
	/**  
	 * Get the end of the class declaration.
	 * @return
	 */
	private static String getClassEnd() {
		return 
			"""
				}
			}
			""";
	}
	
	/**
	 * Generate the source code for a binder class for the given object class.
	 * @param objectClass
	 * @param metaData
	 * @param suppressBinary
	 * @return
	 * @throws SQLException
	 */
	public static String generateCode(Class<?> objectClass, ResultSetMetaData metaData, boolean suppressBinary) throws SQLException {
		String codePackage = getPackage(objectClass.getPackage().getName().replace(".model", ".binder"));
		StringBuilder codeImports = new StringBuilder(
			"""
			%s%s
			%s%s
			""".formatted(getImport("java.sql.ResultSet"),
						  getImport("java.sql.SQLException"),
						  getImport("com.roth.jdbc.util.Binder"),
						  getImport(objectClass.getCanonicalName()))); 
		StringBuilder codeClass = new StringBuilder(getClassStart(objectClass.getSimpleName(), getMetaDataHash(metaData)));
		boolean permissiveBinding = objectClass.isAnnotationPresent(PermissiveBinding.class);
		
		boolean warning = false;
		for (int i = 1; i <= metaData.getColumnCount(); i++) {
			String columnLabel = metaData.getColumnLabel(i).toLowerCase();
			String setter = Data.getSetterName(columnLabel);
			Class<?> paramClass = getSetterClassName(objectClass, setter);
			if (paramClass == null) {
				String message = "No field setter was found for column label '" + columnLabel + "' in class '" + objectClass.getCanonicalName() + "'.";
				if (!permissiveBinding)
					throw new SQLException(message);
				warning = true;
				codeClass.append("""
							Log.logWarning("%s", null);	
					""".formatted(message));
			}
			else {
				codeClass.append(generateBind(i, setter, paramClass, suppressBinary));
				if (paramClass.isEnum())
					codeImports.append("""
							%s
							""".formatted(getImport(paramClass.getCanonicalName())));
			}
		}
		codeClass.append(getClassEnd());
		
		if (warning)
			codeImports.append("""
					%s
					""".formatted(getImport("com.roth.base.log.Log")));
		
		String result = codePackage + codeImports.toString() + codeClass.toString();
		Log.logDebug(result, null, "generateCode");
		return result;
	}
	
	/**
	 * Get the result set getter name for the given canonical class name.
	 * @param cName
	 * @return
	 */
	private static String getDataSourceColumn(String cName) {
		return switch(cName) {
			case "java.lang.Boolean" -> "getBoolean";
			case "java.lang.Byte" -> "getByte";
			case "java.lang.Double" -> "getDouble";
			case "java.lang.Float" -> "getFloat";
			case "java.lang.Integer" -> "getInt";
			case "java.lang.Long" -> "getLong";
			case "java.lang.Short" -> "getShort";
			case "java.lang.String" -> "getString";
			case "java.math.BigDecimal" -> "getBigDecimal";
			case "java.sql.Date" -> "getDate";
			case "java.sql.Time" -> "getTime";
		    case "java.sql.Timestamp" -> "getTimeStamp";
		    default -> "getObject";
		};
	}
	
	/**
	 * Get the class name for the parameter expected by the model setter method.
	 * @param objectClass
	 * @param setter
	 * @return
	 */
	private static Class<?> getSetterClassName(Class<?> objectClass, String setter) {
		try {
			Method method = null;
			for (Method m : objectClass.getMethods())
				if (setter.equalsIgnoreCase(m.getName()) && !m.isBridge()) {
					method = m;
					break;
				}
			
			if (method == null)
				return null;
			
			method.setAccessible(true);
			return method.getParameterTypes()[0];
		}
		catch (Exception e) {
			Log.logWarning("Setter method (" + setter + ") not found in " + objectClass.getCanonicalName(), null);
			return null;
		}
	}
	
	/**
	 * Get a hash value for the supplied meta data.  The reason for this is to differentiate between
	 * model classes that have the same name, but are used for different result sets.
	 * @param metaData
	 * @return
	 * @throws SQLException
	 */
	public static String getMetaDataHash(ResultSetMetaData metaData) throws SQLException {
		StringBuilder hashSource = new StringBuilder(Data.integerToStr(metaData.getColumnCount()));
		for (int i = 1; i <= metaData.getColumnCount(); i++)
			hashSource.append("." + metaData.getColumnName(i));
		return Data.toHexString(hashSource.toString().hashCode(), 8);
	}
	
	/**
	 * Get the physical location of the class path for a given class.  If the class path belongs to a class 
	 * in a library, it will return the full jar file path and name.  If the class path belongs to a class 
	 * in a web application, it will return the path to the base folder of the compiled classes in the web 
	 * application.
	 * @param objectClass
	 * @return
	 */
	public static String getClassPath(Class<?> objectClass) {
		String result = null;
		try { result = new File(objectClass.getProtectionDomain().getCodeSource().getLocation().toURI()).getPath().replace("\\", "/"); }
		catch (Exception e) { /* Eat it */ }
		if (result == null)
			try { result = objectClass.getClassLoader().getResource("/").toString().replace("file:/", ""); }
		catch (Exception e) { /* Eat it */ }
		return result;
	}
	
	/**
	 * Compile and return a binder class based on the given source code and object class.   
	 * @param className
	 * @param sourceCode
	 * @param objectClass
	 * @return
	 * @throws Exception
	 */
	public static Class<? extends Binder> getCompiledBinderClass(String className, String sourceCode, Class<?> objectClass) throws Exception {
		String libraryPath = new File(Binder.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getPath().replace("\\", "/");
		int pos = libraryPath.lastIndexOf('/');
		String coyotePath = libraryPath.substring(0, pos) + "/tomcat-coyote.jar";
		String jspApiPath = libraryPath.substring(0, pos) + "/jsp-api.jar";
		String objectPath = getClassPath(objectClass);
		String separator = System.getProperty("os.name").contains("Windows") ? ";" : ":";
        String classpath = libraryPath + separator + coyotePath + separator + jspApiPath + separator + objectPath;
		MemoryJavaCompiler compiler = MemoryJavaCompiler.newInstance();
		compiler.useParentClassLoader(objectClass.getClassLoader());
		compiler.useOptions("-proc:full", "-classpath", classpath);
		Class<?> compiledClass = compiler.compile(className, sourceCode);
        return compiledClass.asSubclass(Binder.class);
	}
	
	
	public static void generateToFile(Class<?> objectClass, ResultSetMetaData metaData, boolean suppressBinary) throws SQLException, IOException {
		String rothTemp = (String)Data.getWebEnv("rothTemp");
		String binderPath = rothTemp + getBinderFilePath(objectClass, metaData);
		
		File directory = new File(binderPath.substring(0, binderPath.lastIndexOf("/")));
		if (!directory.exists())
			directory.mkdirs();
		
		if (!new File(binderPath).exists())
			try (BufferedWriter writer = new BufferedWriter(new FileWriter(binderPath))) {
				writer.write(BinderGenerator.generateCode(objectClass, metaData, suppressBinary));
			}
	}
}
