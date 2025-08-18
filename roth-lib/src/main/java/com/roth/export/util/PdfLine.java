package com.roth.export.util;

import java.awt.Color;

import com.roth.base.util.Data;

public class PdfLine {
	private Color color;
	private float[] pattern;
	private float phase;
	private float width;
	
	public PdfLine() { init(null, null); }
	public PdfLine(Color color) { init(color, null); }
	public PdfLine(Color color, float width) { init(color, width); }
	
	protected void init(Color color, Float width) {
		this.color = color != null ? color : Color.BLACK;
		this.width = Data.nvl(width, 1f);
		pattern = new float[0];
	}
	
	public Color getColor() { return color; }
	public void setColor(Color color) { this.color = color; }
	
	public float[] getPattern() { return pattern; }
	public void setPattern(float[] pattern) { this.pattern = pattern; }
	
	public float getPhase() { return phase; }
	public void setPhase(float phase) { this.phase = phase; }
	
	public float getWidth() { return width; }
	public void setWidth(float width) { this.width = width; }
}
