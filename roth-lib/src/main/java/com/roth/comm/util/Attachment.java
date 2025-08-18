package com.roth.comm.util;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;

import com.roth.base.util.Data;

public class Attachment implements Serializable, Closeable {
	private static final long serialVersionUID = 5383373720435060291L;

	private String filename;
	private String tempname;
	
	public Attachment(String filename, InputStream input) throws IOException {
		this.filename = filename;
		setFile(input);
	}

	public String getFilename() { return filename; }
	public void setFilename(String filename) { this.filename = filename; }
	
	private String getTempPath() {
		return Data.nvl(Data.getContextEnv(Data.ROTH_TEMP), "/temp");
	}
	
	private void setFile(InputStream input) throws IOException {
		tempname = Data.writeUuidFile(getTempPath(), input);
	}
	
	public void getFile(OutputStream output) throws IOException {
		if (tempname == null)
			throw new IllegalStateException("No file is available.");
		Data.readTempFile(tempname, output);
	}
	
	@Override
	public void close() throws IOException {
		Path path = Path.of(String.format("%s/%s", getTempPath().replaceAll("/$", ""), tempname));
		Files.deleteIfExists(path);
	}
}
