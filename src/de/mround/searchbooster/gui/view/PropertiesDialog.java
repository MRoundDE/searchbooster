package de.mround.searchbooster.gui.view;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.File;
import java.util.List;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;

import net.miginfocom.swing.MigLayout;
import de.mround.searchbooster.gui.controller.ConfigurationSetting;
import de.mround.searchbooster.gui.controller.GuiConfigXml;
import de.mround.searchbooster.gui.controller.GuiController;

/**
 * Dialog for configuration of SearchBooster properties
 * 
 * @author Tobias Schulz
 */
@SuppressWarnings("serial")
public class PropertiesDialog extends JDialog {

	/**
	 * Configuration settings
	 */
	private static ConfigurationSetting configSettings = new ConfigurationSetting();
	
	/**
	 * Min. dialog size
	 */
	private final int MIN_DIALOG_HIGH = 300;
	private final int MIN_DIALOG_WIDTH = 450;

	/**
	 * Self handle
	 */
	private PropertiesDialog propertiesDialog;

	/**
	 * GUI controller handle
	 */
	private GuiController guiController;

	/* GUI elements */
	private final JButton propertiesOkButton = new JButton("Ok");
	private final JButton propertiesCancelButton = new JButton("Cancel");
	private final JCheckBox indexForceUpdateCheckBox = new JCheckBox("Delete and rebuild the whole index (this cannot be undone)");
	private final FileTreePanel indexTreePanel = new FileTreePanel();
	private final JButton pathRefreshButton = new JButton("Refresh");
	private final JLabel propertiesIndexLabel = new JLabel("Index");
	private final JSeparator propertiesIndexSeparator = new JSeparator();
	private final JLabel propertiesGeneralLabel = new JLabel("General");
	private final JSeparator propertiesGeneralSeparator = new JSeparator();
	private final JCheckBox invisibleFilesCheckBox = new JCheckBox("Show invisible files");
	private final JCheckBox defaultIndexSearchCheckBox = new JCheckBox("Default persistent index search (no search path selected)");

	/**
	 * Constructor
	 * 
	 * @param owner GUI owner of this dialog
	 */
	public PropertiesDialog(JFrame owner, GuiController guiController) {

		super(owner, true);
		this.propertiesDialog = this;
		this.guiController = guiController;
		initGUI();

		/* Get index from API */
		guiController.getIndex();
		
		/* Get configuration settings from XML file */
		PropertiesDialog.loadConfigurationSettings();
		
		/* Set configuration settings */
		this.defaultIndexSearchCheckBox.setSelected(configSettings.defaultIndexSearch);
		this.invisibleFilesCheckBox.setSelected(configSettings.showInvisibleFiles);
		this.indexTreePanel.setShowInvisibleFiles(configSettings.showInvisibleFiles);
		
	}

	/**
	 * Init GUI
	 */
	private void initGUI() {
		
		/* Set title and icon */
		setTitle("Properties");
		setIconImage(Toolkit.getDefaultToolkit().getImage(PathLoadSaveDialog.class.getResource("/de/mround/searchbooster/gui/view/images/SearchBoosterIcon.png")));
		setSize(MIN_DIALOG_WIDTH + 50, MIN_DIALOG_HIGH + 25);

		/* Prevent dialog from being resized to a size too small */
		this.addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent event) {
				propertiesDialog.setSize(Math.max(MIN_DIALOG_WIDTH, propertiesDialog.getWidth()),
						Math.max(MIN_DIALOG_HIGH, propertiesDialog.getHeight()));
			}
		});

		getContentPane().setLayout(new MigLayout("", "[10px:n,left][grow,left][left][left]", "[top][top][top][top][top][top][top][top][grow,top][bottom]"));

		getContentPane().add(this.propertiesIndexLabel, "cell 0 3 4 1,alignx left,aligny baseline");
		this.propertiesIndexSeparator.setOrientation(SwingConstants.VERTICAL);

		getContentPane().add(this.propertiesIndexSeparator, "cell 0 4 1 5,alignx center,growy");
		getContentPane().add(this.indexTreePanel, "cell 1 5 3 3,grow");
		this.pathRefreshButton.setToolTipText("Refresh the index file tree.");

		this.pathRefreshButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {

				indexTreePanel.refresh();
			}
		});
		getContentPane().add(pathRefreshButton, "cell 1 8,sizegroupx button,alignx left,sizegroupy button,aligny center");
		this.propertiesOkButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {

				List<File> recursiveSelectedFiles = new Vector<File>();
				List<File> flatSelectedFiles = new Vector<File>();
				indexTreePanel.getSelectedFiles(recursiveSelectedFiles, flatSelectedFiles);
				guiController.setIndex(
						recursiveSelectedFiles,
						flatSelectedFiles,
						indexForceUpdateCheckBox.isSelected());

				/* Save properties settings to XML file */
				PropertiesDialog.configSettings.showInvisibleFiles = invisibleFilesCheckBox.isSelected();
				PropertiesDialog.configSettings.defaultIndexSearch = defaultIndexSearchCheckBox.isSelected();
				PropertiesDialog.saveConfigurationSettings();

				dispose();
			}
		});

		getContentPane().add(this.propertiesGeneralLabel, "cell 0 0 4 1,alignx left,aligny baseline");
		this.propertiesGeneralSeparator.setOrientation(SwingConstants.VERTICAL);

		getContentPane().add(this.propertiesGeneralSeparator, "cell 0 1 1 2,alignx center,growy");
		this.invisibleFilesCheckBox.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent event) {
				
				/* Set file hiding/showing property of index file tree */
				indexTreePanel.setShowInvisibleFiles(invisibleFilesCheckBox.isSelected());
			}
		});

		getContentPane().add(this.invisibleFilesCheckBox, "cell 1 1 3 1,alignx left,aligny baseline");

		getContentPane().add(this.defaultIndexSearchCheckBox, "cell 1 2 3 1,alignx left,aligny baseline");
		
				getContentPane().add(this.indexForceUpdateCheckBox, "cell 1 4 3 1,alignx left,aligny center");

		getContentPane().add(this.propertiesOkButton, "cell 2 9,sizegroupx button,alignx right,sizegroupy button,aligny center");
		this.propertiesCancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {

				dispose();
			}
		});

		getContentPane().add(this.propertiesCancelButton, "cell 3 9,sizegroupx button,alignx right,sizegroupy button,aligny center");
	}

	public FileTreePanel getIndexTree() {
		return this.indexTreePanel;
	}

	protected static boolean isShowInvisibleFiles() {
		return configSettings.showInvisibleFiles;
	}

	protected static void setShowInvisibleFiles(boolean showInvisibleFiles) {
		PropertiesDialog.configSettings.showInvisibleFiles = showInvisibleFiles;
		saveConfigurationSettings();
	}

	protected static boolean isDefaultIndexSearch() {
		return configSettings.defaultIndexSearch;
	}

	protected static void setDefaultIndexSearch(boolean defaultIndexSearch) {
		PropertiesDialog.configSettings.defaultIndexSearch = defaultIndexSearch;
		saveConfigurationSettings();
	}
	
	protected static void loadConfigurationSettings() {
		GuiConfigXml.getInstance().getConfigurationSettings(configSettings);
	}
	
	protected static void saveConfigurationSettings() {
		GuiConfigXml.getInstance().setConfigurationSettings(configSettings);
		GuiConfigXml.getInstance().saveToXmlFile();
	}
}
