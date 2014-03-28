package org.rr.jeborker.gui.cell;

import java.awt.Component;
import java.io.File;
import java.sql.SQLException;
import java.util.EventObject;
import java.util.logging.Level;

import javax.swing.AbstractCellEditor;
import javax.swing.JTree;
import javax.swing.tree.TreeCellEditor;
import javax.swing.tree.TreePath;

import org.rr.commons.log.LoggerFactory;
import org.rr.commons.utils.StringUtils;
import org.rr.jeborker.db.item.EbookPropertyItem;
import org.rr.jeborker.gui.MainController;
import org.rr.jeborker.gui.model.BasePathTreeModel;
import org.rr.jeborker.gui.model.EbookPropertyDBTableModel;

import com.j256.ormlite.stmt.Where;

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

		editingStarted(tree, row);
		return editor;
	}

	@Override
	public Object getCellEditorValue() {
		return renderer.getValue();
	}
	
	private void editingStarted(JTree tree, int row) {
		final TreePath filterTreePath = tree.getPathForRow(row);
		final Object cellEditorValue = getCellEditorValue();
		
		if(cellEditorValue == null || !cellEditorValue.equals(previousEditorValue)) {
			String cellEditorValueString = StringUtils.toString(cellEditorValue);
			if(cellEditorValueString.indexOf(File.separatorChar) != -1) {
				setPathFilter(cellEditorValue.toString());
				((BasePathTreeModel)tree.getModel()).setFilterTreePath(filterTreePath);
				MainController.getController().refreshTable();
			} else {
				boolean remove = MainController.getController().getTableModel().removeWhereCondition(QUERY_IDENTIFER);
				((BasePathTreeModel)tree.getModel()).setFilterTreePath(null);
				if(remove) {
					MainController.getController().refreshTable();
				}				
			}
		}
		previousEditorValue = cellEditorValue;
	}
	
	private void setPathFilter(final String fullResourceFilterPath) {
		MainController.getController().getTableModel().addWhereCondition(new EbookPropertyDBTableModel.EbookPropertyDBTableModelQuery() {
			
			@Override
			public String getIdentifier() {
				return QUERY_IDENTIFER;
			}
			
			@Override
			public void appendQuery(Where<EbookPropertyItem, EbookPropertyItem> where) throws SQLException {
				where.like("file", fullResourceFilterPath + "%");
			}
		});

		//additionalFilterCondition.addOrChild(new QueryCondition("file", fullResourceFilterPath + "%", "like", QUERY_IDENTIFER));
		//rootCondition.addAndChild(additionalFilterCondition);		
	}
}
