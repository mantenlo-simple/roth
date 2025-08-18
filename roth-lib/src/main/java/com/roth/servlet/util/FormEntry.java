package com.roth.servlet.util;

import java.io.Serializable;
import java.util.Arrays;

public class FormEntry implements Serializable {
	private static final long serialVersionUID = -4594938853985515250L;
	
	private InputEntry[] inputs;
	private String manualJsp;
	
	public InputEntry[] getInputs() { return inputs; }
	public void setInputs(InputEntry[] inputs) { this.inputs = inputs; }

	public String getManualJsp() { return manualJsp; }
	public void setManualJsp(String manualJsp) { this.manualJsp = manualJsp; }
	
	@Override
	public boolean equals(Object o) {
		if (this == o) 
			return true;
        if (o == null || getClass() != o.getClass()) 
        	return false;
        FormEntry entry = (FormEntry)o;
        return Arrays.equals(inputs, entry.inputs);
	}
	
	@Override
    public int hashCode() {
        return Arrays.hashCode(inputs);
    }

	@Override
    public String toString() {
        return String.format("FormEntry{inputs=%s}", 
        	   Arrays.toString(inputs));
    }
}