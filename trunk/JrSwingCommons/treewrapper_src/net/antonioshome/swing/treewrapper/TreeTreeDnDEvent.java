/*
 * $Id: TreeTreeDnDEvent.java,v 1.1.1.1 2006/04/25 22:08:47 aviva Exp $
 * Read the "license.txt" file for licensing information.
 * (C) Antonio Vieiro. All rights reserved.
 */

package net.antonioshome.swing.treewrapper;

import java.util.EventObject;
import javax.swing.JTree;
import javax.swing.tree.TreeNode;

/**
 * TreeTreeDnDEvent is an event fired when a node from a JTree is dropped into another node (of the same or of other JTree).
 * 
 * @author Antonio Vieiro (antonio@antonioshome.net), $Author: aviva $
 * @version $Revision: 1.1.1.1 $
 */
public class TreeTreeDnDEvent extends EventObject {
	private JTree sourceTree;
	private JTree targetTree;
	private TreeNode sourceNode;
	private TreeNode targetNode;

	/**
	 * Creates a new instance of TreeTreeDnDEvent.
	 * 
	 * @param aSourceTree
	 *            the JTree containing the dragged node.
	 * @param aSourceNode
	 *            the TreeNode being dragged into aTargetNode.
	 * @param aTargetTree
	 *            the JTree containing the node on which the drop operation is about to happen.
	 * @param aTargetNode
	 *            the TreeNode onto which aSourceNode is about to be dropped.
	 */
	public TreeTreeDnDEvent(JTree aSourceTree, TreeNode aSourceNode, JTree aTargetTree, TreeNode aTargetNode) {
		super(aSourceTree);
		setSourceTree(aSourceTree);
		setSourceNode(aSourceNode);
		setTargetTree(aTargetTree);
		setTargetNode(aTargetNode);
	}

	public JTree getSourceTree() {
		return sourceTree;
	}

	public void setSourceTree(JTree sourceTree) {
		this.sourceTree = sourceTree;
	}

	public JTree getTargetTree() {
		return targetTree;
	}

	public void setTargetTree(JTree targetTree) {
		this.targetTree = targetTree;
	}

	public TreeNode getSourceNode() {
		return sourceNode;
	}

	public void setSourceNode(TreeNode sourceNode) {
		this.sourceNode = sourceNode;
	}

	public TreeNode getTargetNode() {
		return targetNode;
	}

	public void setTargetNode(TreeNode targetNode) {
		this.targetNode = targetNode;
	}

}
