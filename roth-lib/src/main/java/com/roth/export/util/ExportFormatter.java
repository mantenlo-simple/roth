package com.roth.export.util;

import java.io.OutputStream;
import java.util.Collection;

public interface ExportFormatter {
	public void formatData(Collection<?> data, OutputStream output); 
}
