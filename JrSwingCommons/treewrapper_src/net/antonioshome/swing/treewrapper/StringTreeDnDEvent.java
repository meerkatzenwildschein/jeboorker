/*
 * $Id: StringTreeDnDEvent.java,v 1.1.1.1 2006/04/25 22:08:47 aviva Exp $
 * Read the "license.txt" file for licensing information.
 * (C) Antonio Vieiro. All rights reserved.
 */

package net.antonioshome.swing.treewrapper;

import java.util.EventObject;
import javax.swing.JTree;
import javax.swing.tree.TreeNode;

/**
 * StringTreeDnDEvent is an event fired whenever a String is about to be dropped into a node in a JTree.
 * 
 * @author Antonio Vieiro (antonio@antonioshome.net), $Author: aviva $
 * @version $Revision: 1.1.1.1 $
 */
public class StringTreeDnDEvent extends EventObject {
	private JTree targetTree;
	private TreeNode targetNode;
	private String sourceString;

	/**
	 * Creates a new instance of StringTreeDnDEvent
	 * 
	 * @param aSourceString
	 *            the String being dragged.
	 * @param aTargetTree
	 *            the JTree containing the node.
	 * @param aTargetNode
	 *            the node onto which the String is about to be dropped into.
	 */
	public StringTreeDnDEvent(String aSourceString, JTree aTargetTree, TreeNode aTargetNode) {
		super(aSourceString);
		setSourceString(aSourceString);
		setTargetTree(aTargetTree);
		setTargetNode(aTargetNode);
	}

	public JTree getTargetTree() {
		return targetTree;
	}

	public void setTargetTree(JTree targetTree) {
		this.targetTree = targetTree;
	}

	public TreeNode getTargetNode() {
		return targetNode;
	}

	public void setTargetNode(TreeNode targetNode) {
		this.targetNode = targetNode;
	}

	public String getSourceString() {
		return sourceString;
	}

	public void setSourceString(String sourceString) {
		this.sourceString = sourceString;
	}

}
