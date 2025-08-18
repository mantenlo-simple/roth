package com.roth.export.util;

import java.awt.Color;

import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import com.roth.base.util.Data;

public class PdfFont {
	private PDFont font;
	private float fontSize;
	private float lineHeight;
	private Color color;
	
	public PdfFont() { init(null, null, null); }
	public PdfFont(PDFont font) { init(font, null, null); }
	public PdfFont(PDFont font, Float fontSize) { init(font, fontSize, null); }
	public PdfFont(PDFont font, Float fontSize, Color color) { init(font, fontSize, color); }
	
	protected void init(PDFont font, Float fontSize, Color color) {
		this.font = font != null ? font : new PDType1Font(Standard14Fonts.FontName.HELVETICA);
		this.fontSize = Data.nvl(fontSize, 12f);
		lineHeight = 1.2f;
		this.color = color != null ? color : Color.BLACK;
	}
	
	public PDFont getFont() { return font; }
	public void setFont(PDFont font) { this.font = font; }
	
	public float getFontSize() { return fontSize; }
	public void setFontSize(float fontSize) { this.fontSize = fontSize; }
	
	public float getLineHeight() { return lineHeight; }
	public void setLineHeight(float lineHeight) { this.lineHeight = lineHeight; }
	
	public Color getColor() { return color; }
	public void setColor(Color color) { this.color = color; }
}
