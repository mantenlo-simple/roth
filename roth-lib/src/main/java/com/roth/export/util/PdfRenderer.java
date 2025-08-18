package com.roth.export.util;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import com.roth.base.util.Data;

public class PdfRenderer {
	
	public static final int OVERFLOW_ERROR = -1;
	public static final int OVERFLOW_CLIPPED = 0;
	public static final int OVERFLOW_OVERDRAW = 1;
	
	private ArrayList<PdfPage> pages;
	
	protected PDDocument doc;
	private float pageHeight;
	private PDPageContentStream contents;
	
	private int overflowMode;
	public int getOverflowMode() { return overflowMode; }
	/**
	 * Sets whether container overflow is allowed, and how to handle overflow if not.<br/>
	 * OVERFLOW_ERROR will cause an exception to be thrown if a container that overflows its parent is pushed onto the stack.<br/>
	 * OVERFLOW_CLIPPED (default) will cause the the pushed container to be clipped (resized) to fit its parent.<br/>
	 * OVERFLOW_OVERDRAW will allow the pushed container to overflow the parent.
	 * @param overflowMode
	 */
	public void setOverflowMode(int overflowMode) { this.overflowMode = overflowMode; }
	
	private ArrayList<PDRectangle> containmentStack;
	public void popContainer() throws Exception {
		if (containmentStack == null)
			throw new Exception("The containment stack has not been initialized.");
		if (containmentStack.size() <= 1)
			throw new Exception("Cannot pop page containment.");
		containmentStack.remove(containmentStack.size() - 1);
	}
	/**
	 * Pushes a container boundary onto the container stack.  The X and Y coordinates are relative to
	 * the parent (the prior container pushed onto the stack).  The bottom of the stack (the first container)
	 * is the area within the margins of the page and cannot be popped.  All X and Y coordinates used in
	 * drawing is relative to the container stack.
	 * @param rect
	 * @throws Exception
	 */
	public void pushContainer(PDRectangle rect) throws Exception {
		if (containmentStack == null)
			throw new Exception("The containment stack has not been initialized.");
		containmentStack.add(validateContainer(rect));
	}
	
	public PdfRenderer() {
		pages = new ArrayList<>();
	}
	
	/**
	 * Initialize the document to make it ready for rendering.
	 */
	public void init() {
		if (doc == null)
			doc = new PDDocument();
		else
			throw new IllegalStateException("Document is already initialized.");
	}
	
	/**
	 * Close the document after saving it to a file.
	 * @param filename
	 * @throws IOException
	 */
	public void close(String filename) throws IOException {
		if (doc != null) {
			contents.close();
			contents = null;
			if (filename != null)
				doc.save(filename);
			doc.close();
			doc = null;
		}
		else
			throw new IllegalStateException("Document is already closed.");
	}
	
	/**
	 * Close the document without saving it to a file.  This effectively cancels the rendering.
	 * @param filename
	 * @throws IOException
	 */
	public void close() throws IOException {
		close(null);
	}
	
	/**
	 * Create a new page with default dimensions.  If this is not the first page, then
	 * the default dimensions are derived from the first page, otherwise the dimensions
	 * are set to (PAGE_LETTER, ORIENTATION_PORTRAIT, MARGINS_WIDE).
	 * @throws IOException
	 */
	public void addPage() throws IOException {
		if (pages.size() > 0)
			addPage(pages.get(0).copy());
		else
			addPage(new PdfPage());
	}
	
	/**
	 * Create a new page with new dimensions.
	 * @param pageSize
	 * @param orientation
	 * @param margins
	 * @throws IOException
	 */
	public void addPage(float[] pageSize, int orientation, float[] margins) throws IOException {
		PdfPage page = new PdfPage(pageSize, orientation, margins);
		addPage(page);
	}
	
	/**
	 * Support method to add a copied page.
	 * @param page
	 * @throws IOException
	 */
	protected void addPage(PdfPage page) throws IOException {
		if (contents != null)
			contents.close();
		pages.add(page);
		contents = page.init(doc);
		pageHeight = page.getAdjPageSize()[1];
		
		if (containmentStack == null)
			containmentStack = new ArrayList<>();
		else
			containmentStack.clear();
		float x = page.getMargins()[0];
		float y = page.getMargins()[1];
		float width = page.getAdjPageSize()[0] - (page.getMargins()[0] + page.getMargins()[2]);
		float height = page.getAdjPageSize()[1] - (page.getMargins()[1] + page.getMargins()[3]);
		containmentStack.add(new PDRectangle(x, y, width, height));
	}
	
	protected float stackX(float x) {
		float newx = x;
		for (PDRectangle rec : containmentStack)
			newx += rec.getLowerLeftX();
		return newx;
	}
	protected float stackY(float y) {
		float newy = y;
		for (PDRectangle rec : containmentStack)
			newy += rec.getLowerLeftY();
		return newy;
	}
	/**
	 * Support method to translate origin to the upper-left corner instead of the lower-left (why did they do that with PDF?).
	 * @param y
	 * @return
	 */
	protected float translateY(float y) { return pageHeight - y; }
	
	public void drawRect(PDRectangle rect) throws IOException { drawRect(rect, null); }
	
	public void drawRect(PDRectangle rect, PdfLine line) throws IOException {
		PdfLine _line = line != null ? line : new PdfLine();
		contents.setStrokingColor(_line.getColor());
		contents.setLineWidth(_line.getWidth());
		contents.setLineDashPattern(_line.getPattern(), _line.getPhase());
		contents.moveTo(stackX(rect.getLowerLeftX()), translateY(stackY(rect.getLowerLeftY())));
		contents.lineTo(stackX(rect.getUpperRightX()), translateY(stackY(rect.getLowerLeftY())));
		contents.lineTo(stackX(rect.getUpperRightX()), translateY(stackY(rect.getUpperRightY())));
		contents.lineTo(stackX(rect.getLowerLeftX()), translateY(stackY(rect.getUpperRightY())));
		contents.lineTo(stackX(rect.getLowerLeftX()), translateY(stackY(rect.getLowerLeftY())));
		contents.closeAndStroke();
	}
	
	public void fillRect(PDRectangle rect) throws IOException { fillRect(rect, null, null); }
	
	public void fillRect(PDRectangle rect, PdfLine line, Color fillColor) throws IOException {
		PdfLine _line = line != null ? line : new PdfLine();
		Color _fillColor = fillColor != null ? fillColor : Color.WHITE;
		contents.setStrokingColor(_line.getColor());
		contents.setLineWidth(_line.getWidth());
		contents.setLineDashPattern(_line.getPattern(), _line.getPhase());
		contents.setNonStrokingColor(_fillColor);
		contents.moveTo(stackX(rect.getLowerLeftX()), translateY(stackY(rect.getLowerLeftY())));
		contents.lineTo(stackX(rect.getUpperRightX()), translateY(stackY(rect.getLowerLeftY())));
		contents.lineTo(stackX(rect.getUpperRightX()), translateY(stackY(rect.getUpperRightY())));
		contents.lineTo(stackX(rect.getLowerLeftX()), translateY(stackY(rect.getUpperRightY())));
		contents.lineTo(stackX(rect.getLowerLeftX()), translateY(stackY(rect.getLowerLeftY())));
		contents.closeAndFillAndStroke();
	}
	
	/**
	 * Draws wrapped text within the bounds described.  If there is any excess text 
	 * (i.e. text that exceeds the bounds), it is returned.
	 * @param text
	 * @param font
	 * @param fontSize
	 * @param rect
	 * @return
	 * @throws IOException
	 */
	public String drawText(String text, PdfFont font, PDRectangle rect) throws IOException {
		float x = stackX(rect.getLowerLeftX());
		float height = rect.getHeight();
		float width = rect.getWidth();
		float offset = translateY(stackY(rect.getLowerLeftY())) - font.getFontSize();
		int maxLines = floorFloat(height / (font.getFontSize() * font.getLineHeight()));
		int lineCount = 1;
		ArrayList<String> lines = wrappedLines(Data.nvl(text), font.getFont(), font.getFontSize(), width);
		contents.setNonStrokingColor(font.getColor());
		contents.beginText();
		contents.setFont(font.getFont(), font.getFontSize());
		contents.newLineAtOffset(x, offset);
		
		for (String line : lines) {
			contents.showText(Data.nvl(line));
			newLine(font.getFontSize() * font.getLineHeight());
			lineCount++;
			if (lineCount > maxLines)
				break;
		}
		
		contents.endText();
		
		return (lineCount >= lines.size()) ? null : Data.join(lines.subList(lineCount - 1, lines.size()).toArray(new String[lines.size() - lineCount]), "\n");
	}
	
	protected int floorFloat(float value) {
		int result = Math.round(value);
		return result > value ? result - 1 : result;
	}
	
	protected void newLine(float offset) throws IOException {
		contents.newLineAtOffset(0f, -offset);
	}
	
	protected static ArrayList<String> wrappedLines(String text, PDFont font, float fontSize, float maxWidth) throws IOException {
		ArrayList<String> result = new ArrayList<>();
		String[] crlf =  Data.splitLF(text);// text.split("\r\n|\r|\n");
		for (String s : crlf) {
			if (getTextWidth(s, font, fontSize) <= maxWidth)
				result.add(s);
			else {
				String[] sp = s.split(" ");
				String ns = "";
				for (String sps : sp) {
					if (getTextWidth(ns + (ns.isEmpty() ? "" : " ") + sps, font, fontSize) <= maxWidth)
						ns += (ns.isEmpty() ? "" : " ") + sps;
					else {
						result.add(ns);
						ns = sps;
					}
				}
				result.add(ns);
			}
		}
		return result;
	}
	
	protected static float getTextWidth(String text, PDFont font, float fontSize) throws IOException {
		return font.getStringWidth(text) / 1000 * fontSize;
	}
	
	/**
	 * Draws an image within the bounding rectangle.  The image will be stretched to the bounding rectangle, so it is 
	 * important to set the width and height to the same ratio as the original image.
	 * @param filename
	 * @param rect
	 * @throws IOException
	 */
	public void drawImage(String filename, PDRectangle rect) throws IOException {
		PDImageXObject image = PDImageXObject.createFromFile(filename, doc);
		contents.drawImage(image, stackX(rect.getLowerLeftX()), translateY(stackY(rect.getLowerLeftY() + rect.getHeight())), rect.getWidth(), rect.getHeight());
	}
	
	protected PDRectangle validateContainer(PDRectangle rect) throws Exception {
		if (overflowMode == OVERFLOW_OVERDRAW)
			return rect;
		else {
			PDRectangle cont = containmentStack.get(containmentStack.size() - 1);
			if (cont.getWidth() < (rect.getLowerLeftX() + rect.getWidth()) || cont.getHeight() < (rect.getLowerLeftY() + rect.getHeight())) {
				if (overflowMode == OVERFLOW_ERROR)
					throw new Exception();
				float newWidth = cont.getWidth() < (rect.getLowerLeftX() + rect.getWidth()) ? cont.getWidth() - rect.getLowerLeftX() : rect.getWidth();
				float newHeight = cont.getHeight() < (rect.getLowerLeftY() + rect.getHeight()) ? cont.getHeight() - rect.getLowerLeftY() : rect.getHeight();
				PDRectangle newRect = new PDRectangle(rect.getLowerLeftX(), rect.getLowerLeftY(), newWidth, newHeight);
				return newRect;
			}
		}
		return rect;
	}
}
