package com.roth.export.util;

import java.io.IOException;
import java.io.Serializable;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;

public class PdfPage implements Serializable {
	private static final long serialVersionUID = 6104570002921556379L;

	public static final float POINTS_PER_INCH = 72f;
	public static final float POINTS_PER_MM = 2.834646f;
	
	public static final float[] MARGINS_NARROW = { 0.5f * POINTS_PER_INCH, 0.5f * POINTS_PER_INCH, 0.5f * POINTS_PER_INCH, 0.5f * POINTS_PER_INCH };
	public static final float[] MARGINS_DEFAULT = { POINTS_PER_INCH, POINTS_PER_INCH, POINTS_PER_INCH, POINTS_PER_INCH };
	
	public static final int ORIENTATION_LANDSCAPE = 1;
	public static final int ORIENTATION_PORTRAIT = 0;
	
	public static final float[] PAGE_GOVT_LEGAL = { POINTS_PER_INCH * 8.5f,  POINTS_PER_INCH * 13 };
	public static final float[] PAGE_GOVT_LETTER = { POINTS_PER_INCH * 8, POINTS_PER_INCH * 10.5f };
	public static final float[] PAGE_HALF_LETTER = { POINTS_PER_INCH * 5.5f, POINTS_PER_INCH * 8.5f };
	public static final float[] PAGE_JR_LEGAL = { POINTS_PER_INCH * 5, POINTS_PER_INCH * 8 };
	public static final float[] PAGE_LEDGER = { POINTS_PER_INCH * 17, POINTS_PER_INCH * 11 };
	public static final float[] PAGE_LEGAL = { POINTS_PER_INCH * 8.5f, POINTS_PER_INCH * 14 };
	public static final float[] PAGE_LETTER = { POINTS_PER_INCH * 8.5f, POINTS_PER_INCH * 11 };
	public static final float[] PAGE_TABLOID = { POINTS_PER_INCH * 11, POINTS_PER_INCH * 17 };
	
	public static final float[] PAGE_A0 = { POINTS_PER_MM * 841, POINTS_PER_MM * 1189 };
	public static final float[] PAGE_A1 = { POINTS_PER_MM * 594, POINTS_PER_MM * 841 };
	public static final float[] PAGE_A2 = { POINTS_PER_MM * 420, POINTS_PER_MM * 594 };
	public static final float[] PAGE_A3 = { POINTS_PER_MM * 297, POINTS_PER_MM * 420 };
	public static final float[] PAGE_A4 = { POINTS_PER_MM * 210, POINTS_PER_MM * 297 };
	public static final float[] PAGE_A5 = { POINTS_PER_MM * 148, POINTS_PER_MM * 210 };
	public static final float[] PAGE_A6 = { POINTS_PER_MM * 105, POINTS_PER_MM * 148 };
	
	private float[] margins;
	private int orientation;
	private float[] pageSize;
		
	private PDPage page;
	private PDPageContentStream contents;
	
	protected float[] adjPageSize;
	
	public PdfPage() {
		pageSize = PAGE_LETTER;
		orientation = ORIENTATION_PORTRAIT;
		margins = MARGINS_DEFAULT;
		adjustPageSize();
	}
	
	public PdfPage(float[] pageSize, int orientation, float[] margins) {
		setPageSize(pageSize);
		setOrientation(orientation);
		setMargins(margins);
	}
	
	public PDPageContentStream init(PDDocument doc) throws IOException {
		page = new PDPage(new PDRectangle(adjPageSize[0], adjPageSize[1]));
		doc.addPage(page);
		contents = new PDPageContentStream(doc, page);
		return contents;
	}
	
	public PdfPage copy() { return new PdfPage(pageSize, orientation, margins); }
	
	public float[] getMargins() { return margins; }
	public void setMargins(float[] margins) {
		if (margins == null || margins.length != 4)
			throw new IllegalArgumentException("Invalid argument supplied; the argument was either null or an improper length.");
		this.margins = margins;
	}

	public int getOrientation() { return orientation; }
	public void setOrientation(int orientation) {
		if (orientation != ORIENTATION_PORTRAIT && orientation != ORIENTATION_LANDSCAPE)
			throw new IllegalArgumentException("Invalid argument " + orientation + " supplied.");
		this.orientation = orientation;
		adjustPageSize();
	}

	public float[] getPageSize() { return pageSize; }
	public void setPageSize(float[] pageSize) { 
		if (pageSize == null || pageSize.length != 2)
			throw new IllegalArgumentException("Invalid argument supplied; the argument was either null or an improper length.");
		this.pageSize = pageSize;
		adjustPageSize();
	}
	
	protected void adjustPageSize() {
		float[] adjPageSize = { orientation == ORIENTATION_PORTRAIT ? pageSize[0] : pageSize[1], orientation == ORIENTATION_PORTRAIT ? pageSize[1] : pageSize[0] };
		this.adjPageSize = adjPageSize;
	}
	public float[] getAdjPageSize() { return adjPageSize; }
	
	public PDPage getPage() { return page; }
	public PDPageContentStream getContents() { return contents; }
}
