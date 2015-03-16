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
import java.util.List;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;

import net.miginfocom.swing.MigLayout;

import de.mround.searchbooster.gui.controller.GuiConfigXml;
import de.mround.searchbooster.gui.controller.SearchParameter;

/**
 * Dialog for load/save of parameters
 * 
 * @author Tobias Schulz
 */
@SuppressWarnings("serial")
public class ParameterLoadSaveDialog extends JDialog {
	
	/**
	 * Min. dialog size
	 */
	private final int MIN_DIALOG_HIGH = 225;
	private final int MIN_DIALOG_WIDTH = 250;
	
	/**
	 * Self handle
	 */
	private ParameterLoadSaveDialog parameterLoadSaveDialog;
	
	/**
	 * GUI view handle
	 */
	private GuiView guiView;
	
	/**
	 * GUI configuration XML handle
	 */
	private final GuiConfigXml guiConfigXml = GuiConfigXml.getInstance();
	
	/* GUI elements */
	private final JButton parameterSaveButton = new JButton("Save");
	private final JButton parameterLoadButton = new JButton("Load");
	private final JButton parameterRemoveButton = new JButton("Remove");
	private final JButton parameterClearButton = new JButton("Remove all");
	private final JTextField parameterDnfSetsNameTextField = new JTextField();
	@SuppressWarnings("rawtypes")
	private JList parameterDnfSetsList = null;
	private final JScrollPane parameterDnfSetsScrollPanel = new JScrollPane();
	private final JLabel parameterSaveLabel = new JLabel("Save");
	private final JSeparator saveSeparator = new JSeparator();
	private final JLabel parameterSaveNameLabel = new JLabel("Name");
	private final JLabel parameterLoadLabel = new JLabel("Load");
	private final JSeparator loadSeparator = new JSeparator();
	private final JLabel parameterLoadListLabel = new JLabel("Stored search parameter DNF sets");

	/**
	 * Constructor
	 * 
	 * @param owner GUI owner of this dialog
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public ParameterLoadSaveDialog(JFrame owner) {
		
		super(owner, true);
		this.parameterLoadSaveDialog = this;
		this.guiView = (GuiView) owner;
		
		/* Get search parameter configuration from XML file */
		List<String> searchParameterDnfSetsNames = this.guiConfigXml.getSearchParameterDnfSetsNames();
		DefaultListModel parameterDnfSetsListModel = new DefaultListModel();
		for (String searchParameterDnfSetsName : searchParameterDnfSetsNames) {
			parameterDnfSetsListModel.addElement(searchParameterDnfSetsName);
		}
		this.parameterDnfSetsList = new JList(parameterDnfSetsListModel);
		initGUI();
	}
	
	/**
	 * Init GUI
	 */
	private void initGUI() {
		
		/* Set title and icon */
		setTitle("Load/Save search parameter DNF sets");
		setIconImage(Toolkit.getDefaultToolkit().getImage(ParameterLoadSaveDialog.class.getResource("/de/mround/searchbooster/gui/view/images/SearchBoosterIcon.png")));
		
		/* Set layout */
		getContentPane().setLayout(new MigLayout("", "[10px:n,left][left][grow,left][right]", "[top][top][top][top][grow,top]"));
		setSize(MIN_DIALOG_WIDTH + 100, MIN_DIALOG_HIGH + 50);
		
		/* Prevent dialog from being resized to a size too small */
		this.addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent event) {
				parameterLoadSaveDialog.setSize(Math.max(MIN_DIALOG_WIDTH, parameterLoadSaveDialog.getWidth()),
						Math.max(MIN_DIALOG_HIGH, parameterLoadSaveDialog.getHeight()));
			}
		});
		
				
		getContentPane().add(this.parameterSaveLabel, "cell 0 0 4 1,alignx left,aligny baseline");
		this.saveSeparator.setOrientation(SwingConstants.VERTICAL);
		
		getContentPane().add(this.saveSeparator, "cell 0 1,alignx center,growy");
		
		getContentPane().add(this.parameterSaveNameLabel, "cell 1 1,alignx left,aligny baseline");
		this.parameterDnfSetsNameTextField.setToolTipText("Save name");
		getContentPane().add(this.parameterDnfSetsNameTextField, "cell 2 1,growx,aligny baseline");
		/* Save parameter DNF sets on parameterDnfSetsNameTextField action (e.g. Enter) */
		this.parameterDnfSetsNameTextField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				
				parameterSaveButton.doClick();
			}
		});
		
		/* Load path set on pathSetList action (e.g. Enter, double click) */
		this.parameterDnfSetsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		parameterDnfSetsList.addMouseListener(new MouseAdapter() {
			
			@Override
			public void mouseClicked(MouseEvent event) {
				
				if (event.getClickCount() >= 2) {
					event.consume();
					parameterLoadButton.doClick();
				}
			}
		});
		parameterDnfSetsList.addKeyListener(new KeyAdapter() {
			
			@Override
			public void keyPressed(KeyEvent event) {
				
				if (event.getKeyCode() == KeyEvent.VK_ENTER) {
					event.consume();
					parameterLoadButton.doClick();
				}
			}
		});
		
		/* Save parameter DNF sets on parameterSaveButton action (e.g. click) */
		this.parameterSaveButton.setToolTipText("Save the current search parameter DNF set.");
		getContentPane().add(this.parameterSaveButton, "cell 3 1,sizegroupx button,alignx right,sizegroupy button,aligny baseline");
		this.parameterSaveButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				
				/* Get search parameter DNF sets to save */
				List<SearchParameter> searchParameterDnfSets = new Vector<SearchParameter>();
				int searchParameterDnfSetIndex = 0;
				guiView.setActiveParameterDnfSetIndex(0);
				for (int i = 0; i == searchParameterDnfSetIndex; i++) {
					searchParameterDnfSets.add(guiView.getActiveParameterDnfSet());
					guiView.setActiveParameterDnfSetIndex(i + 1);
					searchParameterDnfSetIndex = guiView.getActiveParameterDnfSetIndex();
				}
				
				/* Create search parameter DNF sets */
				String searchParameterDnfSetsName = parameterDnfSetsNameTextField.getText();
				try {
					guiConfigXml.addSearchParameterDnfSets(searchParameterDnfSetsName);
				} catch (IllegalArgumentException e) {
					if (e.getMessage().contains("empty")) {
						JOptionPane.showMessageDialog(parameterLoadSaveDialog, "The search parameter DNF set name is empty.",
								"Save error", JOptionPane.ERROR_MESSAGE);
					}
					else if (e.getMessage().contains("exist")) {
						JOptionPane.showMessageDialog(parameterLoadSaveDialog, "A search parameter DNF set with the same name already exists.",
								"Save error", JOptionPane.ERROR_MESSAGE);
					}
					else {
						JOptionPane.showMessageDialog(parameterLoadSaveDialog, "The search parameter DNF set name contains illegal symbols\nother than characters, numbers and spaces.",
								"Save error", JOptionPane.ERROR_MESSAGE);
					}
					return;
				}

				/* Add parameter DNF sets to XML file */
				for (SearchParameter searchParameterDnfSet : searchParameterDnfSets) {
					guiConfigXml.addSearchParameterSetToDnfSets(searchParameterDnfSetsName, searchParameterDnfSet);
				}
				guiConfigXml.saveToXmlFile();
				
				/* End dialog */
				dispose();
			}
		});
		
		/* Scroll panel for parameterDnfSetsList */		
		
		getContentPane().add(this.parameterLoadLabel, "cell 0 2 4 1,alignx left,aligny baseline");
		
		getContentPane().add(this.parameterLoadListLabel, "cell 1 3 3 1,alignx left,aligny baseline");
		this.loadSeparator.setOrientation(SwingConstants.VERTICAL);
		
		getContentPane().add(this.loadSeparator, "cell 0 3 1 2,alignx center,growy");
		getContentPane().add(this.parameterDnfSetsScrollPanel, "cell 1 4 2 1,grow");
		this.parameterDnfSetsScrollPanel.setViewportView(parameterDnfSetsList);
		
		/* Load parameter DNF sets on parameterLoadButton action (e.g. click) */
		this.parameterLoadButton.setToolTipText("Load the selected search parameter DNF set.");
		getContentPane().add(this.parameterLoadButton, "flowy,cell 3 4,sizegroupx button,alignx right,sizegroupy button,aligny top");
		this.parameterLoadButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				/* Get selected search parameter DNF sets */
				String searchParameterDnfSetsName = getSelectedSearchParameterDnfSetsName();
				if (searchParameterDnfSetsName == null) {
					return;
				}
				List<SearchParameter> searchParameterSets = guiConfigXml.getSearchParameterDnfSets(searchParameterDnfSetsName);
				
				/* Set search parameter DNF sets to parameter card panel */
				guiView.removeAllParameterDnfSets();
				for (int i = 0; i < searchParameterSets.size(); i++) {
					SearchParameterPanel parameterPanel = new SearchParameterPanel();
					parameterPanel.setSearchParameter(searchParameterSets.get(i));
					guiView.addParameterDnfSet(parameterPanel);
				}
				guiView.setActiveParameterDnfSetIndex(0);
				guiView.removeActiveParameterDnfSet();
				
				/* End dialog */
				dispose();
			}
		});
		
		/* Remove parameter DNF sets on parameterRemoveButton action (e.g. click) */
		
		/* Remove all parameter DNF set sets on parameterClearButton action (e.g. click) */
		this.parameterRemoveButton.setToolTipText("Remove the selected search parameter DNF set.");
		getContentPane().add(this.parameterRemoveButton, "cell 3 4,sizegroupx button,alignx right,sizegroupy button,aligny top");
		this.parameterRemoveButton.addActionListener(new ActionListener() {
			@SuppressWarnings("rawtypes")
			public void actionPerformed(ActionEvent e) {
				
				/* Get selected search path set */
				String parameterDnfSetsName = getSelectedSearchParameterDnfSetsName();
				if (parameterDnfSetsName == null) {
					return;
				}
				
				/* Remove parameter DNF sets from XML file */
				guiConfigXml.removeSearchParameterDnfSets(parameterDnfSetsName);
				guiConfigXml.saveToXmlFile();
				
				/* Remove parameter DNF sets from list */
				((DefaultListModel) parameterDnfSetsList.getModel()).removeElement(parameterDnfSetsName);
			}
		});
		this.parameterClearButton.setToolTipText("Remove all search parameter DNF sets.");
		getContentPane().add(this.parameterClearButton, "cell 3 4,sizegroupx button,alignx right,sizegroupy button,aligny top");
		this.parameterClearButton.addActionListener(new ActionListener() {
			@SuppressWarnings("rawtypes")
			public void actionPerformed(ActionEvent e) {

				/* Remove all parameter DNF sets from XML file */
				List<String> searchParameterDnfSetsNames = guiConfigXml.getSearchParameterDnfSetsNames();
				for (String searchParameterDnfSetsName : searchParameterDnfSetsNames) {
					guiConfigXml.removeSearchParameterDnfSets(searchParameterDnfSetsName);
				}
				guiConfigXml.saveToXmlFile();

				/* Remove all path sets from list */
				((DefaultListModel) parameterDnfSetsList.getModel()).removeAllElements();
			}
		});
	}
	
	/**
	 * Get the selected parameter DNF sets name from the GUI list
	 * 
	 * @return Selected parameter DNF sets name
	 */
	private String getSelectedSearchParameterDnfSetsName() {
		
		/* Get selected search parameter DFN sets name */
		String parameterDnfSetsName = (String) parameterDnfSetsList.getSelectedValue();
		if (parameterDnfSetsName == null) {
			JOptionPane.showMessageDialog(parameterLoadSaveDialog, "No search parameter DNF set is selected.",
					"Load error", JOptionPane.ERROR_MESSAGE);
		}
		return parameterDnfSetsName;
	}
}
