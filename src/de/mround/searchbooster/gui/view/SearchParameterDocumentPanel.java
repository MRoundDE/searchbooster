package de.mround.searchbooster.gui.view;

import javax.swing.JPanel;
import net.miginfocom.swing.MigLayout;
import javax.swing.JTextField;
import javax.swing.JLabel;

/**
 * Extended search parameter panel for documents
 * 
 * @author Tobias Schulz
 */
@SuppressWarnings("serial")
public class SearchParameterDocumentPanel extends JPanel {
	
	/* GUI elements */
	private final JTextField searchParameterDocumentContentTextField = new JTextField();
	private final JLabel documentContentLabel = new JLabel("Content");
	
	/**
	 * Constructor
	 */
	public SearchParameterDocumentPanel() {
		this.searchParameterDocumentContentTextField.setToolTipText("<html>Document content<br>Available wildcards<br><blockquote>* - Any number of arbitrary characters<br>? - One arbitrary character</blockquote></html>");
		this.searchParameterDocumentContentTextField.setColumns(10);
		initGUI();
	}
	
	/**
	 * Init GUI
	 */
	private void initGUI() {
		
		/* Set layout */
		setLayout(new MigLayout("", "[left][grow,left]", "[top]"));
		
		add(this.documentContentLabel, "cell 0 0,alignx left,aligny baseline");
		
		add(this.searchParameterDocumentContentTextField, "cell 1 0,growx,aligny baseline");
	}
	
	/**
	 * Get document content search string
	 * 
	 * @return Document content search string
	 */
	public String getDocumentContentString() {
		return this.searchParameterDocumentContentTextField.getText();
	}
	
	/**
	 * Set document content search string
	 * 
	 * @param content Document content search string
	 */
	public void setDocumentContentString(String content) {
		this.searchParameterDocumentContentTextField.setText(content);
	}

}
