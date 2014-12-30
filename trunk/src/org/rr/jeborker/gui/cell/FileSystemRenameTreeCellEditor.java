package org.rr.jeborker.gui.cell;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.AbstractCellEditor;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.text.NavigationFilter;
import javax.swing.text.Position;
import javax.swing.tree.TreeCellEditor;

import org.apache.commons.io.FilenameUtils;
import org.rr.commons.collection.LRUCacheMap;
import org.rr.commons.swing.SwingUtils;
import org.rr.commons.swing.components.button.JMediumWeightPopupMenu;
import org.rr.commons.swing.components.resources.ImageResourceBundle;
import org.rr.commons.utils.ListUtils;
import org.rr.commons.utils.StringUtils;
import org.rr.commons.utils.UtilConstants;

public class FileSystemRenameTreeCellEditor extends AbstractCellEditor implements TreeCellEditor {

	private static final ImageIcon ARROW_IMAGE = ImageResourceBundle.getResourceAsImageIcon("arrow.gif");
	
	/** 
	 * The Swing component being edited. 
	 */
	protected JPanel editorComponent;
	
	private JButton arrowButton;
	
	private JTextField textField;
	
	private LRUCacheMap<String, List<String>> latestFileNameOffers = new LRUCacheMap<String, List<String>>(3);
	
	/**
	 * A wrapper for the actions to be displayed when the pop-up is drawn.
	 */
	private class ActionOptionWrapper extends AbstractAction {
		
		private static final long serialVersionUID = -7161990723874074407L;
		
		private Action internalAction = null;
		
		/**
		 * Creates a new instance of a wrapper action
		 * 
		 * @param wrapAction
		 *            The action to be wrapped
		 */
		public ActionOptionWrapper(Action wrapAction) {
			internalAction = wrapAction;
			this.setEnabled(wrapAction.isEnabled());
			putValue(Action.NAME, wrapAction.getValue(Action.NAME));
			putValue(Action.SMALL_ICON, wrapAction.getValue(Action.SMALL_ICON));
		}

		/**
		 * Fired when the action is performed
		 * 
		 * @param actionEvent
		 *            The action event
		 */
		public void actionPerformed(ActionEvent actionEvent) {
			internalAction.actionPerformed(actionEvent);
		}
	}	
	
	/**
	 * NavigationFilter for the text field that did not allow to navigate behind the 
	 * file extension.
	 */
	private class RenameFieldNavigationFilter extends NavigationFilter {
		
		public void setDot(NavigationFilter.FilterBypass fb, int dot, Position.Bias bias) {
			int dotIdx = textField.getText().lastIndexOf('.');
			if (dotIdx >= 0 && dot >= dotIdx) {
				fb.setDot(dotIdx, bias);
			} else {
				fb.setDot(dot, bias);
			}
		}

		public void moveDot(NavigationFilter.FilterBypass fb, int dot, Position.Bias bias) {
			int dotIdx = textField.getText().lastIndexOf('.');
			if (dotIdx >= 0 && dot >= dotIdx) {
				fb.moveDot(dotIdx, bias);
			} else {
				fb.moveDot(dot, bias);
			}
		}
	}
	
	/**
	 * Constructs a <code>DefaultCellEditor</code> object that uses a combo box.
	 * 
	 * @param comboBox
	 *            a <code>JComboBox</code> object
	 */
	protected FileSystemRenameTreeCellEditor() {
		createEditorComponent();
	}
	
	
	private JPanel createEditorComponent() {
		editorComponent = new JPanel();
		editorComponent.setLayout(new BorderLayout());
		textField = new JTextField("", 9) {

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
        
		textField.setNavigationFilter(new RenameFieldNavigationFilter());

        textField.setBorder(null);
        textField.addKeyListener(new KeyAdapter() {

			@Override
			public void keyPressed(KeyEvent e) {
				if(e.getKeyCode() == KeyEvent.VK_DOWN) {
					e.consume();
					
					//open menu on key down
					arrowButton.getAction().actionPerformed(null);
				} else if(e.getKeyCode() == KeyEvent.VK_ENTER) {
					stopCellEditing();
				}
			}
        	
		});
        editorComponent.add(textField, BorderLayout.CENTER);
        
        arrowButton = new JButton();
        arrowButton.setAction(new AbstractAction() {
			
			@Override
			public void actionPerformed(ActionEvent ev) {
				final Component focusOwner = SwingUtils.getSurroundingComponent(editorComponent, JTree.class);
				JPopupMenu.setDefaultLightWeightPopupEnabled(false);
				
				JPopupMenu popup = new JMediumWeightPopupMenu();
				popup.setLightWeightPopupEnabled(false);
				
				List<String> offers = getCommaChangeOffers(textField.getText());
				for (final String offer : offers) {
					AbstractAction action  = new AbstractAction() {
						
						@Override
						public void actionPerformed(ActionEvent e) {
							textField.setText(offer);
							stopCellEditing();
							if(focusOwner != null) {
								focusOwner.requestFocus();
							}
						}
					};		
					action.putValue(Action.NAME, offer);
					popup.add(new ActionOptionWrapper(action));	
				}
				
				popup.setMinimumSize(new Dimension(getEditorWidth(textField.getText()), offers.size() * 25));
				popup.show(editorComponent, 0, editorComponent.getHeight());
			}
		});
        arrowButton.setIcon(ARROW_IMAGE);
        editorComponent.add(arrowButton, BorderLayout.EAST);
        return editorComponent;		
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
	 * Get the current cell editor value. 
	 */
	public Object getCellEditorValue() {
		return textField.getText();
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
		if (anEvent == null || anEvent instanceof KeyEvent) {
			return true;
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
		final String stringValue = tree.convertValueToText(value, isSelected, expanded, leaf, row, false);
		
		int width = getEditorWidth(stringValue);
		int height = editorComponent.getPreferredSize().height;
		editorComponent.setPreferredSize(new Dimension(width, height));

		textField.setText(stringValue);
		if(getCommaChangeOffers(stringValue).isEmpty()) {
			arrowButton.setVisible(false);
		} else {
			arrowButton.setVisible(true);
		}
		return editorComponent;
	}

	private int getEditorWidth(String stringValue) {
		Dimension textDimension = SwingUtils.getTextDimension(stringValue, textField.getFont());
		int width = textDimension.width + arrowButton.getPreferredSize().width;
		return width;
	}
	
	/**
	 * Get some change offers for the given file name.
	 */
	private List<String> getCommaChangeOffers(final String filename) {
		List<String> cachedOffer = latestFileNameOffers.get(filename);
		if(cachedOffer != null) {
			return cachedOffer;
		}
		
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
		List<String> toRemove = new ArrayList<>();
		toRemove.add(filename);
		
		int nameWhiteSpaces = StringUtils.occurrence(name, " ", UtilConstants.COMPARE_BINARY);
		for(String offer : offers) {
			if(StringUtils.occurrence(offer, " ", UtilConstants.COMPARE_BINARY) != nameWhiteSpaces) {
				toRemove.add(offer);
			}
		}
		offers.removeAll(toRemove);
		
		List<String> toAdd = new ArrayList<>(offers.size());
		for(int i = 0; i < offers.size(); i++) {
			String offer = offers.get(i);
			if(offer.contains(" - ")) {
				List<String> split = ListUtils.split(offer, " - ");
				if(split.size() == 2) {
					String sName = FilenameUtils.getBaseName(split.get(1));
					String sExt = FilenameUtils.getExtension(split.get(1));
					toAdd.add(sName + " - " + split.get(0) + "." + sExt);	
				}
			}
		}
		offers.addAll(toAdd);
		latestFileNameOffers.put(filename, offers);
		return offers;
	}
}
