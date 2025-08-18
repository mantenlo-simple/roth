package com.roth.jdbc.util;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

import com.roth.base.log.Log;
import com.roth.base.log.LogLevel;
import com.roth.base.util.Data;
import com.roth.jdbc.annotation.ConnectionParams;
import com.roth.jdbc.annotation.JdbcTable;
import com.roth.jdbc.annotation.NoTest;
import com.roth.jdbc.meta.model.ColumnInfoBean;
import com.roth.jdbc.meta.model.IndexInfoBean;
import com.roth.jdbc.meta.model.TableInfoBean;
import com.roth.jdbc.meta.util.DataType;
import com.roth.jdbc.meta.util.DataTypeMap;
import com.roth.jdbc.meta.util.MetaUtil;
import com.roth.tags.el.Util;

public class JdbcTest {
	private MetaUtil meta;
	private DataTypeMap typeMap;
	private Map<String,TableInfoBean> tableMap;
	private LogLevel outputLevel = LogLevel.WARNING;
	private Map<String,Method> methodMap;
	private List<String> overloads;
	
	/**
	 * Constructor given an instantiated MetaUtil and a schema.  This is for
	 * JUnit5 tests were a singleton extension of MetaUtil that uses a 
	 * @ConnectionParams annotation for connection to the database.
	 * @param meta
	 * @param schema
	 * @throws SQLException
	 */
	public JdbcTest(MetaUtil meta, String schema) throws SQLException {
		this.meta = meta;
		typeMap = DataType.getTypeMap(meta.getDbName());
		List<TableInfoBean> tables = meta.getTables(schema);
		tableMap = new HashMap<>();
		if (!Data.isEmpty(tables))
			for (TableInfoBean table : tables)
				tableMap.put(table.getTableName(), table);
	}
	
	/**
	 * Constructor given a jndiName and a schema.  This is for runtime use as it 
	 * requires a container with JNDI connection pools.
	 * @param jndiName
	 * @param schema
	 * @throws SQLException
	 */
	public JdbcTest(String jndiName, String schema) throws SQLException {
		this(new MetaUtil(jndiName), schema);
	}
	
	/**
	 * Set the output levels using the Log.LOG_ERROR, Log.LOG_WARNING, or 
	 * Log.LOG_INFO values (the others are not applicable).  The default is
	 * Log.LOG_WARNING.
	 * @param outputLevel
	 */
	public void setOutputLevel(LogLevel outputLevel) { this.outputLevel = outputLevel; }
	
	public static Set<String> getClassNamesFromJarFile(File givenFile) throws IOException {
	    Set<String> classNames = new HashSet<>();
	    try (JarFile jarFile = new JarFile(givenFile)) {
	        Enumeration<JarEntry> e = jarFile.entries();
	        while (e.hasMoreElements()) {
	            JarEntry jarEntry = e.nextElement();
	            if (jarEntry.getName().endsWith(".class")) {
	                String className = jarEntry.getName()
	                  .replace("/", ".")
	                  .replace(".class", "");
	                classNames.add(className);
	            }
	        }
	        return classNames;
	    }
	}
	
	/**
	 * List all class files found within the named directory (recursively).
	 * @param directoryName
	 * @param files
	 * @throws ClassNotFoundException 
	 */
	private List<Class<?>> findClassesInPath(String directoryName, String packageName) throws ClassNotFoundException {
	    File directory = new File(directoryName);
	    File[] fileList = directory.listFiles();
	    List<Class<?>> result = new ArrayList<>();
	    if(fileList != null)
	        for (File file : fileList) {      
	        	String path = file.getPath().replace("\\", "/");
	            if (file.isFile() && path.contains(".class")) {
	            	int bpos = path.indexOf(packageName);
					int cpos = path.indexOf(".class");
					result.add(Class.forName(path.substring(bpos, cpos).replace("/", ".")));
	            }
	            else if (file.isDirectory())
	            	result.addAll(findClassesInPath(file.getAbsolutePath(), packageName));
	        }
	    return result;
	}
	
	private List<Class<?>> findAllClassesUsingClassLoader(String packageName, String filter) throws IOException, ClassNotFoundException {
        List<URL> list = Collections.list(this.getClass().getClassLoader().getResources(packageName));
        String classPath = null;
        // The packageName alone can pull up four or more paths that are different stages of source 
        // code, build folder's classes, packaged JAR and source JAR.  Look for one that matches the 
        // filter (for JUnit, this should be the one that identifies the build folder's path).
        for (URL u : list)
        	if (u.toString().contains(filter))
        		classPath = u.toString().replace("file:", "");
       	return classPath == null ? new ArrayList<>() : findClassesInPath(classPath, packageName);
    }
 
	private List<Class<?>> getJdbcModels(String basePackage, String filter, ClassLoader classLoader) throws IOException, ClassNotFoundException {
		return findAllClassesUsingClassLoader(basePackage, filter)
			.stream()
			.filter(cls -> cls.getAnnotation(JdbcTable.class) != null)
			.sorted((p1,p2) -> p1.getCanonicalName().compareTo(p2.getCanonicalName()))
			.collect(Collectors.toList());
	}
	
	private List<Class<?>> getJdbcUtilDescendents(String basePackage, String filter, ClassLoader classLoader) throws IOException, ClassNotFoundException {
		return findAllClassesUsingClassLoader(basePackage, filter)
			.stream()
			.filter(cls -> JdbcUtil.class.isAssignableFrom(cls) && cls.getAnnotation(NoTest.class) == null)
			.sorted((p1,p2) -> p1.getCanonicalName().compareTo(p2.getCanonicalName()))
			.collect(Collectors.toList());
	}
	
	private static final String SPACING = "    ";
	private static final String ERROR_MISSING = SPACING + "ERROR: Missing method [ %s ]";
	private static final String ERROR_PARAMETERS = SPACING + "ERROR: Invalid number of parameters in method [ %s ]  Found [ %d ] | Expected: [ %d ]";
	private static final String ERROR_INCOMPATIBLE = SPACING + "ERROR: Incompatible type in method [ %s ]  Found: [ %s ] | Expected: [ %s ]";
	private static final String ERROR_OVERLOADED = SPACING + "ERROR: Overloaded method [ %s ]  Metadata-related methods may not be overloaded.";
	private static final String WARN_NOT_OPTIMAL = SPACING + "WARNING: Suboptimal type in method [ %s ]  Found: [ %s ] | Expected: [ %s ]";
	private static final String INFO_CORRECT = SPACING + "INFO: Valid method signature confirmed [ %s ]";
	
	private String checkColumn(Class<?> cls, String methodName, String type) {
		boolean getter = methodName.startsWith("get");
		int paramsExpected = getter ? 0 : 1;
		Method method = methodMap.get(methodName);
		String infoOffset = outputLevel.ordinal() >= LogLevel.INFO.ordinal() ? "    " : "";
		String overload = Data.in(methodName, overloads) 
				        ? "\n" + infoOffset + String.format(ERROR_OVERLOADED, methodName) 
				        : "";
		
		if (method == null)
			return String.format(infoOffset + ERROR_MISSING, methodName) + overload;
		
		if (getter && method.getParameterCount() != paramsExpected) 
			return String.format(infoOffset + ERROR_PARAMETERS, method.getName(), method.getParameterCount(), paramsExpected) + overload;
		
		String metaType = (type.contains("(") ? type.substring(0, type.indexOf("(")) : type).toUpperCase();
		DataType dataType = typeMap.fromDbmsType(metaType.replace("AUTO_INCREMENT", "").trim(), 0, 0);
		Class<?> methodType = getter ? method.getReturnType() : method.getParameterTypes()[0];
		
		if (!dataType.isCompatible(methodType))
			return String.format(infoOffset + ERROR_INCOMPATIBLE, method.getName(), methodType.getCanonicalName(), dataType.getTypeClass().getCanonicalName()) + overload;
		
		if (!dataType.getTypeClass().equals(methodType))
			return String.format(infoOffset + WARN_NOT_OPTIMAL, method.getName(), methodType.getCanonicalName(), dataType.getTypeClass().getCanonicalName()) + overload;
		
		return String.format(infoOffset + INFO_CORRECT, method.getName()) + overload;
	}
	
	/**
	 * Map the methods for a given class keyed by name.  This map is used to validate whether the class 
	 * contains getters and setters for every column in the table defined in the @JdbcTable.name on the 
	 * class.
	 * @param source
	 * @return
	 */
	private void mapModelMethods(Class<?> source) {
		methodMap = new HashMap<>();
		overloads = new ArrayList<>();
		Method[] methods = source.getMethods();
		for (Method method : methods) {
			boolean getter = method.getName().startsWith("get");
			boolean setter = method.getName().startsWith("set");
			if (!getter && !setter)
				continue;
			if (methodMap.get(method.getName()) != null)
				overloads.add(method.getName());
			methodMap.put(method.getName(), method);
		}
	}
	
	/**
	 * Validate that the primary key defined in the @JdbcTable annotation matches the table's schema.
	 * Note that @JdbcTable.schema is optional, so it's not a failure if it's not set.
	 * @param modelSchema
	 * @param metaSchema
	 * @return
	 */
	private String validateSchema(String modelSchema, String metaSchema) {
		String[] messages = {
			"INFO: No schema specified.  Expected [ %s%s ].  Ensure schema selection is done in the data source definition.",
			"ERROR: Invalid schema specified.  Found: [ %s ] | Expected: [ %s ]",
			"INFO: Valid schema specification confirmed."
		};
		int msgidx = Data.isEmpty(modelSchema) ? 0 : !modelSchema.equalsIgnoreCase(metaSchema) ? 1 : 2;
		return "    " + String.format(messages[msgidx], Data.nvl(modelSchema), metaSchema); 
	}
	
	/**
	 * Validate that the primary key defined in the @JdbcTable annotation matches the primary key defined
	 * in the table's meta data.
	 * @param modelKey
	 * @param indexes
	 * @return
	 */
	private String validatePrimaryKey(String[] modelKey, List<IndexInfoBean> indexes) {
		String[] messages = {
			"INFO: Valid primary key definition confirmed.",
			"ERROR: Invalid primary key definition.  Found: [ %s ] | Expected: [ %s ]"
		};
		String[] metaKey = null;
		for (IndexInfoBean index : indexes)
			if (index.getPrimaryKey().equals("Y"))
				metaKey = index.getColumns().replace(" ", "").split(",");
		if (modelKey != null)
			Arrays.sort(modelKey);
		if (metaKey != null)
			Arrays.sort(metaKey);
		String modkey = modelKey == null ? "" : Data.join(modelKey, ",");
		String metkey = metaKey == null ? "" : Data.join(metaKey, ",");
		boolean matches = modkey.equalsIgnoreCase(metkey);
		int msgidx = matches ? 0 : 1;
		return "    " + String.format(messages[msgidx], modkey, metkey);
	}
	
	/**
	 * Print a line to the message.  The content of the line is examined to determine log level.<br/>
	 * ERROR = -1<br/>
	 * WARNING = 0<br/>
	 * All else = 1<br/>
	 * The lesser of pLevel and the derived level is returned.
	 * @param line
	 * @param message
	 * @param pLevel
	 * @return
	 */
	private int println(String line, StringBuilder message, Integer pLevel) {
		int olevel = line.contains("ERROR") ? -1 : line.contains("WARNING") ? 0 : line.contains("INFO") ? 1 : -1;
		if (olevel + 2 <= outputLevel.ordinal())
			message.append(line + "\n");
		return Util.lesserOf(Data.nvl(pLevel, 1), olevel);
	}
	
	public int testModels(String basePackage, String filter, ClassLoader classLoader) throws IOException, SQLException, ClassNotFoundException {
		int result = 1;
		List<Class<?>> classes = getJdbcModels(basePackage, filter, classLoader);
		
		Log.println(String.format("Classes found containing a @JdbcTable annotation: [ %d ]", classes == null ? -1 : classes.size()));
		if (!Data.isEmpty(classes))
			for (Class<?> cls : classes) {
				mapModelMethods(cls);
				JdbcTable jtbl = cls.getAnnotation(JdbcTable.class);
				StringBuilder message = new StringBuilder();
				println(String.format("Validating class [ %s ] | @JdbcTable.name [ %s ]", cls.getCanonicalName(), jtbl.name()), message, null);
				
				try {
					TableInfoBean table = tableMap.get(jtbl.name());
					if (table == null)
						result = println(String.format("    ERROR: Table [ %s ] does not exist in the data source.", jtbl.name()), message, result);
					else {
						result = println(validateSchema(jtbl.schema(), table.getSchema()), message, result);
						result = println(validatePrimaryKey(jtbl.primaryKeyColumns(), meta.getIndexes(table.getTableId())), message, result);
						List<ColumnInfoBean> columns = meta.getColumns(table.getTableId());
						for (ColumnInfoBean column : columns) {
							println(String.format("    INFO: Checking column [ %s ]", column.getColumnName()), message, null);
							result = println(checkColumn(cls, Data.getGetterName(column.getColumnName()), column.getColumnType()), message, result);
							result = println(checkColumn(cls, Data.getSetterName(column.getColumnName()), column.getColumnType()), message, result);
						}
					}
					Log.print(message.toString());
				}
				catch (SQLException e) {
					Log.logException(e, null);
				}
			}
		return result;
	}
	
	private static String interpretModifiers(int modifiers) {
		String scope = (modifiers & Modifier.PUBLIC) == Modifier.PUBLIC ? "public" 
				     : (modifiers & Modifier.PROTECTED) == Modifier.PROTECTED ? "protected"
				     : (modifiers & Modifier.PRIVATE) == Modifier.PRIVATE ? "private"
				     : "package private";
		String stat = (modifiers & Modifier.STATIC) == Modifier.STATIC ? "static" : "";
		String sync = (modifiers & Modifier.SYNCHRONIZED) == Modifier.SYNCHRONIZED ? "synchronized" : "";
		String abst = (modifiers & Modifier.ABSTRACT) == Modifier.ABSTRACT ? "synchronized" : "";
		return String.format("%s %s %s %s", scope, stat, sync, abst);
	}
	
	public int testUtils(String basePackage, String filter, ClassLoader classLoader) throws IOException, SQLException, ClassNotFoundException {
		int result = 1;
		List<Class<?>> classes = getJdbcUtilDescendents(basePackage, filter, classLoader);
		JdbcUtil.initTestMode(meta.getClass().getAnnotation(ConnectionParams.class));
		
		Log.println(String.format("Testable JdbcUtil descendant classes found: %d", classes == null ? -1 : classes.size()));
		if (!Data.isEmpty(classes))
			for (Class<?> cls : classes) {
				Log.println(String.format("Class: [ %s ]", cls.getCanonicalName()));
				Method[] methods = cls.getDeclaredMethods();
				try {
					Constructor<?>[] constructors = cls.getDeclaredConstructors();
					for (Constructor<?> constructor : constructors) { 
						
						Log.println(String.format("  Constructor [ %s | %s ] has [ %d ] parameters.", constructor.getName(), interpretModifiers(constructor.getModifiers()), constructor.getParameterCount()));
						
						Parameter[] params = constructor.getParameters();
						for (Parameter param : params) 
							Log.println(String.format("    Param: [ %s ] | Type: [ %s ]", param.getName(), param.getType().getCanonicalName()));
					}
					
					// JdbcUtil util = (JdbcUtil)Data.newInstance(cls);
					
					
					for (Method method : methods) {
						boolean isPublic = (method.getModifiers() & Modifier.PUBLIC) != 0;
						if (!isPublic || method.getAnnotation(NoTest.class) != null)
							continue;
						
					
						// OK... How on earth do I handle all parameter types?
						// Primitives and atomic class types are all well and good,
						// and maybe even arrays of those same types, but what do I 
						// do with maps or collections?
						
						// Maybe default the primitives and atomics, and look for 
						// annotations that hit the element types of maps and collections
						// similar to the JsonMap and JsonCollection annotations.
						// Print warning to the report if one doesn't exist.
						/*
						cls.getCon
						
						Parameter[] params = method.getParameters();
						params[0].getType();
						
						method.invoke(classes, methods)
						*/
					}
				} catch (Exception /*InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException*/ e) {
					Log.logException(e, null);
				}
				
			}
		
		return result;
	}
}
