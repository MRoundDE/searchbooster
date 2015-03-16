package de.mround.searchbooster.gui.view;

import java.awt.BorderLayout;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.filechooser.FileSystemView;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import ca.ansir.swing.tristate.TriState;
import ca.ansir.swing.tristate.TriStateEvent;
import ca.ansir.swing.tristate.TriStateListener;
import ca.ansir.swing.tristate.TriStateTreeHandler;
import ca.ansir.swing.tristate.TriStateTreeNode;

/**
 * File tree with tristate check boxes
 * 
 * @author Tobias Schulz
 */
@SuppressWarnings("serial")
public class FileTreePanel extends JPanel {

	/**
	 * File tree root node
	 */
	private final DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) new FileTriStateTreeNode(
			"Root");

	/**
	 * File tree handle
	 */
	private JTree fileTree = null;

	/**
	 * File tree event listener
	 */
	private FileTreeListener fileTreeListener = null;

	/**
	 * Show invisible files flag
	 */
	private boolean showInvisibleFiles = false;

	/**
	 * Constructor
	 */
	public FileTreePanel() {

		/* Border layout */
		super(new BorderLayout());

		/* Setup file tree */
		this.addFileTreeSystemRoots();

		this.fileTree = new JTree(rootNode);
		new TriStateTreeHandler(fileTree);
		fileTree.setToggleClickCount(Integer.MAX_VALUE); // Disable folder open
		// action on click
		fileTree.setShowsRootHandles(true);
		fileTree.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
		fileTree.setRootVisible(false);

		/* File tree controller */
		this.fileTreeListener = new FileTreeListener();
		addFileTreeTriStateComboBoxListeners(rootNode, this.fileTreeListener);
		fileTree.addTreeExpansionListener(fileTreeListener);

		/* Scroll panel */
		this.add(new JScrollPane(fileTree));
	}

	/**
	 * Adds file system roots to file tree
	 */
	private void addFileTreeSystemRoots() {

		/*
		 * File system roots are determined by the file function (FileSystemView
		 * does return different file system roots)
		 */
		File[] fileSystemRoots = File.listRoots();
		if (fileSystemRoots != null) {
			Arrays.sort(fileSystemRoots);
			for (File fileSystemRoot : fileSystemRoots) {
				/* Adding all system roots */
				DefaultMutableTreeNode systemRootNode = new FileTriStateTreeNode(
						fileSystemRoot);

				/* Create dummy node to enable root handles */
				FileTriStateTreeNode dummyNode = new FileTriStateTreeNode("..");
				dummyNode.setEnabled(false);
				systemRootNode.add((DefaultMutableTreeNode) dummyNode);

				rootNode.add(systemRootNode);
			}
		}
	}

	/**
	 * Adds TriStateListener to all TriStateTreeNodes recursively
	 * 
	 * @param node
	 *            Root node
	 * @param fileTreeListener
	 *            TriStateListener
	 */
	private void addFileTreeTriStateComboBoxListeners(
			DefaultMutableTreeNode node, FileTreeListener fileTreeListener) {

		if (node instanceof TriStateTreeNode) {
			TriStateTreeNode triStateNode = (TriStateTreeNode) node;
			triStateNode.addTriStateListener(fileTreeListener);

			/* Recursion */
			if (!node.isLeaf()) {
				/* Add file tree listener to all children */
				for (int i = 0, childCount = node.getChildCount(); i < childCount; i++) {
					DefaultMutableTreeNode child = (DefaultMutableTreeNode) node
							.getChildAt(i);
					addFileTreeTriStateComboBoxListeners(child,
							fileTreeListener);
				}
			}
		}
	}

	/**
	 * Set all recursive and flat selected files/folders of the file tree
	 * 
	 * @param recursiveSelectedFiles
	 *            List of recursive selected files found
	 * @param flatSelectedFiles
	 *            List of flat selected files found
	 */
	public void setSelectedFiles(List<File> recursiveSelectedFiles,
			List<File> flatSelectedFiles) {

		/* Save expanded file tree paths */
		Enumeration<TreePath> expandedPaths = this.fileTree
				.getExpandedDescendants(new TreePath(this.rootNode));

		/* Clear all selections */
		for (int i = this.rootNode.getChildCount() - 1; i >= 0; i--) {
			((FileTriStateTreeNode) this.rootNode.getChildAt(i))
			.setState(TriState.UNSELECTED);
		}

		/* Traverse full file tree and set selections */
		this.setSelectedFiles((FileTriStateTreeNode) this.rootNode,
				recursiveSelectedFiles, flatSelectedFiles);

		/* Expand tree if necessary to set file selection of non contained nodes */
		for (File file : flatSelectedFiles) {
			boolean continueNextNewFile = false;
			FileTriStateTreeNode fileNode = (FileTriStateTreeNode) this.rootNode;
			if (file.exists()) {
				/* Check visibility of the file */
				if (file.isHidden() && !this.showInvisibleFiles) {
					continue;
				}

				/* Get list of files on the path to the new file */
				List<File> filePathFiles = this.getFilePathFiles(file);
				for (File pathFile : filePathFiles) {
					/* Expand folder */
					this.fileTree.expandPath(new TreePath(fileNode.getPath()));

					/* Search children for the file path file */
					int nodeIndex = -1;
					for (int i = (fileNode.getChildCount() - 1); i >= 0; i--) {
						File fileNodeFile = ((FileTriStateTreeNode) fileNode
								.getChildAt(i)).getFile();
						if (fileNodeFile != null) {
							if (fileNodeFile.equals(pathFile)) {
								nodeIndex = i;
							}
						}
					}
					if (nodeIndex >= 0) {
						try {
							fileNode = (FileTriStateTreeNode) fileNode
									.getChildAt(nodeIndex);
						} catch (ArrayIndexOutOfBoundsException e) {
							System.err.println(e.getMessage());
						}
					} else {
						/* Unable to traverse file tree to new file */
						continueNextNewFile = true;
					}
				}
				if (continueNextNewFile) {
					continue;
				}

				/*
				 * Expand folder (add folder files to file tree to be able to
				 * select folder flat)
				 */
				this.fileTree.expandPath(new TreePath(fileNode.getPath()));

				/* Select first child (.. file) */
				((FileTriStateTreeNode) fileNode.getChildAt(0))
				.setState(TriState.SELECTED);
			}
		}

		for (File file : recursiveSelectedFiles) {
			boolean continueNextNewFile = false;
			FileTriStateTreeNode fileNode = (FileTriStateTreeNode) this.rootNode;
			if (file.exists()) {
				/* Check visibility of the file */
				if (file.isHidden() && !this.showInvisibleFiles) {
					continue;
				}

				List<File> filePathFiles = this.getFilePathFiles(file);
				for (File pathFile : filePathFiles) {
					/* Expand folder */
					this.fileTree.expandPath(new TreePath(fileNode.getPath()));

					/* Search children for the file path file */
					int nodeIndex = -1;
					for (int i = (fileNode.getChildCount() - 1); i >= 0; i--) {
						File fileNodeFile = ((FileTriStateTreeNode) fileNode
								.getChildAt(i)).getFile();
						if (fileNodeFile != null) {
							if (fileNodeFile.equals(pathFile)) {
								nodeIndex = i;
							}
						}
					}

					if (nodeIndex >= 0) {
						try {
							fileNode = (FileTriStateTreeNode) fileNode
									.getChildAt(nodeIndex);
						} catch (ArrayIndexOutOfBoundsException e) {
							System.err.println(e.getMessage());
						}
					} else {
						/* Unable to traverse file tree to new file */
						continueNextNewFile = true;
					}
				}
				if (continueNextNewFile) {
					continue;
				}

				fileNode.setState(TriState.SELECTED);
			}
		}

		((DefaultTreeModel) this.fileTree.getModel())
		.nodeStructureChanged(this.rootNode);

		/* Reexpand available file tree paths */
		if (expandedPaths != null) {
			while (expandedPaths.hasMoreElements()) {
				this.fileTree.expandPath(expandedPaths.nextElement());
			}
		}
	}

	/**
	 * Set all recursive and flat selected files/folders of the file tree
	 * 
	 * @param node
	 *            File tree node to start the recursive setting
	 * @param recursiveSelectedFiles
	 *            List of recursive selected files found
	 * @param flatSelectedFiles
	 *            List of flat selected files found
	 */
	private void setSelectedFiles(FileTriStateTreeNode node,
			List<File> recursiveSelectedFiles, List<File> flatSelectedFiles) {

		if (recursiveSelectedFiles == null) {
			throw new NullPointerException(
					"Recursive selected file list is a null pointer.");
		}
		if (flatSelectedFiles == null) {
			throw new NullPointerException(
					"Flat selected file list is a null pointer.");
		}

		File file = node.getFile();

		if (file == null) {
			/* .. file or root */
		} else if (recursiveSelectedFiles.contains(file)) {
			/* Recursive selected */
			recursiveSelectedFiles.remove(file);
			node.setState(TriState.SELECTED);
			return;
		} else if (flatSelectedFiles.contains(file)) {
			/* Flat selected */
			flatSelectedFiles.remove(file);

			/* Select first child (.. file) */
			((FileTriStateTreeNode) node.getChildAt(0))
			.setState(TriState.SELECTED);
		}

		/* Recursion */
		for (int i = node.getChildCount() - 1; i >= 0; i--) {
			this.setSelectedFiles((FileTriStateTreeNode) node.getChildAt(i),
					recursiveSelectedFiles, flatSelectedFiles);
		}
	}

	/**
	 * Get list of files on the file path to the specified file
	 */
	private List<File> getFilePathFiles(File file) {

		List<File> filePathFiles = new Vector<File>();

		filePathFiles.add(file);
		File nextFilePathFile = file.getParentFile();

		while (nextFilePathFile != null) {
			filePathFiles.add(0, nextFilePathFile);
			nextFilePathFile = nextFilePathFile.getParentFile();
		}

		return filePathFiles;
	}

	/**
	 * Get all recursive and flat selected files/folders of the file tree
	 * 
	 * @param recursiveSelectedFiles
	 *            List of recursive selected files found
	 * @param flatSelectedFiles
	 *            List of flat selected files found
	 */
	public void getSelectedFiles(List<File> recursiveSelectedFiles,
			List<File> flatSelectedFiles) {

		/*
		 * Traverse file tree from root and search for all recursive selected
		 * folders/files
		 */
		this.getSelectedFiles((FileTriStateTreeNode) this.rootNode,
				recursiveSelectedFiles, flatSelectedFiles);
	}

	/**
	 * Get all recursive and flat selected files/folders for a given file tree
	 * node
	 * 
	 * @param node
	 *            File tree node to start the recursive search
	 * @param recursiveSelectedFiles
	 *            List of recursive selected files found
	 * @param flatSelectedFiles
	 *            List of flat selected files found
	 */
	private void getSelectedFiles(FileTriStateTreeNode node,
			List<File> recursiveSelectedFiles, List<File> flatSelectedFiles) {

		switch (node.getState()) {
		case SELECTED:
			/* Recursive selected folder/file found */
			File file = node.getFile();
			if (file != null) {
				recursiveSelectedFiles.add(file);
				break;
			} else if (node.isRoot()) {
				/* Continue mixed case */
			} else {
				/* File tree element does not contain an file => .. node */
				flatSelectedFiles.add(((FileTriStateTreeNode) node.getParent())
						.getFile());
				break;
			}
		case MIXED:
			/* Recursively search for children being recursive selected */
			for (int i = 0, childCount = node.getChildCount(); i < childCount; i++) {
				this.getSelectedFiles(
						(FileTriStateTreeNode) node.getChildAt(i),
						recursiveSelectedFiles, flatSelectedFiles);
			}
			break;
		case UNSELECTED:
		default:
			break;
		}
	}

	/**
	 * Refresh file tree (file system changed)
	 */
	public void refresh() {

		/* Save expanded file tree paths */
		Enumeration<TreePath> expandedPaths = this.fileTree
				.getExpandedDescendants(new TreePath(this.rootNode));

		/* Refresh nodes */
		this.refreshChildNodes((FileTriStateTreeNode) this.rootNode);
		((DefaultTreeModel) this.fileTree.getModel())
		.nodeStructureChanged(this.rootNode);

		/* Reexpand available file tree paths */
		if (expandedPaths != null) {
			while (expandedPaths.hasMoreElements()) {
				this.fileTree.expandPath(expandedPaths.nextElement());
			}
		}
	}

	/**
	 * Refresh file tree nodes children
	 * 
	 * @param parent
	 *            Parent file tree node
	 */
	private void refreshChildNodes(FileTriStateTreeNode parent) {

		/*
		 * Continue only if parent node has already children (files/folders,
		 * more than 1 the default ..-file, except the root node)
		 */
		if ((parent.getChildCount() <= 1) && (!parent.isRoot())) {
			return;
		}

		/* List of potentially new children */
		List<File> newChildFiles = new Vector<File>();
		if (parent.isRoot()) {
			newChildFiles.addAll((List<File>) Arrays.asList(File.listRoots()));
		} else {
			newChildFiles.addAll((List<File>) Arrays.asList(FileSystemView
					.getFileSystemView().getFiles(parent.getFile(),
							!this.showInvisibleFiles)));
		}
		Collections.sort(newChildFiles);

		/* List of file indexes in the list */
		List<Integer> newChildFileIndexes = new Vector<Integer>(
				newChildFiles.size());
		for (int i = 0; i < newChildFiles.size(); i++) {
			newChildFileIndexes.add(i);
		}

		/* Delete non existing children */
		for (int i = (parent.getChildCount() - 1); i >= 0; i--) {
			FileTriStateTreeNode child = (FileTriStateTreeNode) parent
					.getChildAt(i);
			File childFile = (File) child.getFile();

			/* Remove child from list of new childs */
			if (childFile != null) {
				int index = newChildFiles.indexOf(child.getFile());
				if (index >= 0) {
					newChildFiles.remove(index);
					newChildFileIndexes.remove(index);
				}
			}

			/* Refresh child */
			if (childFile == null) {
				/* .. node => do nothing */
			} else if ((!childFile.exists()) && (parent.isRoot() == false)) {
				/*
				 * File does not exist any more, delete all children from this
				 * child and itself
				 */
				child.removeFromParent();
			} else if ((parent.isRoot() == true)
					&& (!FileSystemView.getFileSystemView().isTraversable(
							childFile))) {
				/*
				 * System root does not exist any more, delete all children from
				 * this child and itself
				 */
				child.removeFromParent();
			} else if ((parent.isRoot() == false) && childFile.isHidden()
					&& !this.showInvisibleFiles) {
				/* Dont show invisible files */
				child.removeFromParent();
			} else {
				/* Refresh child sub file tree recursively */
				this.refreshChildNodes(child);
			}
		}

		/* Add new children */
		File newFile = null;
		for (int i = 0; i < newChildFiles.size(); i++) {
			newFile = newChildFiles.get(i);

			/*
			 * Adding a child to the file tree (see
			 * FileTreeListener.treeExpanded)
			 */
			FileTriStateTreeNode childNode = new FileTriStateTreeNode(newFile);
			childNode.addTriStateListener(this.fileTreeListener);
			if (newFile.isDirectory() || parent.isRoot()) { // Specific handling
				// of system roots
				// (isDirectory()
				// returns false)
				FileTriStateTreeNode dummyNode = new FileTriStateTreeNode("..");
				dummyNode.setEnabled(false);
				childNode.add((DefaultMutableTreeNode) dummyNode); // Necessary
				// to enable
				// root
				// handle, no
				// leave
				// anymore!!!
			} else {
				childNode.setAllowsChildren(false);
			}

			if (parent.isSelected()) {
				childNode.setState(TriState.SELECTED);
			}
			parent.insert((DefaultMutableTreeNode) childNode,
					newChildFileIndexes.get(i) + 1); // Increased insert index
			// due to .. node
			/* End of child add */
		}
	}

	/**
	 * Check if invisible files are shown/hidden
	 * 
	 * @return True Invisible files are shown
	 * @return False Invisible files are hidden
	 */
	public boolean isShowInvisibleFiles() {
		return showInvisibleFiles;
	}

	public void setShowInvisibleFiles(boolean showInvisibleFiles) {
		if ((this.fileTreeListener != null)
				&& (this.showInvisibleFiles != showInvisibleFiles)) {
			this.fileTreeListener.showInvisibleFiles = showInvisibleFiles;
			this.showInvisibleFiles = showInvisibleFiles;
			this.refresh();
		}
	}
}

/**
 * TriStateTreeNode wrapper
 * 
 * Wrapper is needed since TriStateTreeNode overwrites user object
 * 
 * @author Tobias Schulz
 */
@SuppressWarnings("serial")
class FileTriStateTreeNode extends TriStateTreeNode {

	/**
	 * File
	 */
	private File file;

	/**
	 * Constructor
	 * 
	 * @param string
	 *            File tree node name
	 */
	public FileTriStateTreeNode(String string) {
		super(string);
	}

	/**
	 * Constructor
	 * 
	 * @param file
	 *            File tree node file
	 */
	public FileTriStateTreeNode(File file) {
		super("");

		/*
		 * Files with "no name" (having a path separator at the end of the name
		 * e.g. roots) are displayed with their full path names
		 */
		String fileName = FileSystemView.getFileSystemView()
				.getSystemDisplayName(file);
		if (fileName.isEmpty()) {
			try {
				fileName = file.getCanonicalPath();
			} catch (IOException e) {
				System.err.println(e.getMessage());
			}
		}
		this.setUserObject(fileName);
		this.file = file;
	}

	/**
	 * Get file tree node file
	 * 
	 * @return Node file
	 */
	public File getFile() {
		return file;
	}
}

/**
 * File tree controller
 * 
 * Adding files to file tree view if folder is opened the first time.
 * Controlling tristate check boxes.
 * 
 * @author Tobias Schulz
 */
class FileTreeListener implements TreeExpansionListener, TriStateListener {

	protected boolean showInvisibleFiles = false;

	public FileTreeListener() {

	}

	@Override
	public void treeCollapsed(TreeExpansionEvent event) {

	}

	@Override
	public void treeExpanded(TreeExpansionEvent event) {

		FileTriStateTreeNode node = (FileTriStateTreeNode) event.getPath()
				.getLastPathComponent();
		DefaultTreeModel fileTreeModel = (DefaultTreeModel) ((JTree) event
				.getSource()).getModel();

		/*
		 * If node has already children (files/folders, more than 1 the default
		 * ..-file) or is a file itself, no files have to be added to the file
		 * tree view
		 */
		if ((node.getChildCount() > 1) || (!node.getAllowsChildren())) {
			return;
		}

		/* Determine files and folders to be added to the file tree view */
		File[] children = FileSystemView.getFileSystemView().getFiles(
				(File) node.getFile(), !this.showInvisibleFiles);
		if (children != null) {
			Arrays.sort(children);
			for (File file : children) {
				/*
				 * Adding a child to the file tree (see
				 * FileTreePanel.refreshChildNodes)
				 */
				FileTriStateTreeNode childNode = new FileTriStateTreeNode(file);
				childNode.addTriStateListener(this);
				if (file.isDirectory()) {
					FileTriStateTreeNode dummyNode = new FileTriStateTreeNode(
							"..");
					dummyNode.setEnabled(false);
					childNode.add((DefaultMutableTreeNode) dummyNode); // Necessary
					// to
					// enable
					// root
					// handle,
					// no
					// leave
					// anymore!!!
				} else {
					childNode.setAllowsChildren(false);
				}

				if (node.isSelected()) {
					childNode.setState(TriState.SELECTED);
				}
				node.add((DefaultMutableTreeNode) childNode);
				/* End of child add */
			}
			fileTreeModel.nodeStructureChanged(node);
		}
	}

	@Override
	public void stateChanged(TriStateEvent event) {
		// Object node = event.getSource();
		// if ((node != null) && (node instanceof FileTriStateTreeNode)) {
		//
		// }
	}
}
