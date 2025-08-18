package com.roth.compiler;

import javax.tools.SimpleJavaFileObject;
import java.io.IOException;
import java.net.URI;

public class SourceCode extends SimpleJavaFileObject {
	private String code;
	private String className;

	public SourceCode(String className, String code) throws Exception {
		super(URI.create("string:///" + className.replace('.', '/') + Kind.SOURCE.extension), Kind.SOURCE);
		this.code = code;
		this.className = className;
	}

	public String getClassName() { return className; }
	public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException { return code; }
}
