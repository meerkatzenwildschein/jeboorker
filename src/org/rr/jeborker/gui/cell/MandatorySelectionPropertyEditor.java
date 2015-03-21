package org.rr.jeborker.gui.cell;

import static org.rr.commons.utils.StringUtils.EMPTY;

import java.awt.Component;
import java.awt.Insets;
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

import org.rr.commons.utils.ListUtils;
import org.rr.commons.utils.StringUtils;
import org.rr.commons.utils.UtilConstants;

import com.l2fprod.common.beans.editor.AbstractPropertyEditor;

/**
 * ComboBoxPropertyEditor. <br>
 * 
 */
public class MandatorySelectionPropertyEditor extends AbstractPropertyEditor {

	private Object oldValue;
	private Icon[] icons;

	public MandatorySelectionPropertyEditor() {
		editor = new JComboBox() {
			public void setSelectedItem(Object anObject) {
				oldValue = getSelectedItem();
				super.setSelectedItem(anObject);
			}
		};

		final JComboBox combo = (JComboBox) editor;
		
		combo.setEditable(false);
		((JTextField)combo.getEditor().getEditorComponent()).setBorder(new EmptyBorder(new Insets(0, 3, 0, 0)));
		combo.setRenderer(new Renderer());
		combo.addPopupMenuListener(new PopupMenuListener() {
			public void popupMenuCanceled(PopupMenuEvent e) {
			}

			public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
				MandatorySelectionPropertyEditor.this.firePropertyChange(oldValue, combo.getSelectedItem());
			}

			public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
			}
		});
		
		combo.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER || e.getKeyCode() == KeyEvent.VK_TAB) {
					MandatorySelectionPropertyEditor.this.firePropertyChange(oldValue, combo.getSelectedItem());
				}
			}
		});
		combo.setSelectedIndex(-1);
	}

	public Object getValue() {
		return ((JComboBox) editor).getSelectedItem();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void setValue(Object value) {
		if(value instanceof List) { //always with multiselection
			List viewList = new ArrayList(Arrays.asList(new Object[((List) value).size()]));
			Collections.copy(viewList, ((List) value));
			
			//the first one is the value which is stored to all selected ebooks.
			Object selectedValue = viewList.remove(0);
			viewList = ListUtils.distinct(viewList, UtilConstants.COMPARE_TEXT);
			viewList.remove(EMPTY);
			viewList.remove(null);
			Object[] values = viewList.toArray(new Object[viewList.size()]);
			this.setAvailableValues(values);
			if(selectedValue == null) {
				((JComboBox) editor).setSelectedIndex(0);
			} else if(StringUtils.toString(selectedValue).isEmpty()) {
				((JComboBox) editor).setSelectedIndex(1);
			} else {
				((JComboBox) editor).setSelectedItem(selectedValue);
			}
		} else if(value instanceof String) {
			((JComboBox) editor).setSelectedItem(value);
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
