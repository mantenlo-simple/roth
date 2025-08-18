package com.roth.export.util;

import java.lang.reflect.InvocationTargetException;

public interface JsonFormatter {
	String toJson(boolean formatted) throws InvocationTargetException, IllegalAccessException;
}
