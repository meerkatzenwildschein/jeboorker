package org.rr.jeborker.gui.renderer;

import java.awt.Component;
import java.io.File;
import java.util.EventObject;

import javax.swing.AbstractCellEditor;
import javax.swing.JTree;
import javax.swing.tree.TreeCellEditor;

import org.rr.commons.utils.StringUtils;
import org.rr.jeborker.db.QueryCondition;
import org.rr.jeborker.gui.MainController;

public class BasePathTreeCellEditor extends AbstractCellEditor implements TreeCellEditor {

	private static final String QUERY_IDENTIFER = BasePathTreeCellEditor.class.getName();

	private BasePathTreeCellRenderer renderer;
	
	private Object previousEditorValue;
	
	public BasePathTreeCellEditor(JTree tree) {
		renderer = new BasePathTreeCellRenderer(tree);
	}

	public boolean isCellEditable(EventObject event) {
		boolean returnValue = true;
		return returnValue;
	}

	public Component getTreeCellEditorComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row) {
		Component editor = renderer.getTreeCellRendererComponent(tree, value, true, expanded, leaf, row, true);

		editingStarted();
		return editor;
	}

	@Override
	public Object getCellEditorValue() {
		return renderer.getValue();
	}
	
	private void editingStarted() {
		Object cellEditorValue = getCellEditorValue();
		if(cellEditorValue == null || !cellEditorValue.equals(previousEditorValue)) {
			String cellEditorValueString = StringUtils.toString(cellEditorValue);
			if(cellEditorValueString.indexOf(File.separatorChar) != -1) {
				setPathFilter(cellEditorValue.toString());
				MainController.getController().refreshTable();
			} else {
				boolean remove = removePathFilter();
				if(remove) {
					MainController.getController().refreshTable();
				}				
			}
		}
		previousEditorValue = cellEditorValue;
	}
	
	private void setPathFilter(String fullResourceFilterPath) {
		final QueryCondition rootCondition = MainController.getController().getTableModel().getQueryCondition();
		final QueryCondition additionalFilterCondition = new QueryCondition(null,null,null, QUERY_IDENTIFER);
		
		removePathFilter();
		additionalFilterCondition.addOrChild(new QueryCondition("file", fullResourceFilterPath + "%", "like", QUERY_IDENTIFER));
		rootCondition.addAndChild(additionalFilterCondition);		
	}
	
	/**
	 * Removes a previously set filter condition. Invoke <code>MainController.getController().refreshTable(true);</code> for
	 * pushing the change into the view.
	 * 
	 * @return <code>true</code> if removing was sucessfully and <code>false</code> if nothing was found to remove.
	 */
	private boolean removePathFilter() {
		final QueryCondition rootCondition = MainController.getController().getTableModel().getQueryCondition();
		return rootCondition.removeConditionByIdentifier(QUERY_IDENTIFER); //remove possibly existing search conditions	
	}

}
