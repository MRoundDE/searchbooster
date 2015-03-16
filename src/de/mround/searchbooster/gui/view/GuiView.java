package de.mround.searchbooster.gui.view;

import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
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

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import org.jdesktop.swingx.JXBusyLabel;
import org.jdesktop.swingx.JXCollapsiblePane;
import org.jdesktop.swingx.JXStatusBar;
import net.miginfocom.swing.MigLayout;
import de.mround.searchbooster.gui.controller.GuiController;
import de.mround.searchbooster.gui.controller.SearchParameter;
import de.mround.searchbooster.gui.model.ResultTableModel;
import org.jdesktop.swingx.JXCollapsiblePane.Direction;
import java.awt.BorderLayout;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import java.awt.Insets;

/**
 * GUI View
 * 
 * @author Tobias Schulz
 */
@SuppressWarnings("serial")
public class GuiView extends JFrame {

	/**
	 * Frame min. size
	 */
	private final int MIN_FRAME_HIGH = 475;
	private final int MIN_FRAME_WIDTH = 790;

	/**
	 * Self handle
	 */
	private GuiView guiView;

	/**
	 * GUI controller handle
	 */
	private GuiController guiController;

	/**
	 * Result table model handle
	 */
	private final ResultTableModel resultTableModel;

	/**
	 * GUI elements
	 */
	private JPanel contentPane;
	private final JPanel pathPanel = new JPanel();
	private final JPanel searchPanel = new JPanel();
	private final JPanel resultPanel = new JPanel();
	private final JButton searchButton = new JButton("Search");
	private final JButton pathLoadSaveButton = new JButton("Load/Save");
	private final JScrollPane resultScrollPanel = new JScrollPane();
	private final JTable resultTable = new JTable();
	private final FileTreePanel pathTreePanel = new FileTreePanel();
	private final JButton pathRefreshButton = new JButton("Refresh");
	private final JLabel statusLabel = new JLabel("Ready");
	private final JScrollBar searchDnfScrollBar = new JScrollBar();
	private final JTextField searchDnfNumberTextField = new JTextField();
	private final JButton searchDnfAddButton = new JButton("Add or");
	private final JPanel searchDnfParameterCardPanel = new JPanel(
			new CardLayout(0, 0));
	private final JButton searchDnfRemoveButton = new JButton("Remove");
	private final JButton searchDnfClearButton = new JButton("Remove all");
	private final JPopupMenu resultPopupMenu = new JPopupMenu();
	private final JMenuItem resultOpenMenuItem = new JMenuItem("Open");
	private final JMenuItem resultOpenFolderMenuItem = new JMenuItem(
			"Open folder");
	private int resultPopupMenuRow = 0;
	private final JXCollapsiblePane optionCollapsePanel = new JXCollapsiblePane();
	private final JButton optionCollapseButton = new JButton(
			optionCollapsePanel.getActionMap().get(
					JXCollapsiblePane.TOGGLE_ACTION));
	private final JPanel optionPanel = new JPanel();
	private final JSeparator optionCollapseTopSeparator = new JSeparator();
	private final JSeparator optionCollapseBottomSeparator = new JSeparator();
	private final JXStatusBar statusBar = new JXStatusBar();
	private final JXBusyLabel progressLabel = new JXBusyLabel(new Dimension(15,
			15));
	private final JButton configButton = new JButton("Properties");
	private final JLabel searchParameterLabel = new JLabel("Search parameters");
	private final JSeparator searchParameterSeparator = new JSeparator();
	private final JLabel searchParameterSetNumberLabel = new JLabel("Nr.");
	private final JLabel searchPathLabel = new JLabel("Search paths");
	private final JSeparator searchPathSeparator = new JSeparator();
	private final JButton searchLoadSaveButton = new JButton("Load/Save");
	private final JButton searchCancelButton = new JButton("Cancel");
	private PropertiesDialog propertiesDialog = null;

	/**
	 * Constructor
	 * 
	 * @param rikaController GUI controller
	 */
	public GuiView(GuiController rikaController) {

		this.guiView = this;
		this.guiController = rikaController;
		this.resultTableModel = this.guiController.getResultTableModel();
		PropertiesDialog.loadConfigurationSettings();
		initGUI();
	}

	/**
	 * Initialize the GUI
	 */
	private void initGUI() {

		setIconImage(Toolkit
				.getDefaultToolkit()
				.getImage(
						GuiView.class
						.getResource("/de/mround/searchbooster/gui/view/images/SearchBoosterIcon.png")));
		setTitle("SearchBooster");
		this.resultTable.setModel(this.resultTableModel);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(MIN_FRAME_WIDTH + 160, MIN_FRAME_HIGH);
		setLocationRelativeTo(null);
		contentPane = new JPanel();
		contentPane.setBorder(null);
		setContentPane(contentPane);

		/* Prevent frame from being resized to a size too small */
		this.addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent event) {
				guiView.setSize(Math.max(MIN_FRAME_WIDTH, guiView.getWidth()),
						Math.max(MIN_FRAME_HIGH, guiView.getHeight()));
			}
		});

		statusBar.add(statusLabel);

		this.optionCollapsePanel.setDirection(Direction.RIGHT);

		this.optionCollapsePanel.add(this.optionPanel, BorderLayout.CENTER);
		this.optionPanel.setLayout(new MigLayout("", "[left]",
				"[top][grow,bottom]"));
		this.searchDnfParameterCardPanel.add(new SearchParameterPanel(), "");
		this.optionPanel.add(this.searchPanel, "cell 0 0,grow");
		searchPanel.setLayout(new MigLayout("",
				"[10px:n,left][left][left][left][grow,right][30px:n,right]",
				"[top][grow,top][bottom]"));
		this.searchDnfScrollBar.setToolTipText("Search parameter set number");
		this.searchDnfScrollBar.addAdjustmentListener(new AdjustmentListener() {
			private int prevNumber = 0;

			public void adjustmentValueChanged(AdjustmentEvent event) {

				if (prevNumber < event.getValue()) {
					for (int i = prevNumber; i < event.getValue(); i++) {
						((CardLayout) searchDnfParameterCardPanel.getLayout())
						.next(searchDnfParameterCardPanel);
					}
				} else if (prevNumber > event.getValue()) {
					for (int i = event.getValue(); i < prevNumber; i++) {
						((CardLayout) searchDnfParameterCardPanel.getLayout())
						.previous(searchDnfParameterCardPanel);
					}
				}
				prevNumber = event.getValue();
				searchDnfNumberTextField.setText(String.valueOf(event
						.getValue() + 1));
			}
		});

		this.searchPanel.add(this.searchParameterLabel,
				"cell 0 0 2 1,alignx left,aligny baseline");

		this.searchPanel.add(this.searchParameterSetNumberLabel,
				"cell 2 0,alignx right,aligny baseline");

		this.searchDnfScrollBar.setOrientation(JScrollBar.HORIZONTAL);
		this.searchDnfScrollBar.setBlockIncrement(1);
		this.searchDnfScrollBar.setMinimum(0);
		this.searchDnfScrollBar.setMaximum(1);
		this.searchDnfScrollBar.setVisibleAmount(1);
		this.searchPanel.add(this.searchDnfScrollBar,
				"cell 3 0 2 1,growx,aligny baseline");
		this.searchCancelButton.setToolTipText("Cancel running search.");
		this.searchCancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {

				boolean preFreeze = resultTableModel.isFreeze();
				resultTableModel.setFreeze(true);
				resultTableModel.clear();
				guiController.cancelRunningRequest();
				resultTableModel.setFreeze(preFreeze);
			}
		});
		this.searchParameterSeparator.setOrientation(SwingConstants.VERTICAL);

		this.searchPanel.add(this.searchParameterSeparator,
				"cell 0 1 1 2,alignx center,growy");

		this.searchPanel.add(this.searchDnfParameterCardPanel,
				"cell 1 1 5 1,growx,aligny top");
		this.searchDnfNumberTextField
		.setToolTipText("Search parameter set number");
		this.searchDnfNumberTextField.setText("1");

		this.searchDnfNumberTextField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {

				try {
					searchDnfScrollBar.setValue(Integer
							.parseInt(searchDnfNumberTextField.getText()) - 1);
				} catch (NumberFormatException e) {
				}
				searchDnfNumberTextField.setText(String
						.valueOf(searchDnfScrollBar.getValue() + 1));
			}
		});

		this.searchPanel.add(this.searchDnfNumberTextField,
				"cell 5 0,growx,aligny baseline");
		this.searchDnfAddButton
		.setToolTipText("<html>Add a new search parameter set. The search result will be<br>the concatenation of all search parameter set results.</html>");
		this.searchDnfAddButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {

				addParameterDnfSet(null);
			}
		});

		this.searchPanel
		.add(this.searchDnfAddButton,
				"flowx,cell 1 2,sizegroupx button,alignx left,sizegroupy button,aligny center");
		this.searchDnfRemoveButton
		.setToolTipText("Remove the current search parameter set.");
		this.searchDnfRemoveButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {

				removeActiveParameterDnfSet();
			}
		});

		this.searchPanel
		.add(this.searchDnfRemoveButton,
				"cell 2 2,sizegroupx button,alignx left,sizegroupy button,aligny center");
		this.searchDnfClearButton
		.setToolTipText("Remove all search parameter sets.");
		this.searchDnfClearButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent event) {

				removeAllParameterDnfSets();

			}
		});

		this.searchPanel
		.add(this.searchDnfClearButton,
				"cell 3 2,sizegroupx button,alignx left,sizegroupy button,aligny center");
		this.searchLoadSaveButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent event) {
				ParameterLoadSaveDialog parameterLoadSaveDialog = new ParameterLoadSaveDialog(
						guiView);
				parameterLoadSaveDialog
				.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
				Point p = guiView.getLocation();
				p.translate(10, 25);
				parameterLoadSaveDialog.setLocation(p);
				parameterLoadSaveDialog.setVisible(true);
			}
		});
		this.searchLoadSaveButton
		.setToolTipText("Load or save search parameters.");

		this.searchPanel.add(this.searchLoadSaveButton,
				"cell 4 2 2 1,alignx left,aligny center");
		this.optionPanel.add(this.pathPanel, "cell 0 1,grow");

		this.pathPanel.setLayout(new MigLayout("",
				"[10px:n,left][left][left][grow,right]",
				"[top][grow,top][bottom]"));

		this.pathPanel.add(this.searchPathLabel,
				"cell 0 0 4 1,alignx left,aligny baseline");
		this.searchPathSeparator.setOrientation(SwingConstants.VERTICAL);

		this.pathPanel.add(this.searchPathSeparator,
				"cell 0 1 1 2,alignx center,growy");
		this.pathPanel.add(this.pathTreePanel, "cell 1 1 3 1,grow");
		this.pathRefreshButton
		.setToolTipText("Refresh the search path file tree.");

		this.pathRefreshButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {

				pathTreePanel.refresh();
			}
		});
		this.pathTreePanel.setShowInvisibleFiles(PropertiesDialog
				.isShowInvisibleFiles());

		this.pathPanel
		.add(this.pathRefreshButton,
				"cell 1 2,sizegroupx button,alignx left,sizegroupy button,aligny center");
		this.pathLoadSaveButton.setToolTipText("Load or save search paths.");
		this.pathLoadSaveButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {

				PathLoadSaveDialog pathLoadSaveDialog = new PathLoadSaveDialog(
						guiView, pathTreePanel);
				pathLoadSaveDialog
				.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
				Point p = guiView.getLocation();
				p.translate(10, 25);
				pathLoadSaveDialog.setLocation(p);
				pathLoadSaveDialog.setVisible(true);
			}
		});
		contentPane.setLayout(new BorderLayout(0, 0));

		this.pathPanel
		.add(this.pathLoadSaveButton,
				"cell 2 2,sizegroupx button,alignx left,sizegroupy button,aligny center");
		contentPane.add(optionCollapsePanel, BorderLayout.WEST);

		contentPane.add(resultPanel, BorderLayout.CENTER);
		this.resultPanel.setLayout(new MigLayout("",
				"[left][left][grow,left][right]",
				"[top][grow,top][center][grow,bottom]"));
		this.searchButton
		.setToolTipText("Search files according to search parameters and search paths.");

		searchButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				resultTableModel.clear();

				List<File> recursiveSelectedFiles = new Vector<File>();
				List<File> flatSelectedFiles = new Vector<File>();
				pathTreePanel.getSelectedFiles(recursiveSelectedFiles,
						flatSelectedFiles);

				if (recursiveSelectedFiles.isEmpty()
						&& flatSelectedFiles.isEmpty()
						&& !PropertiesDialog.isDefaultIndexSearch()) {
					/*
					 * Button Name Meaning
					 * 
					 * JOptionPane.YES_OPTION Yes Search index
					 * JOptionPane.NO_OPTION Yes, always Search index and
					 * remember decision JOptionPane.CANCEL_OPTION No Don't
					 * search index, abort search JOptionPane.CLOSE_OPTION
					 * (Dialog closed) Don't search index, abort search
					 */
					Object[] options = { "Yes", "Yes, always", "No" };
					int decision = JOptionPane
							.showOptionDialog(
									guiView,
									"No search path is selected.\nDo you want to search the index?",
									"Index search",
									JOptionPane.YES_NO_CANCEL_OPTION,
									JOptionPane.QUESTION_MESSAGE, null,
									options, options[0]);
					if ((decision == JOptionPane.CANCEL_OPTION)
							|| (decision == JOptionPane.CLOSED_OPTION)) {
						return;
					} else {
						if (decision == JOptionPane.NO_OPTION) {
							/*
							 * Remember to search index if search path list is
							 * empty
							 */
							PropertiesDialog.setDefaultIndexSearch(true);
						}
					}
				}

				List<SearchParameter> searchParameters = new Vector<SearchParameter>();
				for (Component searchParameterPanel : searchDnfParameterCardPanel
						.getComponents()) {
					searchParameters
					.add(((SearchParameterPanel) searchParameterPanel)
							.getSearchParameter());
				}

				guiController.search(recursiveSelectedFiles, flatSelectedFiles,
						searchParameters);
			}
		});

		resultPanel
		.add(searchButton,
				"cell 1 0,sizegroupx button,alignx left,sizegroupy button,aligny center");

		this.resultPanel
		.add(this.searchCancelButton,
				"cell 2 0,sizegroupx button,alignx left,sizegroupy button,aligny center");

		this.resultPanel.add(this.resultScrollPanel, "cell 1 1 3 3,grow");
		this.resultTable.setAutoCreateRowSorter(true);
		this.resultTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		TableColumnAutoSizer.setupTable(this.resultTable);
		this.resultTable.getModel().addTableModelListener(
				new TableModelListener() {
					@Override
					public void tableChanged(TableModelEvent e) {
						TableColumnAutoSizer.updateWidths(resultTable);
					}
				});
		this.resultTable.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent event) {
				if (event.getClickCount() >= 2) {
					int selectedResultTableModelRow = resultTable
							.convertRowIndexToModel(resultTable
									.getSelectedRow());
					try {
						Desktop.getDesktop()
						.open(resultTableModel
								.getFileAt(selectedResultTableModelRow));
					} catch (IOException e) {
						System.err.println(e.getMessage());
					}
					event.consume();
				}
			}
		});
		this.resultTable.addKeyListener(new KeyAdapter() {

			@Override
			public void keyPressed(KeyEvent event) {
				if (event.getKeyCode() == KeyEvent.VK_ENTER) {
					int selectedResultTableModelRow = resultTable
							.convertRowIndexToModel(resultTable
									.getSelectedRow());
					try {
						Desktop.getDesktop()
						.open(resultTableModel
								.getFileAt(selectedResultTableModelRow));
					} catch (IOException e) {
						System.err.println(e.getMessage());
					}
					event.consume();
				}
			}
		});
		this.resultPopupMenu.addPopupMenuListener(new PopupMenuListener() {
			public void popupMenuCanceled(PopupMenuEvent arg0) {
			}

			public void popupMenuWillBecomeInvisible(PopupMenuEvent arg0) {
			}

			public void popupMenuWillBecomeVisible(PopupMenuEvent event) {

				resultOpenMenuItem.setEnabled(false);
				resultOpenFolderMenuItem.setEnabled(false);

				try {
					resultPopupMenuRow = resultTable.convertRowIndexToModel(resultTable
							.rowAtPoint(resultTable.getMousePosition()));
				} catch (Exception e) {
					System.err.println(e.getMessage());
				}

				/* Check if file can be opened */
				File file = resultTableModel.getFileAt(resultPopupMenuRow);
				if (file.canRead()) {
					resultOpenMenuItem.setEnabled(true);
				}

				/* Check if parent folder exists */
				File parent = file.getParentFile();
				if (parent != null) {
					if (parent.exists()) {
						resultOpenFolderMenuItem.setEnabled(true);
					}
				}
			}
		});

		addPopup(this.resultTable, this.resultPopupMenu);
		this.resultOpenMenuItem.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent event) {

				if (resultPopupMenuRow >= 0) {
					try {
						Desktop.getDesktop().open(
								resultTableModel.getFileAt(resultPopupMenuRow));
					} catch (IOException e) {
						System.err.println(e.getMessage());
					}
				}
			}
		});

		this.resultPopupMenu.add(this.resultOpenMenuItem);
		this.resultOpenFolderMenuItem.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent event) {

				if (resultPopupMenuRow >= 0) {
					try {
						Desktop.getDesktop().open(
								resultTableModel.getFileAt(resultPopupMenuRow)
								.getParentFile());
					} catch (IOException e) {
						System.err.println(e.getMessage());
					}
				}
			}
		});

		this.resultPopupMenu.add(this.resultOpenFolderMenuItem);

		this.resultScrollPanel.setViewportView(this.resultTable);
		this.optionCollapseTopSeparator.setOrientation(SwingConstants.VERTICAL);

		resultPanel.add(this.optionCollapseTopSeparator,
				"cell 0 0 1 2,alignx center,growy");
		this.optionCollapseButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {

				if (optionCollapsePanel.isCollapsed()) {
					optionCollapseButton.setText("<");
				} else {
					optionCollapseButton.setText(">");
				}
			}
		});
		this.optionCollapseButton.setMargin(new Insets(10, 2, 10, 2));
		this.optionCollapseButton.setText("<");
		resultPanel.add(this.optionCollapseButton,
				"cell 0 2,alignx center,aligny center");

		contentPane.add(this.statusBar, BorderLayout.SOUTH);

		this.optionCollapseBottomSeparator
		.setOrientation(SwingConstants.VERTICAL);

		resultPanel.add(this.optionCollapseBottomSeparator,
				"cell 0 3,alignx center,growy");
		this.configButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {

				propertiesDialog = new PropertiesDialog(guiView, guiController);
				propertiesDialog
				.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
				Point p = guiView.getLocation();
				p.translate(10, 25);
				propertiesDialog.setLocation(p);
				propertiesDialog.setVisible(true);
				pathTreePanel.setShowInvisibleFiles(PropertiesDialog
						.isShowInvisibleFiles());
			}
		});
		this.configButton.setToolTipText("Configure SearchBooster settings.");

		this.resultPanel
		.add(this.configButton,
				"cell 3 0,sizegroupx button,alignx right,sizegroupy button,aligny center");
		this.progressLabel.setDirection(JXBusyLabel.Direction.RIGHT);

		statusBar.add(progressLabel, 0);
	}

	public void setStatusLabelText(String text, boolean busy) {

		if (text == null) {
			text = "";
		}
		this.statusLabel.setText(text);

		progressLabel.setBusy(busy);
	}

	private static void addPopup(Component component, final JPopupMenu popup) {
		component.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showMenu(e);
				}
			}

			public void mouseReleased(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showMenu(e);
				}
			}

			private void showMenu(MouseEvent e) {
				popup.show(e.getComponent(), e.getX(), e.getY());
			}
		});
	}

	public FileTreePanel getIndexTree() {

		if (this.propertiesDialog != null) {
			return this.propertiesDialog.getIndexTree();
		} else {
			return null;
		}
	}

	protected void addParameterDnfSet(SearchParameterPanel searchParameterPanel) {
		if (searchParameterPanel != null) {
			searchDnfParameterCardPanel.add(searchParameterPanel, "");
		} else {
			searchDnfParameterCardPanel.add(new SearchParameterPanel(), "");
		}
		searchDnfParameterCardPanel.validate();
		searchDnfScrollBar.setMaximum(searchDnfParameterCardPanel
				.getComponentCount());
		searchDnfScrollBar.setValue(searchDnfParameterCardPanel
				.getComponentCount() - 1);
	}

	protected void removeActiveParameterDnfSet() {
		if (searchDnfParameterCardPanel.getComponentCount() > 1) {

			int scrollBarValue = searchDnfScrollBar.getValue();
			searchDnfScrollBar.setValue(scrollBarValue - 1);
			searchDnfParameterCardPanel.remove(scrollBarValue);
			searchDnfParameterCardPanel.validate();
			searchDnfScrollBar.setMaximum(searchDnfParameterCardPanel
					.getComponentCount());
		} else {
			((SearchParameterPanel) searchDnfParameterCardPanel.getComponent(0))
			.restoreDefaults();
		}
	}

	protected void removeAllParameterDnfSets() {
		searchDnfScrollBar.setValue(0);
		searchDnfScrollBar.setMaximum(1);
		searchDnfParameterCardPanel.removeAll();
		searchDnfParameterCardPanel.add(new SearchParameterPanel(), "");
		searchDnfParameterCardPanel.validate();
	}

	protected int getActiveParameterDnfSetIndex() {
		return searchDnfScrollBar.getValue();
	}

	protected void setActiveParameterDnfSetIndex(int index) {
		searchDnfScrollBar.setValue(index);
	}

	protected SearchParameter getActiveParameterDnfSet() {
		return ((SearchParameterPanel) searchDnfParameterCardPanel
				.getComponent(getActiveParameterDnfSetIndex()))
				.getSearchParameter();
	}
}
