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
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.ListCellRenderer;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.plaf.basic.BasicArrowButton;
import javax.swing.plaf.basic.BasicComboBoxEditor;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.table.TableCellEditor;
import javax.swing.text.JTextComponent;
import javax.swing.tree.TreeCellEditor;

import org.apache.commons.io.FilenameUtils;
import org.rr.commons.utils.ListUtils;
import org.rr.commons.utils.StringUtils;
import org.rr.commons.utils.UtilConstants;


public class FileSystemRenameTreeCellEditor extends AbstractCellEditor implements TreeCellEditor, TableCellEditor {

	/** 
	 * The Swing component being edited. 
	 */
	protected JComboBox<String> editorComponent;
	
	/**
	 * The delegate class which handles all methods sent from the <code>CellEditor</code>.
	 */
	protected Delegate editorDelegate;
	
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
	private FileSystemRenameTreeCellEditor(final JComboBox<String> comboBox) {
		editorComponent = comboBox;
		comboBox.putClientProperty("JComboBox.isTableCellEditor", Boolean.TRUE);
		comboBox.setEditable(true);
		editorDelegate = new Delegate() {
			public void setValue(Object value) {
        		BasicComboBoxEditor be = (BasicComboBoxEditor) comboBox.getEditor();
        		JTextComponent edc = (JTextComponent) be.getEditorComponent();
        		edc.setText(StringUtils.toString(value));
			}

			public Object getCellEditorValue() {
        		BasicComboBoxEditor be = (BasicComboBoxEditor) comboBox.getEditor();
        		JTextComponent edc = (JTextComponent) be.getEditorComponent();
        		return edc.getText();				
			}

		};
		comboBox.addActionListener(editorDelegate);
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
		return editorDelegate.getCellEditorValue();
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

		editorDelegate.setValue(stringValue);
		return editorComponent;
	}

	/** 
	 * Implements the <code>TableCellEditor</code> interface. 
	 */
	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
		editorDelegate.setValue(value);
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
	     * Resets the UI property to a value from the current look and feel.
	     *
	     * @see JComponent#updateUI
	     */
	    public void updateUI() {
	        setUI(new BasicComboBoxUI() {
	        	@Override
	            protected JButton createArrowButton() {
	            	return new BasicArrowButton(BasicArrowButton.SOUTH,
                            UIManager.getColor("ComboBox.buttonBackground"),
                            UIManager.getColor("ComboBox.buttonShadow"),
                            UIManager.getColor("ComboBox.buttonDarkShadow"),
                            UIManager.getColor("ComboBox.buttonHighlight")) {
	            		
	            		@Override
	            		public int getWidth() {
	            			if(FileComboBoxEditorComponent.this.getModel().getSize() > 0) {
	            				return super.getWidth();
	            			} else {
	            				return 0;	            				
	            			}
	            		}
	            	};
	            }
	        	
        		@Override
        		public void setPopupVisible(JComboBox c, boolean visible) {
        		    // keeps the popup from coming down if there's nothing in the combo box
        			if(!visible) {
        				super.setPopupVisible(c, visible);
        			} else if (FileComboBoxEditorComponent.this.getModel().getSize() > 0) {
        		    	super.setPopupVisible(c, visible);
        		    }
        		}		        	
	        	
	        });

	        ListCellRenderer<? super String> renderer = getRenderer();
	        if (renderer instanceof Component) {
	            SwingUtilities.updateComponentTreeUI((Component)renderer);
	        }
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
        		values.addAll(getCommaChangeOffers(editorValue));
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
			
			/**
			 * Get some change offers for the given file name.
			 */
			private List<String> getCommaChangeOffers(final String filename) {
				List<String> offers = new ArrayList<String>() {

					@Override
					public boolean add(String e) {
						String clean = clean(e);
						return super.add(clean);
					}

					@Override
					public boolean remove(Object o) {
						String clean = clean((String) o);
						return super.remove(clean);
					}
					
					/**
					 * Clean up the given string.
					 */
					private String clean(String s) {
						String result = s;
						result = StringUtils.replace(s, "  ", "");
						result = result.trim();
						return result;
					}			
				};
				String name = FilenameUtils.getBaseName(filename);
				String ext = FilenameUtils.getExtension(filename);
				if(name.indexOf(',') != -1) {
					offers.add(name.replaceAll("([\\w\\.]*),\\s{0,1}([\\w\\.]*)", "$2 $1").trim() + "." + ext);
					offers.add(name.replaceAll("([\\w\\.]*\\s[\\w\\.]*),\\s{0,1}([\\w\\.]*)", "$2 $1").trim() + "." + ext);
					offers.add(name.replaceAll("([\\w\\.]*\\s[\\w\\.]*),\\s{0,1}([\\w\\.]*\\s\\w*)", "$2 $1").trim() + "." + ext);
					offers.add(name.replaceAll("([\\w\\.]*),\\s{0,1}([\\w\\.]*)\\s([\\w\\.]{1,})", "$2 $3 $1").trim() + "." + ext);
				}
				
				offers = ListUtils.distinct(offers);
				List<String> toRemove = new ArrayList<String>();
				toRemove.add(filename);
				
				int nameWhiteSpaces = StringUtils.occurrence(name, " ", UtilConstants.COMPARE_BINARY);
				for(String offer : offers) {
					if(StringUtils.occurrence(offer, " ", UtilConstants.COMPARE_BINARY) != nameWhiteSpaces) {
						toRemove.add(offer);
					}
				}
				offers.removeAll(toRemove);
				
				List<String> toAdd = new ArrayList<String>(offers.size());
				for(int i = 0; i < offers.size(); i++) {
					String offer = offers.get(i);
					if(offer.indexOf(" - ") != -1) {
						List<String> split = ListUtils.split(offer, " - ");
						if(split.size() == 2) {
							String sName = FilenameUtils.getBaseName(split.get(1));
							String sExt = FilenameUtils.getExtension(split.get(1));
							toAdd.add(sName + " - " + split.get(0) + "." + sExt);	
						}
					}
				}
				offers.addAll(toAdd);
				
				return offers;
			}			

        }
	}

}
