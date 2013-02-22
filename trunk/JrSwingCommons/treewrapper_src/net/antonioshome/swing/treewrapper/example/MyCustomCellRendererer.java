/*
 * $Id: MyCustomCellRendererer.java,v 1.1 2006/05/05 18:42:47 aviva Exp $
 * Read the "license.txt" file for licensing information.
 * (C) Antonio Vieiro. All rights reserved.
 */

package net.antonioshome.swing.treewrapper.example;

import java.awt.Component;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

/**
 * MyCustomCellRendererer is a cell renderer that shows some (ugly, sorry) icons
 * @author Antonio Vieiro (antonio@antonioshome.net), $Author: aviva $
 * @version $Revision: 1.1 $
 */
public class MyCustomCellRendererer
  extends DefaultTreeCellRenderer
{
  ImageIcon orangeIcon;
  ImageIcon appleIcon;
  
  public MyCustomCellRendererer()
  {
    orangeIcon = new ImageIcon( getClass().getResource("orange.png") );
    appleIcon = new ImageIcon( getClass().getResource("apple.png") );
  }

  public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus)
  {
    super.getTreeCellRendererComponent( tree, value, sel,
      expanded, leaf, row, hasFocus );
    
    if ( value.toString().startsWith( "orange") )
      setIcon( orangeIcon );
    else if ( value.toString().startsWith("apple") )
      setIcon( appleIcon );
    else
      setIcon( null );
    
    return this;
  }
  
  
}
