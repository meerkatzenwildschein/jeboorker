package org.rr.jeborker.gui.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;

import org.rr.commons.log.LoggerFactory;
import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.mufs.ResourceHandlerFactory;
import org.rr.jeborker.JeboorkerPreferences;

public class FileSystemTreeModel extends DefaultTreeModel {

	private DefaultMutableTreeNode root;
	
	public FileSystemTreeModel() {
		super(new DefaultMutableTreeNode("root"));
		this.root = (DefaultMutableTreeNode) getRoot();
		this.init();
	}

	public void init() {
		List<String> basePath = JeboorkerPreferences.getBasePath();
		for(String path : basePath) {
			IResourceHandler resourceHandler = ResourceHandlerFactory.getResourceHandler(path);
			FileSystemPathNode basePathNode = new FileSystemPathNode(resourceHandler, null);
			root.add(basePathNode);
		}
	}
	
	public void dispose() {
		TreeModelListener[] treeModelListeners = getTreeModelListeners();
		for(TreeModelListener treeModelListener : treeModelListeners) {
			removeTreeModelListener(treeModelListener);
		}
	}
	
	public static class FileSystemPathNode implements MutableTreeNode {

		private IResourceHandler pathResource;
		
		private List<IResourceHandler> childs;
		
		private List<FileSystemPathNode> childNodes;
		
		private TreeNode parent;
		
		FileSystemPathNode(IResourceHandler pathResource, TreeNode parent) {
			this.pathResource = pathResource;
			this.parent = parent;
		}
		
		@Override
		public TreeNode getChildAt(int childIndex) {
			final List<FileSystemPathNode> childResources = createChildren();
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
				if(((FileSystemPathNode)node).pathResource.equals(resource)) {
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
			return false;
		}

		private List<FileSystemPathNode> createChildren() {
			if(childNodes == null) {
				final List<IResourceHandler> childResources = getChildResources();
				childNodes = new ArrayList<FileSystemTreeModel.FileSystemPathNode>(childResources.size());
				for(int i = 0; i < childResources.size(); i++) {
					IResourceHandler resource = childResources.get(i);
					childNodes.add(new FileSystemPathNode(resource, this));
				}
			}
			return childNodes;
		}
		
		@SuppressWarnings("rawtypes")
		@Override
		public Enumeration children() {
			final List<FileSystemPathNode> childResources = createChildren();
			final Iterator<FileSystemPathNode> childResourcesIterator = childResources.iterator();
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
			if(childs == null) {
				try {
					IResourceHandler[] listDirectoryResources = pathResource.listDirectoryResources();
					childs = Arrays.asList(listDirectoryResources);
				} catch (IOException e) {
					LoggerFactory.getLogger(this).log(Level.WARNING, "Failed to list " + pathResource, e);
					childs = new ArrayList<IResourceHandler>(0);
				}		
			} 
			return childs;
		}

		@Override
		public void insert(MutableTreeNode child, int index) {
			final List<IResourceHandler> childResources = getChildResources();
			childResources.add(index, ((FileSystemPathNode)child).pathResource);
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
		
		public IResourceHandler getPathResource() {
			return this.pathResource;
		}
	}

}
