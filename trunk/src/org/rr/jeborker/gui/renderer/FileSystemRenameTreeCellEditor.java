package org.rr.jeborker.gui.renderer;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;

import javax.swing.AbstractCellEditor;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.plaf.basic.BasicComboBoxEditor;
import javax.swing.table.TableCellEditor;
import javax.swing.text.JTextComponent;
import javax.swing.tree.TreeCellEditor;


public class FileSystemRenameTreeCellEditor extends AbstractCellEditor implements TreeCellEditor, TableCellEditor {

	/** 
	 * The Swing component being edited. 
	 */
	protected JComboBox<String> editorComponent;
	
	/**
	 * The delegate class which handles all methods sent from the <code>CellEditor</code>.
	 */
	protected Delegate listener;
	
	/**
	 * An integer specifying the number of clicks needed to start editing. Even if <code>clickCountToStart</code> is defined as zero, it will not initiate
	 * until a click occurs.
	 */
	protected int clickCountToStart = 1;
	
	public FileSystemRenameTreeCellEditor(final FileSystemTreeCellEditor editor) {
		this(new FileComboBoxEditorComponent(editor));
	}
	
	/**
	 * Constructs a <code>DefaultCellEditor</code> object that uses a combo box.
	 * 
	 * @param comboBox
	 *            a <code>JComboBox</code> object
	 */
	public FileSystemRenameTreeCellEditor(final JComboBox<String> comboBox) {
		editorComponent = comboBox;
		comboBox.putClientProperty("JComboBox.isTableCellEditor", Boolean.TRUE);
		comboBox.setEditable(true);
		listener = new Delegate() {
			public void setValue(Object value) {
				comboBox.setSelectedItem(value);
			}

			public Object getCellEditorValue() {
				return comboBox.getSelectedItem();
			}

		};
		comboBox.addActionListener(listener);
	}

	/**
	 * Returns a reference to the editor component.
	 * 
	 * @return the editor <code>Component</code>
	 */
	public Component getComponent() {
		return editorComponent;
	}

	/**
	 * Specifies the number of clicks needed to start editing.
	 * 
	 * @param count
	 *            an int specifying the number of clicks needed to start editing
	 * @see #getClickCountToStart
	 */
	public void setClickCountToStart(int count) {
		clickCountToStart = count;
	}

	/**
	 * Returns the number of clicks needed to start editing.
	 * 
	 * @return the number of clicks needed to start editing
	 */
	public int getClickCountToStart() {
		return clickCountToStart;
	}

	/**
	 * Forwards the message from the <code>CellEditor</code> to the <code>delegate</code>.
	 * 
	 * @see Delegate#getCellEditorValue
	 */
	public Object getCellEditorValue() {
		return listener.getCellEditorValue();
	}
	
	/**
	 * Returns true if <code>anEvent</code> is <b>not</b> a <code>MouseEvent</code>. Otherwise, it returns true if the necessary number of clicks have
	 * occurred, and returns false otherwise.
	 * 
	 * @param anEvent
	 *            the event
	 * @return true if cell is ready for editing, false otherwise
	 * @see #setClickCountToStart
	 * @see #shouldSelectCell
	 */
	public boolean isCellEditable(EventObject anEvent) {
		if (anEvent instanceof MouseEvent) {
			return ((MouseEvent) anEvent).getClickCount() >= clickCountToStart;
		}
		return true;
	}
	
	/**
	 * Returns true to indicate that the editing cell may be selected.
	 * 
	 * @param anEvent
	 *            the event
	 * @return true
	 * @see #isCellEditable
	 */
	public boolean shouldSelectCell(EventObject anEvent) {
		if (anEvent instanceof MouseEvent) {
			MouseEvent e = (MouseEvent) anEvent;
			return e.getID() != MouseEvent.MOUSE_DRAGGED;
		}
		return true;
	}

	/**
	 * Stops editing and returns true to indicate that editing has stopped. This method calls <code>fireEditingStopped</code>.
	 * 
	 * @return true
	 */
	public boolean stopCellEditing() {
		if (editorComponent.isEditable()) {
			// Commit edited value.
			editorComponent.actionPerformed(new ActionEvent(FileSystemRenameTreeCellEditor.this, 0, ""));
		}		
		fireEditingStopped();
		return true;
	}

	/**
	 * Cancels editing. This method calls <code>fireEditingCanceled</code>.
	 */
	public void cancelCellEditing() {
		fireEditingCanceled();
	}

	/** 
	 * Implements the <code>TreeCellEditor</code> interface. 
	 */
	public Component getTreeCellEditorComponent(JTree tree, Object value, boolean isSelected, boolean expanded, boolean leaf, int row) {
		String stringValue = tree.convertValueToText(value, isSelected, expanded, leaf, row, false);

		listener.setValue(stringValue);
		return editorComponent;
	}

	/** 
	 * Implements the <code>TableCellEditor</code> interface. 
	 */
	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
		listener.setValue(value);
		return editorComponent;
	}

	/**
	 * The protected <code>EditorDelegate</code> class.
	 */
	protected class Delegate implements ActionListener, ItemListener, Serializable {

		/** The value of this cell. */
		protected Object value;

		/**
		 * Returns the value of this cell.
		 * 
		 * @return the value of this cell
		 */
		public Object getCellEditorValue() {
			return value;
		}

		/**
		 * Sets the value of this cell.
		 * 
		 * @param value
		 *            the new value of this cell
		 */
		public void setValue(Object value) {
			this.value = value;
		}

		/**
		 * Returns true to indicate that editing has begun.
		 * 
		 * @param anEvent
		 *            the event
		 */
		public boolean startCellEditing(EventObject anEvent) {
			return true;
		}

		/**
		 * When an action is performed, editing is ended.
		 * 
		 * @param e
		 *            the action event
		 * @see #stopCellEditing
		 */
		public void actionPerformed(ActionEvent e) {
			FileSystemRenameTreeCellEditor.this.stopCellEditing();
		}

		/**
		 * When an item's state changes, editing is ended.
		 * 
		 * @param e
		 *            the action event
		 * @see #stopCellEditing
		 */
		public void itemStateChanged(ItemEvent e) {
			FileSystemRenameTreeCellEditor.this.stopCellEditing();
		}
	}
	
	private static class FileComboBoxEditorComponent extends JComboBox<String> {
		
		FileSystemTreeCellEditor editor;
		
		public FileComboBoxEditorComponent(FileSystemTreeCellEditor editor) {
			super();
			setEditor(new FileComboBoxEditor());
			setModel(new FileComboBoxModel(this));
			this.editor = editor;
		}
		
        /**
         * Overrides <code>JTextField.getPreferredSize</code> to
         * return the preferred size based on current font, if set,
         * or else use renderer's font.
         * @return a <code>Dimension</code> object containing
         *   the preferred size
         */
        public Dimension getPreferredSize() {
            Dimension rendererPreferredSize = editor.getRendererPreferredSize();
            rendererPreferredSize.width += 10;
            return rendererPreferredSize;
        }		
        
        private static class FileComboBoxEditor extends BasicComboBoxEditor {

            protected JTextField createEditorComponent() {
                JTextField editor = new JTextField("", 9) {

					@Override
					public void selectAll() {
						String text = getText();
						if(text.lastIndexOf('.') != -1) {
							select(0, text.lastIndexOf('.'));
						} else {
							super.selectAll();
						}
					}
                	
                };
                editor.setBorder(null);
                return editor;
            }  
        	
        }
        
        /**
         * The {@link ComboBoxModel} with some file name offers. 
         */
        private static class FileComboBoxModel extends DefaultComboBoxModel<String> {
        	
        	private FileComboBoxEditorComponent combobox;
        	
        	private FileComboBoxModel(FileComboBoxEditorComponent combobox) {
        		this.combobox = combobox;
        	}
        	
        	/**
        	 * Get the text value from the {@link JComboBox} text editor component.
        	 */
        	private String getEditorValue() {
        		BasicComboBoxEditor be = (BasicComboBoxEditor) combobox.getEditor();
        		JTextComponent edc = (JTextComponent) be.getEditorComponent();
        		return edc.getText();
        	}
        	
        	private List<String> createValues() {
        		final ArrayList<String> values = new ArrayList<String>();
        		final String editorValue = getEditorValue();
        		values.addAll(FileSystemRenameTreeUtils.getCommaChangeOffers(editorValue));
        		return values;
        	}
        	
			@Override
			public int getSize() {
				return createValues().size();
			}

			@Override
			public String getElementAt(int index) {
				return createValues().get(index);
			}

        }
	}

}
