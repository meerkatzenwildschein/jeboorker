package org.rr.jeborker.gui.renderer;

import java.awt.Component;
import java.util.EventObject;

import javax.swing.AbstractCellEditor;
import javax.swing.JTree;
import javax.swing.tree.TreeCellEditor;

public class ResourceHandlerTreeCellEditor extends AbstractCellEditor implements TreeCellEditor {

	ResourceHandlerTreeCellRenderer renderer;
	
	JTree tree;
	
	public ResourceHandlerTreeCellEditor(JTree tree) {
		this.tree = tree;
		renderer = new ResourceHandlerTreeCellRenderer(tree);
	}

	  public boolean isCellEditable(EventObject event) {
	    boolean returnValue = true;
	    return returnValue;
	  }

	  public Component getTreeCellEditorComponent(JTree tree, Object value,
	      boolean selected, boolean expanded, boolean leaf, int row) {

	    Component editor = renderer.getTreeCellRendererComponent(tree, value,
	        true, expanded, leaf, row, true);

	    return editor;
	  }

	@Override
	public Object getCellEditorValue() {
		return null;
	}

}
