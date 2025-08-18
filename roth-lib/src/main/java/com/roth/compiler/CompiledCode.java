package com.roth.compiler;

import javax.tools.SimpleJavaFileObject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;

public class CompiledCode extends SimpleJavaFileObject {
    private ByteArrayOutputStream output = new ByteArrayOutputStream();
    private String className;

    public CompiledCode(String className) throws Exception {
        super(new URI(className), Kind.CLASS);
        this.className = className;
    }
    
    @Override
    public OutputStream openOutputStream() throws IOException { return output; }

    public String getClassName() { return className; }
    public byte[] getByteCode() { return output.toByteArray(); }
}
