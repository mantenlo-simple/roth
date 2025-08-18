package com.roth.compiler;

import java.util.HashMap;
import java.util.Map;

public class ExtClassLoader extends ClassLoader {

	private Map<String, CompiledCode> customCompiledCode = new HashMap<>();

	public ExtClassLoader(ClassLoader parent) { super(parent); }

	public void addCode(CompiledCode compiledCode) { customCompiledCode.put(compiledCode.getName(), compiledCode); }

	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException {
		CompiledCode compiledCode = customCompiledCode.get(name);
		if (compiledCode == null) {
			return super.findClass(name);
		}
		byte[] byteCode = compiledCode.getByteCode();
		return defineClass(name, byteCode, 0, byteCode.length);
	}
}
