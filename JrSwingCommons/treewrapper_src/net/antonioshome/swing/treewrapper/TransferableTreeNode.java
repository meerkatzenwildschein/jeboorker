/*
 * $Id: TransferableTreeNode.java,v 1.3 2006/05/07 11:56:33 aviva Exp $
 * Read the "license.txt" file for licensing information.
 * (C) Antonio Vieiro. All rights reserved.
 */
package net.antonioshome.swing.treewrapper;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import javax.swing.JTree;
import javax.swing.tree.MutableTreeNode;

/**
 * TransferableTreeNode is a Transferable object used to transfer TreeNodes or Strings in drag and drop operations.
 * 
 * @author Antonio Vieiro (antonio@antonioshome.net), $Author: aviva $
 * @version $Revision: 1.3 $
 */
class TransferableTreeNode implements Transferable {
	/**
	 * The local JVM DataFlavor.
	 */
	private static DataFlavor javaJVMLocalObjectFlavor;
	/**
	 * The supported data flavors.
	 */
	private static DataFlavor[] supportedDataFlavors;

	/**
	 * Returns the Java JVM LocalObject Flavor.
	 */
	public static DataFlavor getJavaJVMLocalObjectFlavor() {
		if (javaJVMLocalObjectFlavor == null) {
			try {
				javaJVMLocalObjectFlavor = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType);
			} catch (ClassNotFoundException cnfe) {
				System.err.println("Cannot create JVM Local Object Flavor " + cnfe.getMessage());
			}
		}
		return javaJVMLocalObjectFlavor;
	}

	/**
	 * Returns the supported data flavors.
	 */
	private static DataFlavor[] getSupportedDataFlavors() {
		if (supportedDataFlavors == null) {
			DataFlavor localJVMFlavor = getJavaJVMLocalObjectFlavor();
			supportedDataFlavors = localJVMFlavor == null ? new DataFlavor[] { DataFlavor.stringFlavor } : new DataFlavor[] { localJVMFlavor,
					DataFlavor.stringFlavor };
		}
		return supportedDataFlavors;
	}

	/**
	 * Creates a new instance of TransferableTreeNode.
	 * 
	 * @param aTree
	 *            the JTree that contains de dragged node.
	 * @param aNode
	 *            the MutableTreeNode in JTree that is to be dragged.
	 * @param wasExpanded
	 *            true if the source node was expanded, false otherwise.
	 */
	public TransferableTreeNode(JTree aTree, MutableTreeNode aNode, boolean wasExpanded) {
		setSourceTree(aTree);
		setSourceNode(aNode);
		setNodeWasExpanded(wasExpanded);
	}

	public boolean isDataFlavorSupported(DataFlavor flavor) {
		DataFlavor[] flavors = getSupportedDataFlavors();
		for (int i = 0; i < flavors.length; i++) {
			if (flavor.equals(flavors[i]))
				return true;
		}
		return false;
	}

	public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
		if (flavor.equals(javaJVMLocalObjectFlavor)) {
			return this;
		} else if (flavor.equals(DataFlavor.stringFlavor)) {
			return getSourceNode().toString();
		} else
			throw new UnsupportedFlavorException(flavor);
	}

	public DataFlavor[] getTransferDataFlavors() {
		return getSupportedDataFlavors();
	}

	/**
	 * Holds value of property sourceTree.
	 */
	private JTree sourceTree;

	/**
	 * Getter for property sourceTree.
	 * 
	 * @return Value of property sourceTree.
	 */
	public JTree getSourceTree() {
		return this.sourceTree;
	}

	/**
	 * Setter for property sourceTree.
	 * 
	 * @param sourceTree
	 *            New value of property sourceTree.
	 */
	public void setSourceTree(JTree sourceTree) {
		this.sourceTree = sourceTree;
	}

	/**
	 * Holds value of property sourceNode.
	 */
	private MutableTreeNode sourceNode;

	/**
	 * Getter for property sourceNode.
	 * 
	 * @return Value of property sourceNode.
	 */
	public MutableTreeNode getSourceNode() {
		return this.sourceNode;
	}

	/**
	 * Setter for property sourceNode.
	 * 
	 * @param sourceNode
	 *            New value of property sourceNode.
	 */
	public void setSourceNode(MutableTreeNode sourceNode) {
		this.sourceNode = sourceNode;
	}

	/**
	 * Holds value of property nodeWasExpanded.
	 */
	private boolean nodeWasExpanded;

	/**
	 * Getter for property nodeWasExpanded.
	 * 
	 * @return Value of property nodeWasExpanded.
	 */
	public boolean isNodeWasExpanded() {
		return this.nodeWasExpanded;
	}

	/**
	 * Setter for property nodeWasExpanded.
	 * 
	 * @param nodeWasExpanded
	 *            New value of property nodeWasExpanded.
	 */
	public void setNodeWasExpanded(boolean nodeWasExpanded) {
		this.nodeWasExpanded = nodeWasExpanded;
	}

}
