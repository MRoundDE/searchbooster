package de.mround.searchbooster.gui.view;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

import net.miginfocom.swing.MigLayout;
import de.mround.searchbooster.gui.controller.GuiConfigXml;
import de.mround.searchbooster.gui.controller.SearchPath;
import javax.swing.JLabel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;

/**
 * Dialog for load/save of paths from a file tree panel
 * 
 * @author Tobias Schulz
 */
@SuppressWarnings("serial")
public class PathLoadSaveDialog extends JDialog {
	
	/**
	 * Min. dialog size
	 */
	private final int MIN_DIALOG_HIGH = 225;
	private final int MIN_DIALOG_WIDTH = 250;
	
	/**
	 * Self handle
	 */
	private PathLoadSaveDialog pathLoadSaveDialog;
	
	/**
	 * File tree handle
	 */
	private FileTreePanel fileTreePanel;
	
	/**
	 * GUI configuration XML handle
	 */
	private final GuiConfigXml guiConfigXml = GuiConfigXml.getInstance();
	
	/* GUI elements */
	private final JButton pathSaveButton = new JButton("Save");
	private final JButton pathLoadButton = new JButton("Load");
	private final JButton pathRemoveButton = new JButton("Remove");
	private final JButton pathClearButton = new JButton("Remove all");
	private final JTextField pathSetNameTextField = new JTextField();
	@SuppressWarnings("rawtypes")
	private JList pathSetList = null;
	private final JScrollPane pathSetScrollPanel = new JScrollPane();
	private final JLabel pathSaveLabel = new JLabel("Save");
	private final JSeparator saveSeparator = new JSeparator();
	private final JLabel pathSaveNameLabel = new JLabel("Name");
	private final JLabel pathLoadLabel = new JLabel("Load");
	private final JSeparator LoadSeparator = new JSeparator();
	private final JLabel pathLoadListLabel = new JLabel("Stored search paths");

	/**
	 * Constructor
	 * 
	 * @param owner GUI owner of this dialog
	 * @param fileTreePanel File tree panel containing paths to load/save
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public PathLoadSaveDialog(JFrame owner, FileTreePanel fileTreePanel) {
		
		super(owner, true);
		this.pathLoadSaveDialog = this;
		this.fileTreePanel = fileTreePanel;
		
		/* Get search path configuration from XML file */
		List<String> searchPathSetNames = this.guiConfigXml.getSearchPathSetNames();
		DefaultListModel pathSetListModel = new DefaultListModel();
		for (String searchPathSetName : searchPathSetNames) {
			pathSetListModel.addElement(searchPathSetName);
		}
		this.pathSetList = new JList(pathSetListModel);
		initGUI();
	}
	
	/**
	 * Init GUI
	 */
	private void initGUI() {
		
		/* Set title and icon */
		setTitle("Load/Save search path set");
		setIconImage(Toolkit.getDefaultToolkit().getImage(PathLoadSaveDialog.class.getResource("/de/mround/searchbooster/gui/view/images/SearchBoosterIcon.png")));
		
		/* Set layout */
		getContentPane().setLayout(new MigLayout("", "[10px:n,left][left][grow,left][right]", "[top][top][top][top][grow,top]"));
		setSize(MIN_DIALOG_WIDTH + 100, MIN_DIALOG_HIGH + 50);
		
		/* Prevent dialog from being resized to a size too small */
		this.addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent event) {
				pathLoadSaveDialog.setSize(Math.max(MIN_DIALOG_WIDTH, pathLoadSaveDialog.getWidth()),
						Math.max(MIN_DIALOG_HIGH, pathLoadSaveDialog.getHeight()));
			}
		});
			
		getContentPane().add(this.pathSaveLabel, "cell 0 0 4 1,alignx left,aligny baseline");
		this.saveSeparator.setOrientation(SwingConstants.VERTICAL);
		
		getContentPane().add(this.saveSeparator, "cell 0 1,alignx center,growy");
		
		getContentPane().add(this.pathSaveNameLabel, "cell 1 1,alignx left,aligny baseline");
		this.pathSetNameTextField.setToolTipText("Save name");
		getContentPane().add(this.pathSetNameTextField, "cell 2 1,growx,aligny baseline");
		/* Save path set on pathSetNameTextField action (e.g. Enter) */	
		this.pathSetNameTextField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				
				pathSaveButton.doClick();
			}
		});
		
		/* Load path set on pathSetList action (e.g. Enter, double click) */
		this.pathSetList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		pathSetList.addMouseListener(new MouseAdapter() {
			
			@Override
			public void mouseClicked(MouseEvent event) {
				
				if (event.getClickCount() >= 2) {
					event.consume();
					pathLoadButton.doClick();
				}
			}
		});
		pathSetList.addKeyListener(new KeyAdapter() {
			
			@Override
			public void keyPressed(KeyEvent event) {
				
				if (event.getKeyCode() == KeyEvent.VK_ENTER) {
					event.consume();
					pathLoadButton.doClick();
				}
			}
		});
		
		/* Save path set on pathSaveButton action (e.g. click) */
		this.pathSaveButton.setToolTipText("Save the current search path set.");
		getContentPane().add(this.pathSaveButton, "cell 3 1,sizegroupx button,alignx right,sizegroupy button,aligny baseline");
		this.pathSaveButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				
				/* Get search paths to save */
				List<File> recursiveSelectedFiles = new Vector<File>();
				List<File> flatSelectedFiles = new Vector<File>();
				fileTreePanel.getSelectedFiles(recursiveSelectedFiles, flatSelectedFiles);
				if ((recursiveSelectedFiles.size() == 0) && (flatSelectedFiles.size() == 0)) {
					JOptionPane.showMessageDialog(pathLoadSaveDialog, "Select search paths to save first.",
							"Save error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				
				/* Create search path set */
				String searchPathSetName = pathSetNameTextField.getText();
				try {
					guiConfigXml.addSearchPathSet(searchPathSetName);
				} catch (IllegalArgumentException e) {
					if (e.getMessage().contains("empty")) {
						JOptionPane.showMessageDialog(pathLoadSaveDialog, "The search path set name is empty.",
								"Save error", JOptionPane.ERROR_MESSAGE);
					}
					else if (e.getMessage().contains("exist")) {
						JOptionPane.showMessageDialog(pathLoadSaveDialog, "A search path set with the same name already exists.",
								"Save error", JOptionPane.ERROR_MESSAGE);
					}
					else {
						JOptionPane.showMessageDialog(pathLoadSaveDialog, "The search path set name contains illegal symbols\nother than characters, numbers and spaces.",
								"Save error", JOptionPane.ERROR_MESSAGE);
					}
					return;
				}

				/* Add path set from file tree to XML file */
				SearchPath searchPath = new SearchPath();
				searchPath.recursionDepth = -1;
				for (File file : recursiveSelectedFiles) {
					try {
						searchPath.path = file.getCanonicalPath();
					} catch (IOException e) {
						guiConfigXml.removeSearchPathSet(searchPathSetName);
						return;
					}
					guiConfigXml.addSearchPathToSet(searchPathSetName, searchPath);
				}
				searchPath.recursionDepth = 0;
				for (File file : flatSelectedFiles) {
					try {
						searchPath.path = file.getCanonicalPath();
					} catch (IOException e) {
						guiConfigXml.removeSearchPathSet(searchPathSetName);
						return;
					}
					guiConfigXml.addSearchPathToSet(searchPathSetName, searchPath);
				}
				guiConfigXml.saveToXmlFile();
				
				/* End dialog */
				dispose();
			}
		});
		
		/* Scroll panel for pathSetList */		
		
		getContentPane().add(this.pathLoadLabel, "cell 0 2 4 1,alignx left,aligny baseline");
		
		getContentPane().add(this.pathLoadListLabel, "cell 1 3 3 1,alignx left,aligny baseline");
		this.LoadSeparator.setOrientation(SwingConstants.VERTICAL);
		
		getContentPane().add(this.LoadSeparator, "cell 0 3 1 2,alignx center,growy");
		getContentPane().add(this.pathSetScrollPanel, "cell 1 4 2 1,grow");
		this.pathSetScrollPanel.setViewportView(pathSetList);
		
		/* Load path set on pathLoadButton action (e.g. click) */
		this.pathLoadButton.setToolTipText("Load the selected search path set.");
		getContentPane().add(this.pathLoadButton, "flowy,cell 3 4,sizegroupx button,alignx right,sizegroupy button,aligny top");
		this.pathLoadButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				/* Get selected search path set */
				String searchPathSetName = getSelectedSearchPathSetName();
				if (searchPathSetName == null) {
					return;
				}
				List<SearchPath> searchPaths = guiConfigXml.getSearchPathsFromSet(searchPathSetName);
				
				/* Set file tree paths */
				List<File> recursiveSelectedFiles = new Vector<File>();
				List<File> flatSelectedFiles = new Vector<File>();
				for (SearchPath searchPath : searchPaths) {
					if (searchPath.recursionDepth != 0) {
						recursiveSelectedFiles.add(new File(searchPath.path));
					}
					else {
						flatSelectedFiles.add(new File(searchPath.path));
					}
				}
				fileTreePanel.setSelectedFiles(recursiveSelectedFiles, flatSelectedFiles);
				
				/* End dialog */
				dispose();
			}
		});
		
		/* Remove path set on pathRemoveButton action (e.g. click) */
		
		/* Remove all path sets on pathClearButton action (e.g. click) */
		this.pathRemoveButton.setToolTipText("Remove the selected search path set.");
		getContentPane().add(this.pathRemoveButton, "cell 3 4,sizegroupx button,alignx right,sizegroupy button,aligny top");
		this.pathRemoveButton.addActionListener(new ActionListener() {
			@SuppressWarnings("rawtypes")
			public void actionPerformed(ActionEvent e) {
				
				/* Get selected search path set */
				String pathSetName = getSelectedSearchPathSetName();
				if (pathSetName == null) {
					return;
				}
				
				/* Remove path set from XML file */
				guiConfigXml.removeSearchPathSet(pathSetName);
				guiConfigXml.saveToXmlFile();
				
				/* Remove path set from list */
				((DefaultListModel) pathSetList.getModel()).removeElement(pathSetName);
			}
		});
		this.pathClearButton.setToolTipText("Remove all search path sets.");
		getContentPane().add(this.pathClearButton, "cell 3 4,sizegroupx button,alignx right,sizegroupy button,aligny top");
		this.pathClearButton.addActionListener(new ActionListener() {
			@SuppressWarnings("rawtypes")
			public void actionPerformed(ActionEvent e) {

				/* Remove all path sets from XML file */
				List<String> searchPathSetNames = guiConfigXml.getSearchPathSetNames();
				for (String searchPathSetName : searchPathSetNames) {
					guiConfigXml.removeSearchPathSet(searchPathSetName);
				}
				guiConfigXml.saveToXmlFile();

				/* Remove all path sets from list */
				((DefaultListModel) pathSetList.getModel()).removeAllElements();
			}
		});
	}
	
	/**
	 * Get the selected path set name from the GUI list
	 * 
	 * @return Selected path set name
	 */
	private String getSelectedSearchPathSetName() {
		
		/* Get selected search path set name */
		String pathSetName = (String) pathSetList.getSelectedValue();
		if (pathSetName == null) {
			JOptionPane.showMessageDialog(pathLoadSaveDialog, "No search path set is selected.",
					"Load error", JOptionPane.ERROR_MESSAGE);
		}
		return pathSetName;
	}
}
