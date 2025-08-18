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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.roth.base.log.Log;

/**
 * 
 * @author James M. Payne
 *
 */
public class GenericComparator extends AbstractSortComparator {
	private static final long serialVersionUID = 6620799386729941769L;

	public GenericComparator(String sortColumn, int sortOrder) { super(sortColumn, sortOrder); }
	
	private Method getDeclaredMethod(Object object, String name) throws NoSuchMethodException {
		Method method = null;
		Class<?> clazz = object.getClass();
		
		do { try { method = clazz.getDeclaredMethod(name); } catch (Exception e) { }
		} while (method == null & (clazz = clazz.getSuperclass()) != null);
	
		if (method == null) throw new NoSuchMethodException();
		return method;
	}
	
	private Object invokeMethod(Object object, String methodName, Object[] arguments) throws NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		Method method = getDeclaredMethod(object, methodName);
		method.setAccessible(true);
		return method.invoke(object, arguments);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public int compare(Object arg0, Object arg1) {
		if (getSortColumn() == null)
			return 0;
		String method = "get" + getSortColumn().substring(0, 1).toUpperCase() + getSortColumn().substring(1);
		Object a = null;
		Object b = null;
		
		try { 
			a = invokeMethod(arg0, method, null);
			b = invokeMethod(arg1, method, null);
		} 
		catch (Exception e) {
			Log.logError("--> RothLib Error: GenericComparator unable to invoke method \"" + method + "\" in class \"" + arg0.getClass().getSimpleName() + "\".", "<unavailable>", e);
		}
		
		int result = 0;
		
        if ((a != null) && (b != null)) {
            if (a instanceof String)
                result = ((String)a).compareToIgnoreCase((String)b);
            else if (a instanceof Comparable)
                result = ((Comparable)a).compareTo((Comparable)b);
        }
        else
            result = Integer.valueOf((a == null) ? 0 : 1).compareTo((b == null) ? 0 : 1);
        
		return result * getSortOrder();
	}
}