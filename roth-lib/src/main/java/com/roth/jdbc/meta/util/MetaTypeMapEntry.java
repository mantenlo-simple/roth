package com.roth.jdbc.meta.util;

import com.roth.jdbc.meta.model.Type;

import jdk.jfr.Experimental;

@Experimental
public class MetaTypeMapEntry {
	private Type metaType;
	private Long sizeMin;
	private Long sizeMax;
	private Byte precisionMin;
	private Byte precisionMax;
	private Class<?> javaClass;
	private String databaseType;

	public MetaTypeMapEntry(Type metaType, Long sizeMin, Long sizeMax, Byte precisionMin, Byte precisionMax, Class<?> javaClass, String databaseType) {
		this.metaType = metaType;
		this.sizeMin = sizeMin;
		this.sizeMax = sizeMax;
		this.precisionMin = precisionMin;
		this.precisionMax = precisionMax;
		this.javaClass = javaClass;
		this.databaseType = databaseType;
	}
	
	public Type getMetaType() { return metaType; }
	public void setMetaType(Type metaType) { this.metaType = metaType; }
	
	public Long getSizeMin() { return sizeMin; }
	public void setSizeMin(Long sizeMin) { this.sizeMin = sizeMin; }
	
	public Long getSizeMax() { return sizeMax; }
	public void setSizeMax(Long sizeMax) { this.sizeMax = sizeMax; }
	
	public Byte getPrecisionMin() { return precisionMin; }
	public void setPrecisionMin(Byte precisionMin) { this.precisionMin = precisionMin; }
	
	public Byte getPrecisionMax() { return precisionMax; }
	public void setPrecisionMax(Byte precisionMax) { this.precisionMax = precisionMax; }
	
	public Class<?> getJavaClass() { return javaClass; }
	public void setJavaClass(Class<?> javaClass) { this.javaClass = javaClass; }
	
	public String getDatabaseType() { return databaseType; }
	public void setDatabaseType(String databaseType) { this.databaseType = databaseType; }
}
