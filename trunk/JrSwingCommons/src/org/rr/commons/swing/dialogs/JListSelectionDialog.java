package org.rr.commons.swing.dialogs;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Frame;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;

import net.miginfocom.swing.MigLayout;

import org.rr.commons.swing.SwingUtils;
import org.rr.commons.utils.StringUtils;


public class JListSelectionDialog<E> extends BaseDialog {

	public static interface DataModel<E> {
		public E getValueAt(int idx);
		public String getViewValueAt(int idx);
		public int getValueCount();
	}

	private DataModel<E> values;

	private String message;

	private List<Integer> selectedIndices;

	public JListSelectionDialog(Frame owner) {
		super(owner);
	}

	private void initialize() {
		setSize(400, 200);

		Container contentPane = getContentPane();

		JLabel messageLabel = new JLabel(StringUtils.toString(message));
		contentPane.add(messageLabel, BorderLayout.NORTH);

		JScrollPane choisePanelScrollPane = new JScrollPane();
		JPanel choisePanel = new JPanel();
		choisePanel.setLayout(new MigLayout());

		for(int i = 0; i < values.getValueCount(); i++) {
			choisePanel.add(new JCheckBox(values.getViewValueAt(i)), "wrap");
		}

		choisePanelScrollPane.setViewportView(choisePanel);
		choisePanelScrollPane.setBorder(new EmptyBorder(0,0,0,0));
		contentPane.add(choisePanelScrollPane, BorderLayout.CENTER);
	}

	public void setValues(DataModel<E> values) {
		this.values = values;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	/**
	 * Get the user selections from the dialog.
	 */
	public List<Integer> getSelectedIndices() {
		Component[] checkboxes = SwingUtils.getAllComponents(JCheckBox.class, getContentPane());
		List<Integer> result = new ArrayList<>(checkboxes.length);
		for (int i = 0; i < checkboxes.length; i++) {
			if(((JCheckBox)checkboxes[i]).isSelected()) {
				result.add(Integer.valueOf(i));
			}
		}
		return result;
	}

	/**
	 * Set the selected checkboxes by it's index.
	 * @param selectedIndices The indices of the checboxes which should be selected.
	 */
	public void setSelectedIndices(List<Integer> selectedIndices) {
		this.selectedIndices = selectedIndices;
	}

	@Override
	public void setVisible(boolean b) {
		if(b) {
			initialize();
			setupSelection();
		}
		super.setVisible(b);
	}

	private void setupSelection() {
		Component[] checkboxes = SwingUtils.getAllComponents(JCheckBox.class, getContentPane());
		for (int i = 0; i < checkboxes.length; i++) {
			boolean selected = selectedIndices.contains(Integer.valueOf(i));
			((JCheckBox)checkboxes[i]).setSelected(selected);
		}
	}

}
