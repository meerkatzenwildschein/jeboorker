package org.rr.jeborker.gui.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;

import org.rr.commons.log.LoggerFactory;
import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.mufs.ResourceNameFilter;
import org.rr.commons.swing.components.tree.NamedNode;
import org.rr.jeborker.gui.action.ActionUtils;

public class FileSystemNode implements MutableTreeNode, NamedNode, Comparable<FileSystemNode> {

	private IResourceHandler pathResource;
	
	private List<IResourceHandler> subFolders;

	private List<FileSystemNode> childNodes;
	
	private TreeNode parent;
	
	private boolean showFiles = true;
	
	FileSystemNode(IResourceHandler pathResource, TreeNode parent) {
		this.pathResource = pathResource;
		this.parent = parent;
	}
	
	FileSystemNode(IResourceHandler pathResource, TreeNode parent, boolean showFiles) {
		this.pathResource = pathResource;
		this.parent = parent;
		this.showFiles = showFiles;
	}
	
	@Override
	public TreeNode getChildAt(int childIndex) {
		final List<FileSystemNode> childResources = createChildren();
		return childResources.get(childIndex);
	}

	@Override
	public int getChildCount() {
		final List<IResourceHandler> childResources = getChildResources();
		return childResources.size();
	}

	@Override
	public TreeNode getParent() {
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
		return this.pathResource.isFileResource();
	}

	private List<FileSystemNode> createChildren() {
		if(childNodes == null) {
			final List<IResourceHandler> childResources = getChildResources();
			childNodes = new ArrayList<FileSystemNode>(childResources.size());
			for(int i = 0; i < childResources.size(); i++) {
				IResourceHandler resource = childResources.get(i);
				childNodes.add(new FileSystemNode(resource, this, showFiles));
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
				IResourceHandler[] listDirectoryResources = pathResource.listDirectoryResources(false);
				subFolders = new ArrayList<IResourceHandler>(Arrays.asList(listDirectoryResources));
				
				if(showFiles) {
					IResourceHandler[] listFileResources = pathResource.listResources(new ResourceNameFilter() {
						
						@Override
						public boolean accept(IResourceHandler resource) {
							if(ActionUtils.isSupportedEbookFormat(resource) && !resource.isHidden()) {
								return true;
							}
							return false;
						}
					});
					subFolders.addAll(Arrays.asList(listFileResources));
				}
			} catch (IOException e) {
				LoggerFactory.getLogger(this).log(Level.WARNING, "Failed to list " + pathResource, e);
				subFolders = new ArrayList<IResourceHandler>(0);
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
		return o.pathResource.toString().compareTo(pathResource.toString());
	}	
	
	public boolean equals(Object o) {
		 if(o instanceof FileSystemNode) {
			 return this.compareTo((FileSystemNode)o) == 0;
		 }
		 return false;
	}

	@Override
	public int hashCode() {
		return pathResource.toString().hashCode();
	}
	
}
