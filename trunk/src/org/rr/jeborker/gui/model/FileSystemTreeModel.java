package org.rr.jeborker.gui.model;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.StringTokenizer;

import javax.swing.Icon;
import javax.swing.JFileChooser;
import javax.swing.JTree;
import javax.swing.event.TreeModelListener;
import javax.swing.filechooser.FileSystemView;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.rr.common.swing.tree.NamedNode;
import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.mufs.ResourceHandlerFactory;
import org.rr.commons.utils.ReflectionUtils;
import org.rr.commons.utils.StringUtils;
import org.rr.jeborker.gui.action.ActionUtils;

public class FileSystemTreeModel extends DefaultTreeModel {

	private static final long serialVersionUID = -456216843620742653L;

	private static final FileSystemView fileSystemViewInstance = FileSystemView.getFileSystemView();

	private JTree tree;
	
	public FileSystemTreeModel(JTree tree) {
		/** create an instance with a TreeNode which handles the root. */
		super(new FileSystemRootNode());
		this.tree = tree;
	}

	public List<IFolderNode> getPath(File path) {
		StringTokenizer fileTokenezier = new StringTokenizer(path.getPath(), String.valueOf(File.separatorChar + ":"));

		// searching the node one after the other is fundamental because the children will be also read for the path
		// and the search only works with children which knows it's sub childs.
		IFolderNode lastNode = (IFolderNode) this.getRoot();
		while (fileTokenezier.hasMoreTokens()) {
			String name = fileTokenezier.nextToken();
			lastNode = this.searchNode(name, lastNode);
			if (lastNode == null) {
				return null; // nothing found.
			}

			if (fileTokenezier.hasMoreTokens()) {
				lastNode.getChildCount(); // invokes the read of all children for the node.
			}
		}

		if (lastNode != null) {
			ArrayList<IFolderNode> result = new ArrayList<IFolderNode>();
			IFolderNode parent = lastNode;
			while (parent != null) {
				result.add(parent);
				parent = (IFolderNode) parent.getParent();
			}

			Collections.reverse(result);
			return result;
		}

		return null;
	}

	/**
	 * Searches the visible node area for a node with the given name.
	 * 
	 * @param name
	 * @param searchNode
	 * @return
	 */
	private IFolderNode searchNode(String name, IFolderNode searchNode) {
		if (searchNode.isChildrenLoaded()) {

			for (int i = 0; i < searchNode.getChildCount(); i++) {
				IFolderNode childNode = (IFolderNode) searchNode.getChildAt(i);
				if (childNode.matchName(name)) {
					return childNode;
				} else if (childNode.isChildrenLoaded()) {
					IFolderNode result = searchNode(name, childNode);
					if (result != null) {
						return result;
					}
				}
			}

		}
		return null;
	}

	protected void fireTreeStructureChanged(Object source, Object[] path, int[] childIndices, Object[] children) {
		super.fireTreeStructureChanged(source, path, childIndices, children);
	}

	/**
	 * This sets the user object of the TreeNode identified by path and posts a node changed. If you use custom user objects in the TreeModel you're going to
	 * need to subclass this and set the user object of the changed node to something meaningful.
	 */
	public void valueForPathChanged(TreePath path, Object newValue) {
		final IFolderNode aNode = (IFolderNode) path.getLastPathComponent();
		String oldPathName = aNode.getFile().getPath();
		String newPathName = oldPathName.substring(0, oldPathName.length() - aNode.getFile().getName().length()) + newValue;

		final File newPath = new File(newPathName);
		aNode.renameTo(newPath);
		nodeChanged(aNode);
	}
	
	@Override
	public void reload(TreeNode node) {
		if(node instanceof FolderNode) {
			((FolderNode)node).childFolderNodes = null;
			((FolderNode)node).subFiles = null;
			((FolderNode)node).subFolders = null;
		}
		super.reload(node);
	}	

	/**
	 * Reloads these node which represents the given {@link IResourceHandler} instance.
	 * If the node is not opened it will not be refreshed.
	 */
	public void reload(IResourceHandler resourceToRefresh) {
		int rowCount = tree.getRowCount();
		for(int i = 0; i < rowCount; i++) {
			TreePath pathForRow = tree.getPathForRow(i);
			if(pathForRow.getLastPathComponent() instanceof IFolderNode) {
				File file = ((IFolderNode)pathForRow.getLastPathComponent()).getFile();
				IResourceHandler resourceHandler = ResourceHandlerFactory.getResourceHandler(file);
				if(resourceHandler.equals(resourceToRefresh)) {
					reload((TreeNode) pathForRow.getLastPathComponent());
					break;
				}
			}
		}
	}

	public interface IFolderNode extends TreeNode, Comparable<IFolderNode>, NamedNode {
		/**
		 * Icon for a file, directory, or folder as it would be displayed in a system file browser. Example from Windows: the "M:\" directory displays a CD-ROM
		 * icon.
		 * 
		 * The default implementation gets information from the ShellFolder class.
		 * 
		 * @return an icon as it would be displayed by a native file chooser
		 * @see JFileChooser#getIcon
		 */
		public Icon getSystemIcon();

		/**
		 * Determines if the sub folders are already loaded. This should help for searching because it's no good idea to search all nodes. This will be like a
		 * complete filesystem scan.
		 * 
		 * @return <code>true</code> if the node has already loaded it's sub folders.
		 */
		public boolean isChildrenLoaded();

		/**
		 * Test if the given name is matching to the node name. The match should also include some impreciseness, so c: matches also to the node "c:\" or "c:\
		 * Datenträger"
		 */
		public boolean matchName(String name);

		/**
		 * Returns the file for this node or <code>null</code> if this is a node without a file path.
		 * 
		 * @return The file path for the node instance.
		 */
		public File getFile();

		/**
		 * Renames the node instance to the given file. The file system rename should also be performed.
		 * 
		 * @param newPathName
		 *            The name of the new path
		 */
		public void renameTo(File newPathName);

		/**
		 * tells the node that it's children should be reread.
		 * 
		 * @param node
		 *            The node which children should be refreshed.
		 */
		void refreshChildren();
	}

	private static class FileSystemRootNode extends FolderNode implements IFolderNode {
		FileSystemRootNode() {
			super(null, null);

			File[] roots = FileSystemView.getFileSystemView().getRoots(); // File.listRoots();
			if (roots.length == 1) {
				folder = roots[0];
			}
		}

		public boolean isChildrenLoaded() {
			this.getChildCount(); // init the children

			// and also init the children for the first sub entry
			((IFolderNode) this.getChildAt(0)).getChildCount();
			return true;
		}
		
		@Override
		public String getName() {
			return null;
		}		
	}

	/**
	 * Should be created for each folder which gets visible.
	 */
	private static class FolderNode implements IFolderNode {

		protected File folder;

		protected TreeNode parent;

		private List<File> subFolders;

		private List<File> subFiles;

		private List<IFolderNode> childFolderNodes;

		/**
		 * @param folder
		 *            The folder which is represented by the <code>FolderNode</code>.
		 */
		FolderNode(File folder, TreeNode parent) {
			this.folder = folder;
			this.parent = parent;
		}

		/**
		 * Invoking this method will cause that the subfolders are read.
		 * 
		 * @return An array with null values. The nodes are created on demand.
		 */
		private List<IFolderNode> getChildren() {
			if (this.childFolderNodes == null) {
				this.childFolderNodes = new ArrayList<FileSystemTreeModel.IFolderNode>();
				File[] subFiles = fileSystemViewInstance.getFiles(this.folder, true);
				ArrayList<File> processedSubFolders = new ArrayList<File>();
				for (int i = 0; i < subFiles.length; i++) {
					if (subFiles[i].isDirectory()) {
						this.childFolderNodes.add(new FolderNode(subFiles[i], this));
						processedSubFolders.add(subFiles[i]);
					}
				}
				this.subFolders = processedSubFolders;
				Collections.sort(this.subFolders);

				subFiles = fileSystemViewInstance.getFiles(this.folder, true);
				ArrayList<File> processedSubFiles = new ArrayList<File>();
				for (int i = 0; i < subFiles.length; i++) {
					if (subFiles[i].isFile() && ActionUtils.isSupportedEbookFormat(ResourceHandlerFactory.getResourceHandler(subFiles[i]))) {
						this.childFolderNodes.add(new FileNode(subFiles[i], this));
						processedSubFiles.add(subFiles[i]);
					}
				}
				this.subFiles = processedSubFiles;
				Collections.sort(this.subFiles);

				if (this.subFolders == null) {
					this.subFolders = Collections.emptyList();
				}
				if (this.subFiles == null) {
					this.subFiles = Collections.emptyList();
				}

				Collections.sort(this.childFolderNodes);
			}

			return this.childFolderNodes;
		}

		/**
		 * @return The folder which is represented by this TreeNode.
		 */
		public File getFile() {
			return this.folder;
		}

		/**
		 * Gets the name of the folder. This is the last part of the path.
		 * 
		 * @return The folder name.
		 */
		public String getFolderName() {
			String result = this.folder.getPath();

			// use system display folder names if possible.
			if (!fileSystemViewInstance.isFloppyDrive(this.folder)) {
				String symbolicName = fileSystemViewInstance.getSystemDisplayName(this.folder);
				if (!toOSCompareString(result).equals(toOSCompareString(symbolicName)) && symbolicName.length() > 0) {
					return symbolicName;
				}
			}

			if (result.indexOf('/') == -1 && result.indexOf('\\') == -1) {
				return result;
			} else {
				result = result.replace('\\', '/');
			}

			result = result.substring(result.lastIndexOf('/') + 1);
			if (result.length() > 1) {
				return result;
			} else {
				return this.folder.getPath();
			}
		}

		/**
		 * creates a new node if not already done. Nodes will be created on demand.
		 */
		public Enumeration<File> children() {
			throw new RuntimeException("not supported");
		}

		/**
		 * I not really know, but children's are allows here. Change it if it gets to disturb something.
		 */
		public boolean getAllowsChildren() {
			return true;
		}

		/**
		 * Gets the child at the desired index. The node will be created on demand.
		 */
		public TreeNode getChildAt(int childIndex) {
			try {
				List<IFolderNode> children = this.getChildren();
				return children.get(childIndex);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		/**
		 * @return the number of children available with this node instance.
		 */
		public int getChildCount() {
			return this.getChildren().size();
		}

		public int getIndex(TreeNode node) {
			List<IFolderNode> children = this.getChildren();

			for (int i = 0; i < children.size(); i++) {
				IFolderNode child = children.get(i);
				if (child != null && child == node) {
					return i;
				} else if (child != null && child instanceof FolderNode
						&& ((FolderNode) child).getFile().getPath().equals(((FolderNode) child).getFile().getPath())) {
					return i;
				}
			}
			return -1;
		}

		/**
		 * The parent TreeNode for this folder node instance.
		 */
		public TreeNode getParent() {
			return this.parent;
		}

		/**
		 * @return <code>true</code> if no sub folders are present.
		 */
		public boolean isLeaf() {
			if (fileSystemViewInstance.isDrive(this.getFile()) || fileSystemViewInstance.isRoot(this.getFile())) {
				return false;
			} else if (this.subFolders == null) {
				return false;
			}
			return this.getChildCount() == 0;
		}

		public String toString() {
			return this.getFolderName();
		}

		/**
		 * Gets an Icon matching to the file represented by this node.
		 */
		public Icon getSystemIcon() {
			if (this.folder.exists()) {
				return fileSystemViewInstance.getSystemIcon(this.folder);
			}
			return null;
		}

		public boolean isChildrenLoaded() {
			return !(this.subFolders == null);
		}

		/**
		 * Tests if the given name matches to the path segement for this node.
		 */
		public boolean matchName(String name) {
			if (fileSystemViewInstance.isDrive(this.folder)) {
				String match = StringUtils.replace(this.folder.getPath(), String.valueOf(File.separatorChar), "");
				match = StringUtils.replace(match, ":", "");
				if (toOSCompareString(match).equals(toOSCompareString(name))) {
					return true;
				}
			} else if (this.getFolderName().equalsIgnoreCase(name)) {
				return true;
			}

			return false;
		}

		/**
		 * renames the folder which is represented by this node instance to the given <code>newPathName</code>.
		 */
		public void renameTo(File newPathName) {
			this.folder.renameTo(newPathName);
			this.folder = newPathName;
		}

		public void refreshChildren() {
			this.subFolders = null;
			this.childFolderNodes = null;
		}

		@Override
		public int compareTo(IFolderNode o) {
			if (o.getFile().isFile() && this.getFile().isDirectory()) {
				return -99;
			} else if(o.getFile().isDirectory() && this.getFile().isFile()) {
				return 99;
			}
			return getFile().compareTo(o.getFile());
		}

		@Override
		public String getName() {
			return getFile().toString();
		}

	}

	private static class FileNode extends FolderNode {

		private File file;

		private FileNode(File file, TreeNode parent) {
			super(file, parent);
			this.file = file;
		}

		@Override
		public TreeNode getChildAt(int childIndex) {
			return null;
		}

		@Override
		public int getChildCount() {
			return 0;
		}

		@Override
		public int getIndex(TreeNode node) {
			return 0;
		}

		@Override
		public boolean getAllowsChildren() {
			return false;
		}

		@Override
		public boolean isLeaf() {
			return true;
		}

		@Override
		public Icon getSystemIcon() {
			if (this.file.exists()) {
				return fileSystemViewInstance.getSystemIcon(this.file);
			}
			return null;

		}

		@Override
		public boolean isChildrenLoaded() {
			return false;
		}

		@Override
		public File getFile() {
			return this.file;
		}

		@Override
		public void renameTo(File newPathName) {
			this.file.renameTo(newPathName);
			this.file = newPathName;
		}

		@Override
		public void refreshChildren() {
		}

	}

	/**
	 * Operating system dependent returns a toLowercased string or not.
	 * 
	 * @param s
	 *            The string to be changed.
	 * @return The changed string
	 */
	private static String toOSCompareString(String s) {
		if (s == null) {
			return null;
		}

		if (ReflectionUtils.getOS() == ReflectionUtils.OS_WINDOWS) {
			return s.toLowerCase();
		} else {
			return s;
		}

	}
	
	public void dispose() {
		TreeModelListener[] treeModelListeners = getTreeModelListeners();
		for(TreeModelListener treeModelListener : treeModelListeners) {
			removeTreeModelListener(treeModelListener);
		}
	}
}
