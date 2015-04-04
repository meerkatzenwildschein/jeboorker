package org.rr.commons.swing.components.model;

import java.util.List;
import java.util.Set;

import org.rr.commons.swing.components.event.ListCheckListener;


public interface JRCheckBoxComboBoxModel<E> {
	
	public int getSize();
	
	public boolean isChecked(int index);
	
	public String getLabel(int index);
	
	public void setChecked(int index, boolean checked);
	
	public E getValueAt(int index);
	
	public int getCheckCount();
	
	public List<E> getCheckedValues();

	public void addListCheckListener(ListCheckListener<E> listCheckListener);

	public void addCheck(E valueAt);

	public Set<Integer> getCheckedIndices();
}
