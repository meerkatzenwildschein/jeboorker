package org.rr.jeborker.gui.cell;

import static org.rr.commons.utils.StringUtils.EMPTY;

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
import javax.swing.border.EmptyBorder;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import org.rr.commons.collection.CompoundList;
import org.rr.commons.utils.ListUtils;
import org.rr.commons.utils.StringUtils;
import org.rr.commons.utils.UtilConstants;

import com.l2fprod.common.beans.editor.AbstractPropertyEditor;

public class MultiListPropertyEditor extends AbstractPropertyEditor {

	private static final String noChanges = "<" + Bundle.getString("ComboBoxPropertyEditor.noChanges") + ">";
	private static final String clear = "<" + Bundle.getString("ComboBoxPropertyEditor.clear") + ">";
	private Object oldValue;
	private Icon[] icons;
	private boolean editorInputChanged = false;
	private boolean selectionChanged = false;

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
				if(noChanges.equals(text) || clear.equals(text) || text == null) {
					((JTextField)combo.getEditor().getEditorComponent()).setText(EMPTY);
				} 
			}

		});

		combo.getEditor().getEditorComponent().addKeyListener(new KeyAdapter() {

			@Override
			public void keyTyped(KeyEvent e) {
				editorInputChanged = true;
				if (e.getKeyChar() == '\n' || e.getKeyChar() == '\t') {
					MultiListPropertyEditor.this.firePropertyChange(oldValue, combo.getSelectedItem());
				}				
			}
			
		});
			
		combo.setRenderer(new Renderer());
		combo.addPopupMenuListener(new PopupMenuListener() {
			public void popupMenuCanceled(PopupMenuEvent e) {
			}

			public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
				MultiListPropertyEditor.this.firePropertyChange(oldValue, combo.getSelectedItem());
				selectionChanged = true;
			}

			public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
				selectionChanged = true;
			}
		});

		combo.setSelectedIndex(-1);
	}

	public Object getValue() {
		int selectedIndex = ((JComboBox) editor).getSelectedIndex();
		Object result;
		if(selectedIndex == -1) {
			//editor value
			result = ((JTextField)((JComboBox) editor).getEditor().getEditorComponent()).getText();
		} else if(!editorInputChanged && selectedIndex == 0) {
			//no change
			result = null;
		} else if(!(editorInputChanged || selectionChanged) && selectedIndex == 1) {
			//no change
			result = null;
		} else if((editorInputChanged || selectionChanged) && selectedIndex == 1) {
			//clear
			result = EMPTY;
		} else {
			result = ((JTextField)((JComboBox) editor).getEditor().getEditorComponent()).getText();
		}
		return result;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void setValue(Object value) {
		List viewList = new ArrayList(Arrays.asList(new Object[((List) value).size()]));
		Collections.copy(viewList, ((List) value));
		
		//the first one is the value which is stored to all selected ebooks.
		Object selectedValue = viewList.remove(0);
		viewList = ListUtils.distinct(viewList, UtilConstants.COMPARE_TEXT);
		viewList.remove(EMPTY);
		viewList.remove(null);
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
