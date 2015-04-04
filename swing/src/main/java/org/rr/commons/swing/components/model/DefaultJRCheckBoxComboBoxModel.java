package org.rr.commons.swing.components.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.rr.commons.swing.components.event.ListCheckListener;
import org.rr.commons.swing.components.event.ListEvent;
import org.rr.commons.utils.ListUtils;

public class DefaultJRCheckBoxComboBoxModel<E> implements JRCheckBoxComboBoxModel<E> {

	private Set<Integer> checks = new HashSet<>();

	private List<E> values;

	private List<String> labels;

	private List<ListCheckListener<E>> listeners = new ArrayList<ListCheckListener<E>>();

	public DefaultJRCheckBoxComboBoxModel(List<E> values, List<String> labels) {
		this.values = values;
		this.labels = labels;
		if(this.labels == null) {
			this.labels = new ArrayList<>();
		}
	}

	@Override
	public int getSize() {
		return values.size();
	}

	@Override
	public boolean isChecked(int index) {
		Integer value = Integer.valueOf(index);
		return checks.contains(value);
	}

	@Override
	public String getLabel(int index) {
		return ListUtils.get(labels, index);
	}

	@Override
	public void setChecked(int index, boolean checked) {
		Integer value = Integer.valueOf(index);
		if (checked) {
			checks.add(value);
			fireListCheckListenerAdded(getValueAt(index));
		} else {
			checks.remove(value);
			fireListCheckListenerRemoved(getValueAt(index));
		}
		return;
	}

	@Override
	public E getValueAt(int index) {
		return values.get(index);
	}

	public int getCheckCount() {
		return checks.size();
	}

	public List<E> getCheckedValues() {
		ArrayList<E> result = new ArrayList<>(checks.size());
		for (Integer check : checks) {
			result.add(getValueAt(check.intValue()));
		}
		return result;
	}
	
	public Set<Integer> getCheckedIndices() {
		return checks;
	}

	public void addListCheckListener(ListCheckListener<E> listCheckListener) {
		listeners.add(listCheckListener);
	}

	private void fireListCheckListenerAdded(E value) {
		for (ListCheckListener<E> listener : listeners) {
			ListEvent<E> listEvent = new ListEvent<>(this, Collections.singletonList(value));
			listener.addCheck(listEvent);
		}
	}
	
	private void fireListCheckListenerRemoved(E value) {
		for (ListCheckListener<E> listener : listeners) {
			ListEvent<E> listEvent = new ListEvent<>(this, Collections.singletonList(value));
			listener.removeCheck(listEvent);
		}
	}

	@Override
	public void addCheck(E toCheck) {
		for (int i = 0; i < values.size(); i++) {
			if(toCheck == values.get(i)) {
				setChecked(i, true);
				break;
			}
		}
	}
}
