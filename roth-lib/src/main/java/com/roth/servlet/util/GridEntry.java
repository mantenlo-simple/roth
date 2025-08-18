package com.roth.servlet.util;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;

import com.roth.base.util.Data;

public class GridEntry implements Serializable {
	private static final long serialVersionUID = -4784010747461774538L;
	
	private String containerId;
	private String title;
	private String dialogCaption;
	private String width;
	private String height;
	private GridOptionsEntry options;
	private ColumnEntry[] columns;
	private String onRowClick;
	private String onRowDblClick;
	private String manualJsp;
	
	public String getContainerId() { return containerId; }
	public void setContainerId(String containerId) { this.containerId = containerId; }

	public String getTitle() { return title; }
	public void setTitle(String title) { this.title = title; }

	public String getDialogCaption() { return dialogCaption; }
	public void setDialogCaption(String dialogCaption) { this.dialogCaption = dialogCaption; }

	public String getWidth() { return width; }
	public void setWidth(String width) { this.width = width; }

	public String getHeight() { return height; }
	public void setHeight(String height) { this.height = height; }

	public GridOptionsEntry getOptions() { return options; }
	public void setOptions(GridOptionsEntry options) { this.options = options; }

	public ColumnEntry[] getColumns() { return columns; }
	public void setColumns(ColumnEntry[] columns) { this.columns = columns; }

	public String getOnRowClick() { return onRowClick; }
	public void setOnRowClick(String onRowClick) { this.onRowClick = onRowClick; }

	public String getOnRowDblClick() { return onRowDblClick; }
	public void setOnRowDblClick(String onRowDblClick) { this.onRowDblClick = onRowDblClick; }

	public String getManualJsp() { return manualJsp; }
	public void setManualJsp(String manualJsp) { this.manualJsp = manualJsp; }
	
	@Override
	public boolean equals(Object o) {
		if (this == o) 
			return true;
        if (o == null || getClass() != o.getClass()) 
        	return false;
        GridEntry entry = (GridEntry)o;
        return Data.eq(containerId, entry.containerId) &&
        	   Data.eq(title, entry.title) &&
        	   Data.eq(dialogCaption, entry.dialogCaption) &&
        	   Data.eq(width, entry.width) &&
        	   Data.eq(height, entry.height) &&
        	   Data.eq(options, entry.options) &&
        	   Arrays.equals(columns, entry.columns);
	}
	
	@Override
    public int hashCode() {
        int result = Objects.hash(containerId, title, dialogCaption, width, height, options);
        result = 31 * result + Arrays.hashCode(columns);
        return result;
    }

	@Override
    public String toString() {
        return String.format("GridEntry{containerId=%s, title=%s, dialogCaption=%s, width=%s, height=%s, options=%s, columns=%s}", 
        	   containerId,
        	   title,
        	   dialogCaption,
        	   width,
        	   height,
        	   options.toString(),
        	   Arrays.toString(columns));
    }
}