package org.rr.jeborker.gui.renderer;

import java.awt.Component;
import java.awt.Insets;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import org.rr.commons.collection.CompoundList;
import org.rr.commons.utils.ListUtils;
import org.rr.commons.utils.StringUtils;
import org.rr.commons.utils.UtilConstants;

import com.l2fprod.common.beans.editor.AbstractPropertyEditor;

/**
 * ComboBoxPropertyEditor. <br>
 * 
 */
public class MultiListPropertyEditor extends AbstractPropertyEditor {

	private static final String noChanges = "<" + Bundle.getString("ComboBoxPropertyEditor.noChanges") + ">";
	private static final String clear = "<" + Bundle.getString("ComboBoxPropertyEditor.clear") + ">";
	private Object oldValue;
	private Icon[] icons;
	private boolean inputChanged = false;

	public MultiListPropertyEditor() {
		editor = new JComboBox() {
			public void setSelectedItem(Object anObject) {
				oldValue = getSelectedItem();
				super.setSelectedItem(anObject);
			}
		};

		final JComboBox combo = (JComboBox) editor;
		
		combo.setEditable(true);
		((JTextField)combo.getEditor().getEditorComponent()).setBorder(new EmptyBorder(new Insets(0, 3, 0, 0)));
		combo.getEditor().getEditorComponent().addFocusListener(new FocusAdapter() {

			@Override
			public void focusGained(FocusEvent e) {
				String text = ((JTextField)combo.getEditor().getEditorComponent()).getText();
				if(noChanges.equals(text) || clear.equals(text)) {
					((JTextField)combo.getEditor().getEditorComponent()).setText("");
					SwingUtilities.invokeLater(new Runnable() {
						
						@Override
						public void run() {
							combo.setSelectedIndex(1);
						}
					});
				} 
			}

		});
		combo.getEditor().getEditorComponent().addKeyListener(new KeyAdapter() {

			@Override
			public void keyTyped(KeyEvent e) {
				inputChanged = true;
			}
			
		});
			
		combo.setRenderer(new Renderer());
		combo.addPopupMenuListener(new PopupMenuListener() {
			public void popupMenuCanceled(PopupMenuEvent e) {
			}

			public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
				MultiListPropertyEditor.this.firePropertyChange(oldValue, combo.getSelectedItem());
			}

			public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
			}
		});
		combo.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER || e.getKeyCode() == KeyEvent.VK_TAB) {
					MultiListPropertyEditor.this.firePropertyChange(oldValue, combo.getSelectedItem());
				}
			}
		});
		combo.setSelectedIndex(-1);
	}

	public Object getValue() {
		Object result = ((JTextField)((JComboBox) editor).getEditor().getEditorComponent()).getText();;
		if(clear.equals(result)) {
			result = "";
		} else if(noChanges.equals(result)) {
			result = null;
		} else if(inputChanged) {
			result = ((JTextField)((JComboBox) editor).getEditor().getEditorComponent()).getText();
		} else if(result != null && result.toString().isEmpty()) {
			result = null;
		} 

		return result;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void setValue(Object value) {
		List viewList = new ArrayList(Arrays.asList(new Object[((List) value).size()]));
		Collections.copy(viewList, ((List) value));
		
		//the first one is the value which is stored to all selected ebooks.
		Object selectedValue = viewList.remove(0);
		viewList = ListUtils.distinct(viewList, UtilConstants.COMPARE_BINARY);
		viewList.remove("");
		viewList = new CompoundList(Arrays.asList(new Object[] {noChanges, clear}), viewList);
		Object[] values = viewList.toArray(new Object[viewList.size()]);
		this.setAvailableValues(values);
		if(selectedValue == null) {
			((JComboBox) editor).setSelectedIndex(0);
		} else if(StringUtils.toString(selectedValue).isEmpty()) {
			((JComboBox) editor).setSelectedIndex(1);
		} else {
			((JComboBox) editor).setSelectedItem(selectedValue);
		}
	}

	public void setAvailableValues(Object[] values) {
		((JComboBox) editor).setModel(new DefaultComboBoxModel(values));
	}

	public void setAvailableIcons(Icon[] icons) {
		this.icons = icons;
	}

	private class Renderer extends DefaultListCellRenderer {
		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			Component component = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			if (icons != null && index >= 0 && component instanceof JLabel) {
				((JLabel) component).setIcon(icons[index]);
			}
			return component;
		}
	}
}
