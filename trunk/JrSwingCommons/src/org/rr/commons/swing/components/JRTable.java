package org.rr.commons.swing.components;

import java.util.logging.Level;

import javax.swing.CellEditor;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableModel;

import org.rr.commons.log.LoggerFactory;

public class JRTable extends JTable {

	private boolean stopEditOnSelectionChange = false;
	
	public JRTable() {
	    getSelectionModel().addListSelectionListener(new StopEditListSelectionListener());
	}
	
    /**
     * Stops the editing of the current table cell.
     * @return The editor that was stopped. <code>null</code> is returned if 
     *     the table isn't in edit mode.
     */
    public synchronized CellEditor stopEdit() {
        try {
            if (isEditing()) {
                CellEditor editor = getCellEditor(editingRow, editingColumn);
                if (editor == null) {
                    editor = getDefaultEditor(getColumnClass(editingColumn));
                }
                editor.stopCellEditing();
                removeEditor();
                return editor;
            }
        } catch (Exception ex) {
            LoggerFactory.getLogger(this).log(Level.INFO, "stopEdit has failed.", ex);
        }
        
        return null;
    }

	@Override
	public void setModel(TableModel dataModel) {
		super.setModel(dataModel);
		
		//after setting the model sometimes the first renderer is rendered with the 
		// wrong background color. A repaint fix these problem.
		SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				repaint();
			}
		});
	}

	public boolean isStopEditOnSelectionChange() {
		return stopEditOnSelectionChange;
	}

	/**
	 * Stops the table edit if the table selection changes.
	 */
	public void setStopEditOnSelectionChange(boolean stopEditOnSelectionChange) {
		this.stopEditOnSelectionChange = stopEditOnSelectionChange;
	} 	
    
    
	private class StopEditListSelectionListener implements ListSelectionListener {

		@Override
		public void valueChanged(ListSelectionEvent e) {
			if(isStopEditOnSelectionChange()) {
				if(getSelectedRow() != getEditingRow()) {
					stopEdit();
				}
			}
		}
		
	}

	@Override
	public void setSelectionModel(ListSelectionModel newModel) {
		super.setSelectionModel(newModel);
		newModel.addListSelectionListener(new StopEditListSelectionListener());
	}
    
}
