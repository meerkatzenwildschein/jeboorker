/*
 * $Id: TreeModelBuilder.java,v 1.1 2006/05/05 18:42:47 aviva Exp $
 * Read the "license.txt" file for licensing information.
 * (C) Antonio Vieiro. All rights reserved.
 */

package net.antonioshome.swing.treewrapper.example;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

/**
 * TreeModelBuilder builds tree models for the sample.
 * @author Antonio Vieiro (antonio@antonioshome.net), $Author: aviva $
 * @version $Revision: 1.1 $
 */
final class TreeModelBuilder
{
  static DefaultTreeModel createModel()
  {
    DefaultMutableTreeNode root = new DefaultMutableTreeNode("sample-tree");
    
    DefaultTreeModel model = new DefaultTreeModel( root );

    DefaultMutableTreeNode oranges = new DefaultMutableTreeNode("oranges");
    model.insertNodeInto( oranges, root, 0 );
    
    for( int i=0; i<5; i++ )
      model.insertNodeInto( new DefaultMutableTreeNode("orange " + (i+1) ), oranges, i );
    
    DefaultMutableTreeNode apples = new DefaultMutableTreeNode("apples");
    model.insertNodeInto( apples, root, 1 );
    for( int i=0; i<3; i++ )
      model.insertNodeInto( new DefaultMutableTreeNode("apple " + (i+1)), apples, i );

    return model;
  }
}
