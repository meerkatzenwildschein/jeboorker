package org.rr.jeborker.gui.cell;

import java.awt.Component;
import java.util.EventObject;

import javax.swing.AbstractCellEditor;
import javax.swing.JTree;
import javax.swing.tree.TreeCellEditor;

public class BasePathTreeCellEditor extends AbstractCellEditor implements TreeCellEditor {

	private BasePathTreeCellRenderer renderer;
	
	public BasePathTreeCellEditor(JTree tree) {
		renderer = new BasePathTreeCellRenderer(tree);
	}

	public boolean isCellEditable(EventObject event) {
		boolean returnValue = true;
		return returnValue;
	}

	public Component getTreeCellEditorComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row) {
		return renderer.getTreeCellRendererComponent(tree, value, true, expanded, leaf, row, true);
	}

	@Override
	public Object getCellEditorValue() {
		return renderer.getValue();
	}

}
