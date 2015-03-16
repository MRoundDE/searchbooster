package de.mround.searchbooster.gui.view;

import java.awt.CardLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.miginfocom.swing.MigLayout;

import org.jdesktop.swingx.JXDatePicker;

import de.mround.searchbooster.api.xml.RequestSearchBuilder.Content.FileType;
import de.mround.searchbooster.gui.controller.SearchParameter;
import javax.swing.JLabel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;

/**
 * Search parameter panel
 * 
 * @author Tobias Schulz
 */
@SuppressWarnings("serial")
public class SearchParameterPanel extends JPanel {
	
	/* GUI elements */
	private final JTextField searchParameterStringTextField = new JTextField();
	private final JXDatePicker searchParameterBeginModificationDatePicker = new JXDatePicker();
	private final JXDatePicker searchParameterEndModificationDatePicker = new JXDatePicker();
	private final String [] searchParameterTypes = {"", "Document", "Music", "Picture"};
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private final JComboBox searchParameterTypeComboBox = new JComboBox(searchParameterTypes);
	private final JPanel searchParameterTypeCardPanel = new JPanel(new CardLayout(0, 0));
	private final SearchParameterDocumentPanel searchParameterDocumentPanel = new SearchParameterDocumentPanel();
	private final SearchParameterMusicPanel searchParameterMusicPanel = new SearchParameterMusicPanel();
	private final JLabel fileNameLabel = new JLabel("File name");
	private final JLabel fileModificationDateLabel = new JLabel("Modification date");
	private final JLabel fileModificationDateCenterLabel = new JLabel("until");
	private final JLabel fileTypeLabel = new JLabel("File type");
	private final JSeparator separator = new JSeparator();
	
	/**
	 * Constructor
	 */
	public SearchParameterPanel() {
		
		/* Add extended search panels to card layout */
		this.searchParameterTypeCardPanel.add(new JPanel(), this.searchParameterTypes[0]);
		this.searchParameterTypeCardPanel.add(searchParameterDocumentPanel, this.searchParameterTypes[1]);
		this.searchParameterTypeCardPanel.add(searchParameterMusicPanel, this.searchParameterTypes[2]);
		this.searchParameterTypeCardPanel.add(new JPanel(), this.searchParameterTypes[3]);
		
		initGUI();
	}
	
	/**
	 * Init GUI
	 */
	private void initGUI() {
		setLayout(new MigLayout("", "[10px:n,left][left][left][left][grow,left]", "[top][top][top][grow,bottom]"));
		
		/* Switch extended search parameter cards according to searchParameterTypeComboBox */
		
		add(this.fileNameLabel, "cell 0 0 2 1,alignx left,aligny baseline");
		this.searchParameterStringTextField.setToolTipText("<html>File name<br>Available wildcards<br><blockquote>* - Any number of arbitrary characters<br>? - One arbitrary character</blockquote></html>"); //TODO list wildcards
		this.searchParameterStringTextField.setColumns(10);
		
		add(this.searchParameterStringTextField, "cell 2 0 3 1,growx,aligny baseline");
		
		add(this.fileModificationDateLabel, "cell 0 1 2 1,alignx left,aligny baseline");
		this.searchParameterBeginModificationDatePicker.setToolTipText("Earliest modification date");
		add(searchParameterBeginModificationDatePicker, "cell 2 1,alignx left,aligny baseline");
		
		add(this.fileModificationDateCenterLabel, "cell 3 1,alignx center,aligny baseline");
		this.searchParameterEndModificationDatePicker.setToolTipText("Latest modification date");
		add(searchParameterEndModificationDatePicker, "cell 4 1,alignx left,aligny baseline");
		
		add(this.fileTypeLabel, "cell 0 2 2 1,alignx left,aligny baseline");
		this.searchParameterTypeComboBox.setToolTipText("File type");
		add(this.searchParameterTypeComboBox, "cell 2 2,alignx left,aligny baseline");
		this.searchParameterTypeComboBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				
				((CardLayout) searchParameterTypeCardPanel.getLayout()).show(searchParameterTypeCardPanel, (String) searchParameterTypeComboBox.getSelectedItem());
			}
		});
		this.separator.setOrientation(SwingConstants.VERTICAL);
		
		add(this.separator, "cell 0 3,alignx center,growy");
		
		add(this.searchParameterTypeCardPanel, "cell 1 3 4 1,grow");
	}
	
	/**
	 * Get search parameter
	 * 
	 * @return Search parameter
	 */
	public SearchParameter getSearchParameter() {
		
		SearchParameter searchParameter = new SearchParameter();
		searchParameter.beginModificationDate = searchParameterBeginModificationDatePicker.getDate();
		searchParameter.endModificationDate = searchParameterEndModificationDatePicker.getDate();
		searchParameter.fileNameString = searchParameterStringTextField.getText();
		
		/* Append extended parameter */
		String extendedParameter = (String) searchParameterTypeComboBox.getSelectedItem();
		if (extendedParameter.equals(searchParameterTypes[1])) {
			/* Document */
			searchParameter.fileType = FileType.DOCUMENT;
			searchParameter.documentContentString = this.searchParameterDocumentPanel.getDocumentContentString();
		}
		else if (extendedParameter.equals(searchParameterTypes[2])) {
			/* Music */
			searchParameter.fileType = FileType.MUSIC;
			searchParameter.musicInterpretString = this.searchParameterMusicPanel.getMusicInterpretString();
			searchParameter.musicAlbumString = this.searchParameterMusicPanel.getMusicAlbumString();
		}
		else if (extendedParameter.equals(searchParameterTypes[3])) {
			/* Picture */
			searchParameter.fileType = FileType.PICTURE;
		}
		else {
			searchParameter.fileType = FileType.ALL;
		}
		
		return searchParameter;
	}
	
	/**
	 * Set search parameter
	 * 
	 * @param searchParameterSet - Set search parameter
	 */
	public void setSearchParameter(SearchParameter searchParameterSet) {
		
		searchParameterBeginModificationDatePicker.setDate(searchParameterSet.beginModificationDate);
		searchParameterEndModificationDatePicker.setDate(searchParameterSet.endModificationDate);
		if (searchParameterSet.fileNameString != null) {
			searchParameterStringTextField.setText(searchParameterSet.fileNameString);
		}
		else {
			searchParameterStringTextField.setText("");
		}
		
		/* Set extended parameter */
		switch (searchParameterSet.fileType) {
			case DOCUMENT:
				searchParameterTypeComboBox.setSelectedIndex(1);
				if (searchParameterSet.documentContentString != null) {
					searchParameterDocumentPanel.setDocumentContentString(searchParameterSet.documentContentString);
				}
				else {
					searchParameterDocumentPanel.setDocumentContentString("");
				}
				break;
			case MUSIC:
				searchParameterTypeComboBox.setSelectedIndex(2);
				if (searchParameterSet.musicAlbumString != null) {
					searchParameterMusicPanel.setMusicAlbumString(searchParameterSet.musicAlbumString);
				}
				else {
					searchParameterMusicPanel.setMusicAlbumString("");
				}
				if (searchParameterSet.musicInterpretString != null) {
					searchParameterMusicPanel.setMusicInterpretString(searchParameterSet.musicInterpretString);
				}
				else {
					searchParameterMusicPanel.setMusicInterpretString("");
				}
				break;
			case PICTURE:
				searchParameterTypeComboBox.setSelectedIndex(3);
				break;		
			default:
			case ALL:
				searchParameterTypeComboBox.setSelectedIndex(0);
				break;
		}
	}

	/**
	 * Restore default search parameter
	 */
	public void restoreDefaults() {
		this.searchParameterBeginModificationDatePicker.setDate(null);
		this.searchParameterEndModificationDatePicker.setDate(null);
		this.searchParameterStringTextField.setText("");
		this.searchParameterTypeComboBox.setSelectedItem(searchParameterTypes[0]);
	}

}
