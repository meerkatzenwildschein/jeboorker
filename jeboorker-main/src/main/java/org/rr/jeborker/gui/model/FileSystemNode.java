package org.rr.jeborker.gui.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

import javax.swing.SwingUtilities;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;

import org.rr.commons.log.LoggerFactory;
import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.mufs.ResourceNameFilter;
import org.rr.commons.swing.components.tree.JRTree;
import org.rr.commons.swing.components.tree.NamedNode;
import org.rr.commons.utils.ListUtils;
import org.rr.jeborker.gui.MainViewTreeComponentHandler;
import org.rr.jeborker.gui.action.ActionUtils;

public class FileSystemNode implements MutableTreeNode, NamedNode, Comparable<FileSystemNode> {

	private IResourceHandler pathResource;
	
	private JRTree tree;
	
	private int hashCode;
	
	private List<IResourceHandler> subFolders;

	private List<FileSystemNode> childNodes;
	
	private TreeNode parent;
	
	private boolean showFiles = true;
	
	private Boolean isLeaf;
	
	FileSystemNode(IResourceHandler pathResource, TreeNode parent, JRTree tree) {
		this.pathResource = pathResource;
		this.parent = parent;
		this.tree = tree;
		this.hashCode = pathResource.toString().hashCode();
	}
	
	FileSystemNode(IResourceHandler pathResource, TreeNode parent, JRTree tree, boolean showFiles) {
		this.pathResource = pathResource;
		this.parent = parent;
		this.tree = tree;
		this.showFiles = showFiles;
		this.hashCode = pathResource.toString().hashCode();
	}
	
	@Override
	public TreeNode getChildAt(int childIndex) {
		final List<FileSystemNode> childResources = createChildren();
		return ListUtils.get(childResources, childIndex);
	}

	@Override
	public int getChildCount() {
		if(!this.pathResource.exists()) {
			SwingUtilities.invokeLater(new Runnable() {
				
				@Override
				public void run() {
					MainViewTreeComponentHandler.refreshFileSystemTreeEntry(pathResource, tree, null);
				}
			});
		}
		final List<IResourceHandler> childResources = getChildResources();
		return childResources.size();
	}

	@Override
	public TreeNode getParent() {
		if(!this.pathResource.exists()) {
			return null;
		}
		return this.parent;
	}

	@Override
	public int getIndex(TreeNode node) {
		final List<IResourceHandler> childResources = getChildResources();
		for(int i = 0; i < childResources.size(); i++) {
			IResourceHandler resource = childResources.get(i);
			if(((FileSystemNode)node).pathResource.equals(resource)) {		
				return i;
			}
		}
		return -1;
	}

	@Override
	public boolean getAllowsChildren() {
		return true;
	}

	@Override
	public boolean isLeaf() {
		if(isLeaf == null) {
			isLeaf = Boolean.valueOf(this.pathResource.isFileResource());
		}
		return isLeaf;
	}

	private List<FileSystemNode> createChildren() {
		if(childNodes == null) {
			final List<IResourceHandler> childResources = getChildResources();
			childNodes = new ArrayList<>(childResources.size());
			for(int i = 0; i < childResources.size(); i++) {
				IResourceHandler resource = childResources.get(i);
				childNodes.add(new FileSystemNode(resource, this, tree, showFiles));
			}
		}
		return childNodes;
	}
	
	public void reset() {
		subFolders = null;
		childNodes = null;
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public Enumeration children() {
		final List<FileSystemNode> childResources = createChildren();
		final Iterator<FileSystemNode> childResourcesIterator = childResources.iterator();
		return new Enumeration() {

			@Override
			public boolean hasMoreElements() {
				return childResourcesIterator.hasNext();
			}

			@Override
			public Object nextElement() {
				return childResourcesIterator.next();
			}
		};
	}
	
	private List<IResourceHandler> getChildResources() {
		if(subFolders == null) {
			try {
				final ArrayList<IResourceHandler> subFolder = new ArrayList<>();
				final ArrayList<IResourceHandler> subFiles = new ArrayList<>();
				pathResource.listResources(new ResourceNameFilter() {
					
					@Override
					public boolean accept(IResourceHandler resource) {
						if(!resource.isHidden()) {
							if(resource.isFileResource()) {
								if(showFiles && ActionUtils.isSupportedEbookFormat(resource, false)) {
									subFiles.add(resource);
								}
							} else {
								subFolder.add(resource);
							}
						}
						return false;
					}
				});
				subFolders = new ArrayList<>();
				subFolders.addAll(subFolder);
				subFolders.addAll(subFiles);
			} catch (IOException e) {
				LoggerFactory.getLogger(this).log(Level.WARNING, "Failed to list " + pathResource, e);
				subFolders = new ArrayList<>(0);
			}		
		} 
		return subFolders;
	}

	@Override
	public void insert(MutableTreeNode child, int index) {
		final List<IResourceHandler> childResources = getChildResources();
		childResources.add(index, ((FileSystemNode)child).pathResource);
	}

	@Override
	public void remove(int index) {
		final List<IResourceHandler> childResources = getChildResources();
		childResources.remove(index);
	}

	@Override
	public void remove(MutableTreeNode node) {
		final List<IResourceHandler> childResources = getChildResources();
		int index = this.getIndex(node);
		childResources.remove(index);
	}

	@Override
	public void setUserObject(Object userObject) {
	}

	@Override
	public void removeFromParent() {
		((MutableTreeNode)this.parent).remove(this);
	}

	@Override
	public void setParent(MutableTreeNode newParent) {
		this.parent = newParent;
	}
	
	public String toString() {
		return this.pathResource.getName();
	}
	
	public IResourceHandler getResource() {
		return this.pathResource;
	}

	@Override
	public String getName() {
		return pathResource.toString();
	}
	
	public void renameTo(IResourceHandler newPathName) throws IOException {
		this.pathResource.moveTo(newPathName, false);
		this.pathResource = newPathName;
	}

	@Override
	public int compareTo(FileSystemNode o) {
		return o.pathResource.compareTo(pathResource);
	}	
	
	public boolean equals(Object o) {
		if(this == o) {
			return true;
		} else if(o instanceof FileSystemNode) {
			return this.compareTo((FileSystemNode)o) == 0;
		}
		return false;
	}

	@Override
	public int hashCode() {
		return hashCode;
	}
	
}
