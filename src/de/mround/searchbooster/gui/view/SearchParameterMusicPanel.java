package de.mround.searchbooster.gui.view;

import javax.swing.JPanel;
import net.miginfocom.swing.MigLayout;
import javax.swing.JTextField;
import javax.swing.JLabel;

/**
 * Extended search parameter panel for music
 * 
 * @author Tobias Schulz
 */
@SuppressWarnings("serial")
public class SearchParameterMusicPanel extends JPanel {
	
	/* GUI elements*/
	private final JTextField searchParameterMusicInterpret = new JTextField();
	private final JTextField searchParameterMusicAlbum = new JTextField();
	private final JLabel musicAlbumLabel = new JLabel("Album");
	private final JLabel musicInterpretLabel = new JLabel("Interpret");
	
	/**
	 * Constructor
	 */
	public SearchParameterMusicPanel() {
		this.searchParameterMusicAlbum.setToolTipText("<html>Album name<br>Available wildcards<br><blockquote>* - Any number of arbitrary characters<br>? - One arbitrary character</blockquote></html>");
		this.searchParameterMusicAlbum.setColumns(10);
		this.searchParameterMusicInterpret.setToolTipText("<html>Interpret name<br>Available wildcards<br><blockquote>* - Any number of arbitrary characters<br>? - One arbitrary character</blockquote></html>");
		this.searchParameterMusicInterpret.setColumns(10);
		initGUI();
	}
	
	/**
	 * Init GUI
	 */
	private void initGUI() {
		
		/* Set layout */
		setLayout(new MigLayout("", "[left][grow,left]", "[top][top]"));
		
		add(this.musicAlbumLabel, "cell 0 0,alignx left,aligny baseline");
		add(this.searchParameterMusicAlbum, "cell 1 0,growx,aligny baseline");
		
		add(this.musicInterpretLabel, "cell 0 1,alignx left,aligny baseline");
		
		add(this.searchParameterMusicInterpret, "cell 1 1,growx,aligny baseline");	
	}
	
	/**
	 * Get music interpret search string
	 * 
	 * @return Music interpret search string
	 */
	public String getMusicInterpretString() {
		return this.searchParameterMusicInterpret.getText();
	}
	
	/**
	 * Set music interpret search string
	 * 
	 * @param interpret Music interpret search string
	 */
	public void setMusicInterpretString(String interpret) {
		this.searchParameterMusicInterpret.setText(interpret);
	}
	
	/**
	 * Get music album search string
	 * 
	 * @return Music album search string
	 */
	public String getMusicAlbumString() {
		return this.searchParameterMusicAlbum.getText();
	}
	
	/**
	 * Set music album srearch string
	 * 
	 * @param album Music album search string
	 */
	public void setMusicAlbumString(String album) {
		this.searchParameterMusicAlbum.setText(album);
	}

}
