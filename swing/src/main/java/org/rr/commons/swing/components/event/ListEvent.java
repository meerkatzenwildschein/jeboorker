package org.rr.commons.swing.components.event;

import java.util.List;

import org.rr.commons.swing.components.model.JRCheckBoxComboBoxModel;

public class ListEvent<T> {

	private JRCheckBoxComboBoxModel<T> source;
	private List<T> values;

	public ListEvent(JRCheckBoxComboBoxModel<T> source, List<T> values) {
		this.source = source;
		this.values = values;
	}

	@Override
	public String toString() {
		return getClass().getName() + " " + getValues();
	}

	public JRCheckBoxComboBoxModel<T> getSource() {
		return source;
	}

	public List<T> getValues() {
		return values;
	}

}
