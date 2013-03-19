package org.rr.common.swing.table;

import java.util.logging.Level;

import javax.swing.CellEditor;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.TableModel;

import org.rr.commons.log.LoggerFactory;

public class JRTable extends JTable {

    /**
     * Stops the editing of the current table cell.
     */
    public synchronized void stopEdit() {
        try {
            if (isEditing()) {
                CellEditor editor = getCellEditor(editingRow, editingColumn);
                if (editor == null) {
                    editor = getDefaultEditor(getColumnClass(editingColumn));
                }
                editor.stopCellEditing();
                removeEditor();
            }
        } catch (Exception ex) {
            LoggerFactory.getLogger(this).log(Level.INFO, "stopEdit has failed.", ex);
        }
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
    
    
    
}
