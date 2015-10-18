package org.rr.commons.swing.components;

import static org.rr.commons.utils.StringUtil.EMPTY;

import java.awt.Component;
import java.util.HashSet;
import java.util.List;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.MutableComboBoxModel;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.basic.BasicComboBoxEditor;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;

import org.rr.commons.swing.SwingUtils;
import org.rr.commons.utils.ListUtils;
import org.rr.commons.utils.StringUtil;

public class JREditableHistoryComboBox extends JRComboBox<String> {

	private static final long serialVersionUID = 8405381419161925378L;

	static class FilterFieldComboboxEditor extends BasicComboBoxEditor {

		public Component getEditorComponent() {
			SwingUtils.setColor(editor, false);
			editor.setSelectedTextColor(SwingUtils.getSelectionForegroundColor());
			editor.setSelectionColor(SwingUtils.getSelectionBackgroundColor());
			return editor;
		}

		/**
		 * Creates the internal editor component. Override this to provide a custom implementation.
		 *
		 * @return a new editor component
		 * @since 1.6
		 */
		protected JTextField createEditorComponent() {
			JTextField editor = new BorderlessTextField(EMPTY, 9);
			editor.setBorder(null);
			return editor;
		}

		static class BorderlessTextField extends JTextField {
			public BorderlessTextField(String value, int n) {
				super(value, n);
			}

			// workaround for 4530952
			public void setText(String s) {
				if (getText().equals(s)) {
					return;
				}
				super.setText(s);
			}

			public void setBorder(Border b) {
				if (!(b instanceof UIResource)) {
					super.setBorder(b);
				}
			}
		}
	}

	private final BasicComboBoxEditor comboboxEditor = new FilterFieldComboboxEditor();

	public JREditableHistoryComboBox() {
		super();

		setModel(new DefaultComboBoxModel<String>());
		setEditable(true);
		setEditor(comboboxEditor);
		((JComponent) comboboxEditor.getEditorComponent()).setBorder(new EmptyBorder(0, 5, 0, 5));

	}

	/**
	 * Tells the text filter field to display it self in and active filter color.
	 */
	public void enableFilterColor(boolean enable) {
		if (enable) {
			((JTextComponent) comboboxEditor.getEditorComponent()).setBackground(SwingUtils.getSelectionBackgroundColor());
			((JTextComponent) comboboxEditor.getEditorComponent()).setForeground(SwingUtils.getSelectionForegroundColor());
			((JTextComponent) comboboxEditor.getEditorComponent()).setSelectionColor(SwingUtils.getSelectionBackgroundColor().brighter());
		} else {
			((JTextComponent) comboboxEditor.getEditorComponent()).setForeground(SwingUtils.getForegroundColor());
			((JTextComponent) comboboxEditor.getEditorComponent()).setBackground(SwingUtils.getBackgroundColor());
			((JTextComponent) comboboxEditor.getEditorComponent()).setSelectionColor(SwingUtils.getSelectionBackgroundColor());
		}
	}

	/**
	 * Set the item as editor value. 
	 * 
	 * @param item The item to be used for as the current editor value.
	 */
	public void setItem(String item) {
		comboboxEditor.setItem(item);
	}

	/**
	 * Add the given comma separated values to the history.
	 * 
	 * @param entries The entries to be put to the history.
	 */
	public void setHistoryValues(String entries) {
		final MutableComboBoxModel<String> model = (MutableComboBoxModel<String>) getModel();
		if (entries != null && entries.length() > 0) {
			List<String> split = ListUtils.split(entries, ",");
			for (String string : split) {
				model.addElement(string);
			}
		}
		updateUI();
	}

	/**
	 * Get all history values as comma separated string.
	 * 
	 * @return The comma separated history values.
	 */
	public String getHistoryValues() {
		ComboBoxModel<String> model = getModel();
		StringBuilder modelEntries = new StringBuilder();
		for (int i = 0; i < model.getSize(); i++) {
			String elementAt = StringUtil.replace(StringUtil.toString(model.getElementAt(i)), ",", EMPTY);
			if (modelEntries.length() > 0) {
				modelEntries.append(",");
			}
			modelEntries.append(elementAt);
		}
		return modelEntries.toString();
	}
	

	/**
	 * Adds the given value to the beginning of the combobox list. If the list exceeds more than 10 entries, the last ones will be deleted.
	 *
	 * @param value The search expression to be attached.
	 */
	public void addHistoryValue(final String value) {
		if (value != null && value.length() > 0) {
			MutableComboBoxModel<String> model = (MutableComboBoxModel<String>) getModel();
			model.insertElementAt(value, 0);
			if (model.getSize() > 10) {
				model.removeElementAt(model.getSize() - 1);
			}
			removeDuplicateElementsFromFilterModel(value);
		}
	}

	/**
	 * If the selected expression or any other one is more than one times in the filter history list, the last ones will be removed.
	 *
	 * @param selectedExpression
	 *            The currently selected filter expression.
	 */
	private void removeDuplicateElementsFromFilterModel(final String selectedExpression) {
		MutableComboBoxModel<String> model = (MutableComboBoxModel<String>) getModel();
		HashSet<String> entries = new HashSet<>(model.getSize());
		for (int i = 0; i < model.getSize(); i++) {
			String elementAt = (String) model.getElementAt(i);
			if (entries.contains(elementAt)) {
				model.removeElementAt(i);
				i--;
			}
			entries.add(elementAt);
		}
		model.setSelectedItem(selectedExpression);
	}	
	
	/**
	 * Gets the text from the filter/search field.
	 *
	 * @return The text from the filter/search field. Never returns <code>null</code>.
	 */
	public String getEditorValue() {
		return StringUtil.toString(getEditor().getItem());
	}	

  /**
   * Registers the given observer to begin receiving notifications
   * when changes are made to the document.
   *
   * @param listener the observer to register
   * @see Document#removeDocumentListener
   */
	public void addDocumentListener(DocumentListener listener) {
		((JTextField) comboboxEditor.getEditorComponent()).getDocument().addDocumentListener(listener);
	}
	
}
