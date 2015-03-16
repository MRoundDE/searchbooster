package de.mround.searchbooster.gui.view;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.Component;
import java.awt.FontMetrics;

/**
 * Column auto sizer for a JTable
 * 
 * @author Tobias Schulz
 */
public class TableColumnAutoSizer {

	/**
	 * Setting up a JTable to unlock auto column size
	 * 
	 * @param table Table to be setup
	 */
	public static void setupTable(JTable table) {
		
		/* Auto width of columns prohibits table auto size */
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		
		/* Auto width of columns prohibits user to resize columns manually */
		table.getTableHeader().setResizingAllowed(false);
		
		/* Auto width table columns */
		TableColumnAutoSizer.updateWidths(table);
	}
	
	/**
	 * Update column widths
	 * 
	 * This update method has to be called ever since a column width may have changed.
	 * 
	 * @param table Table to auto width columns
	 */
	public static void updateWidths(JTable table) {
		
		/* Getting some table informations */
		JTableHeader tableHeader = table.getTableHeader();

		if(tableHeader == null) {
			return;
		}

		int columnMargin = table.getColumnModel().getColumnMargin();

		FontMetrics headerFontMetrics = tableHeader.getFontMetrics(tableHeader.getFont());

		int[] minWidths = new int[table.getColumnCount()];
		int[] maxWidths = new int[table.getColumnCount()];

		/* Determine column widths (sorted by view order) */
		for(int columnIndex = 0; columnIndex < table.getColumnCount(); columnIndex++) {
			/* Header column width */
			int headerWidth = headerFontMetrics.stringWidth(table.getColumnName(columnIndex));
			
			/* Row sorter adds an arrow to the header (extra width) */
			if (table.getAutoCreateRowSorter()) {
				headerWidth += 15;
			}

			minWidths[columnIndex] = headerWidth + columnMargin;

			/* Max. column width of all rows */
			int maxWidth = getMaximalRequiredColumnWidth(table, columnIndex, headerWidth);

			maxWidths[columnIndex] = Math.max(maxWidth, minWidths[columnIndex]) + columnMargin;
		}

		/* Set column widths */
		for(int i = 0; i < minWidths.length; i++) {
			table.getColumnModel().getColumn(i).setPreferredWidth(maxWidths[i]);
		}
	}

	/**
	 * Determine max. column width of all rows in a column
	 */
	private static int getMaximalRequiredColumnWidth(JTable table, int columnIndex, int headerWidth) {
		int maxWidth = headerWidth;

		TableColumn column = table.getColumnModel().getColumn(columnIndex);

		/* Get column cell renderer */
		TableCellRenderer cellRenderer = column.getCellRenderer();

		if(cellRenderer == null) {
			cellRenderer = new DefaultTableCellRenderer();
		}

		/* Get cell width from renderer */
		for(int row = 0; row < table.getModel().getRowCount(); row++) {
			Component rendererComponent = cellRenderer.getTableCellRendererComponent(table,
					table.getModel().getValueAt(row, table.getColumnModel().getColumn(columnIndex).getModelIndex()),
					false,
					false,
					row,
					table.getColumnModel().getColumn(columnIndex).getModelIndex());

			double valueWidth = rendererComponent.getPreferredSize().getWidth();

			maxWidth = (int) Math.max(maxWidth, valueWidth);
		}

		return maxWidth;
	}

}
