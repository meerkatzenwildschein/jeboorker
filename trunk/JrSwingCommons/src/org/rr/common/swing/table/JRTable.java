package org.rr.common.swing.table;

import java.util.logging.Level;

import javax.swing.CellEditor;
import javax.swing.JTable;

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
    
    
}
