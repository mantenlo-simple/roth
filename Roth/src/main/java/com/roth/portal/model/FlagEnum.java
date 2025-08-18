package com.roth.portal.model;

public enum FlagEnum {
	N,
	Y;
	
	public boolean bool() { return this == Y; }
}
