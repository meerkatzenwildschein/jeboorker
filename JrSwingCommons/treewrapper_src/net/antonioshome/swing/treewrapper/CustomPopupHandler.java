/*
 * $Id: CustomPopupHandler.java,v 1.1.1.1 2006/04/25 22:08:47 aviva Exp $
 * Read the "license.txt" file for licensing information.
 * (C) Antonio Vieiro. All rights reserved.
 */
package net.antonioshome.swing.treewrapper;

import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.tree.TreeNode;

/**
 * CustomPopupHandler is responsible for selecting which JPopupMenu must
 *   be shown to the user then the user performs a popup trigger gesture.
 * @author Antonio Vieiro (antonio@antonioshome.net), $Author: aviva $
 * @version $Revision: 1.1.1.1 $
 */
public interface CustomPopupHandler
{
  /**
   * Returns a JPopupMenu suitable for rendering when the user
   *  triggers a popup menu in a JTree in a specific TreeNode.
   * @param aJTree the JTree where the user makes a popup trigger
   *  gesture.
   * @param aTreeNode the TreeNode over which the user has performed a
   *  popup trigger gesture.
   * @return the JPopupMenu to be presented to the user, or null
   *  if no menu is to be presented.
   */
  public JPopupMenu getMenuAt( JTree aJTree, TreeNode aTreeNode );
}
