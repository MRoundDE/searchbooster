package de.mround.searchbooster.gui.model;

import java.io.File;
import java.text.DateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import javax.swing.table.AbstractTableModel;

/**
 * GUI model for a table containing search results
 * 
 * The model is realizing the MVC pattern of the Swing GUI.
 * 
 * @author Tobias Schulz
 */
@SuppressWarnings("serial")
public class ResultTableModel extends AbstractTableModel {
	/*
	 * Column	Type		Description
	 * ========================================================================
	 * 0		String		File name
	 * 1		String		File path
	 * 2		String		File modification date
	 * 3 (unused)	Boolean		File content matches search string
	 */	

	/**
	 * List of files
	 */
	private List<File> files = new Vector<File>();
	
	/**
	 * List of file modification dates
	 */
	private List<Date> modificationDate = new Vector<Date>();
	
	/**
	 * List of flags indicating file content search string matches
	 */
	private List<Boolean> contentMatch = new Vector<Boolean>();
	
	/**
	 * Freeze input flag
	 */
	private boolean freeze = false;
	
	/**
	 * Constructor
	 */
	public ResultTableModel() {
		
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getColumnCount() {
		
		return 3;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getRowCount() {
		
		return this.files.size();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		
		if (columnIndex == 0) {
			return this.files.get(rowIndex).getName();
		}
		else if (columnIndex == 1) {
			return this.files.get(rowIndex).getParent();
		}
		else if (columnIndex == 2) {
			return DateFormat.getDateInstance(DateFormat.MEDIUM).format(this.modificationDate.get(rowIndex));
		}
		else {
			return this.contentMatch.get(rowIndex);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Class<?> getColumnClass(int columnIndex) {
		
		if (columnIndex == 0) {
			return String.class;
		}
		else if (columnIndex == 1) {
			return String.class;
		}
		else if (columnIndex == 2) {
			return String.class;
		}
		else {
			return Boolean.class;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getColumnName(int column) {
		
		if (column == 0) {
			return "Name";
		}
		else if (column == 1) {
			return "Path";
		}
		else if (column == 2) {
			return "Modification date";
		}
		else {
			return "Content match";
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		
		return;
	}
	
	/**
	 * Add a row to the table
	 * 
	 * @param file - a file
	 * @param modificationDate - a modification date
	 * @param contentMatch - if content match
	 */
	public void addRow(File file, Date modificationDate, Boolean contentMatch) {
		
		if (!this.freeze) {
			this.files.add(file);
			this.modificationDate.add(modificationDate);
			this.contentMatch.add(contentMatch);

			/* Notify that table changed */
			fireTableRowsInserted(this.getRowCount() - 1, this.getRowCount() - 1);
		}
	}
	
	/**
	 * Remove rows from the table
	 * 
	 * @param rowIndex Rows to be removed
	 */
	public void removeRows(int [] rowIndex) {
		
		/* Sort row index array */
		Arrays.sort(rowIndex);
		
		/* Remove rows in descending order (remaining row indexes to be removed will not be shifted) */
		for (int i = rowIndex.length - 1; i >= 0; i--) {
			int row = rowIndex[i];
			
			try {
				this.files.remove(row);
				this.modificationDate.remove(row);
				this.contentMatch.remove(row);
			} catch (IndexOutOfBoundsException e) {
			}
			
			/* Notify that table changed */
			fireTableRowsDeleted(row, row);
		}
	}
	
	/**
	 * Clear all rows from the table
	 */
	public void clear() {
		
		this.files.clear();
		this.modificationDate.clear();
		this.contentMatch.clear();
		
		/* Notify that table changed */
		fireTableDataChanged();
	}

	/**
	 * Get file object
	 * 
	 * @param row Table row index
	 * @return File object of specified table row
	 */
	public File getFileAt(int row) {
		return this.files.get(row);
	}

	/**
	 * Check if table input is freezed
	 * 
	 * @return Input freeze flag
	 */
	public boolean isFreeze() {
		return freeze;
	}

	/**
	 * Set table input freeze status
	 * 
	 * @param freeze Input freeze status
	 */
	public void setFreeze(boolean freeze) {
		this.freeze = freeze;
	}

}
