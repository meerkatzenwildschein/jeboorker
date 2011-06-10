/*
 * $Id: QTree.java,v 1.6 2008/01/28 21:28:37 edankert Exp $
 *
 * Copyright (c) 2002 - 2008, Edwin Dankert
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions are met:
 * * Redistributions of source code must retain the above copyright notice, 
 *	 this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright 
 * 	 notice, this list of conditions and the following disclaimer in the 
 *	 documentation and/or other materials provided with the distribution. 
 * * Neither the name of 'Edwin Dankert' nor the names of its contributors 
 *	 may  be used to endorse or promote products derived from this software 
 *	 without specific prior written permission. 
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
 * ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR 
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR 
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER 
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, 
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE 
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.bounce;

import java.util.Hashtable;
import java.util.Vector;

import javax.swing.text.Position;

import javax.swing.tree.TreeNode;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import javax.swing.JTree;

/**
 * Extend the JTree class, this tree allows for:
 *
 * @version	$Revision: 1.6 $, $Date: 2008/01/28 21:28:37 $
 * @author Edwin Dankert <edankert@gmail.com>
 */
public class QTree extends JTree {
	private static final long serialVersionUID = 3832618482620839223L;
	
	/**
	 * Default constructor, calls super.
	 */
	public QTree() {
		super();
	}

	/**
	 * Constructor, calls super.
	 * 
	 * @param value a hashtable of nodes.
	 */
	public QTree( Hashtable value) {
		super( value);
	}

	/**
	 * Constructor, calls super.
	 * 
	 * @param value an array of nodes.
	 */
	public QTree( Object[] value) {
		super( value);
	}

	/**
	 * Constructor, calls super.
	 * 
	 * @param model an tree model.
	 */
	public QTree( TreeModel model) {
		super( model);
	}

	/**
	 * Constructor, calls super.
	 * 
	 * @param root the root node.
	 */
	public QTree( TreeNode root) {
		super( root);
	}

	/**
	 * Constructor, calls super.
	 * 
	 * @param root the root node.
	 * @param asksAllowsChildren allows children.
	 */
	public QTree( TreeNode root, boolean asksAllowsChildren) {
		super( root, asksAllowsChildren);
	}

	/**
	 * Constructor, calls super.
	 * 
	 * @param value a vector of nodes.
	 */
	public QTree( Vector value) {
		super( value);
	}
	
	/**
	 * Returns the currently selected node, null if nothing 
	 * has been selected.
	 *
	 * @return a Tree Node.
	 */
	public TreeNode getSelectedNode() {
		TreeNode node = null;
		TreePath path = getSelectionPath();
		
		if ( path != null) {
			node = (TreeNode) path.getLastPathComponent();
		}	
	
		return node;
	}

	/**
	 * Sets the selected node.
	 * Clears the selection if the node is null.
	 *
	 * @param node the node.
	 */
	public void setSelectedNode( DefaultMutableTreeNode node) {
		if ( node != null) {
			setSelectionPath( new TreePath( node.getPath()));
		} else {
			clearSelection();
		}
	}
	
	/**
	 * Expands all the nodes in the tree.
	 */
	public void expandAll() {
		expandNode( (DefaultMutableTreeNode)getModel().getRoot());
	}

	/**
	 * Expands all the nodes in the tree to a certain level.
	 *
	 * @param level the level to expand to.
	 */
	public void expand( int level) {
		expandNode( (DefaultMutableTreeNode)getModel().getRoot(), level);
	}

	/**
	 * Collapses all the nodes in the tree.
	 */
	public void collapseAll() {
		collapseNode( (DefaultMutableTreeNode)getModel().getRoot());
	}

	/**
	 * Collapses all the nodes in the tree from this node up/down.
	 *
	 * @param node the node to be collapsed.
	 */
	public void collapseNode( DefaultMutableTreeNode node) {
		for ( int i = 0; i < node.getChildCount(); i++) {
			collapseNode( (DefaultMutableTreeNode)node.getChildAt( i));
		}

		collapsePath( new TreePath( node.getPath()));
	}

	/**
	 * Expands all the nodes in the tree from this node down.
	 *
	 * @param node the node to be expanded.
	 */
	public void expandNode( DefaultMutableTreeNode node) {
		expandPath( new TreePath( node.getPath()));

		for ( int i = 0; i < node.getChildCount(); i++) {
			expandNode( (DefaultMutableTreeNode)node.getChildAt( i));
		}
	}

	/**
	 * Expands all the nodes in the tree from this node down, 
	 * until a certain level.
	 *
	 * @param node the node to be expanded.
	 * @param level the level to expand to.
	 */
	public void expandNode( DefaultMutableTreeNode node, int level) {
		if ( level > 0) {
			expandPath( new TreePath( node.getPath()));

			for ( int i = 0; i < node.getChildCount(); i++) {
				expandNode( (DefaultMutableTreeNode)node.getChildAt( i), level - 1);
			}
		}
	}

	/**
	 * Bug fix for return and space kay presses.
	 *
	 * Overwrite get next match method, to make sure the nullpointer 
	 * exception does not happen any more.
	 *
	 * @param prefix the character that should prefix the node name.
	 * @param startingRow the level to expand to.
	 * @param bias the level to expand to.
	 * 
	 * @return the tree path to the node.
	 */
	public TreePath getNextMatch( String prefix, int startingRow, Position.Bias bias) {

        int max = getRowCount();

		if (prefix == null) {
		    throw new IllegalArgumentException();
		}

		if (startingRow < 0 || startingRow >= max) {
		    throw new IllegalArgumentException();
		}

		prefix = prefix.toUpperCase();

		// start search from the next/previous element froom the 
		// selected element
		int increment = (bias == Position.Bias.Forward) ? 1 : -1;
		int row = startingRow;

		do {
		    TreePath path = getPathForRow(row);
		    String text = convertValueToText( path.getLastPathComponent(), isRowSelected(row), isExpanded(row), true, row, false);
		    
		    if ( text != null && text.toUpperCase().startsWith(prefix)) {
				return path;
		    }
		    row = (row + increment + max) % max;
		} while (row != startingRow);

		return null;
	}
} 
