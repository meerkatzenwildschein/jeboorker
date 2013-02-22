package org.rr.jeborker.gui.renderer;

import java.awt.Component;
import java.util.EventObject;
import java.util.List;

import javax.swing.AbstractCellEditor;
import javax.swing.JTree;
import javax.swing.tree.TreeCellEditor;

import org.rr.jeborker.JeboorkerPreferences;
import org.rr.jeborker.db.QueryCondition;
import org.rr.jeborker.gui.MainController;

public class ResourceHandlerTreeCellEditor extends AbstractCellEditor implements TreeCellEditor {

	private static final String QUERY_IDENTIFER = ResourceHandlerTreeCellEditor.class.getName();

	private ResourceHandlerTreeCellRenderer renderer;
	
	private JTree tree;
	
	private Object previousEditorValue;
	
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
			List<String> basePath = JeboorkerPreferences.getBasePath();
			if(!basePath.contains(cellEditorValue.toString())) {
				setPathFilter(cellEditorValue.toString());
				MainController.getController().refreshTable(true);
			} else {
				boolean remove = removePathFilter();
				if(remove) {
					MainController.getController().refreshTable(true);
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
