package bd.amazed.docscissors.view;

import bd.amazed.docscissors.model.PageGroup;

public interface UIHandlerListener {

	public void editingModeChanged(int newMode);

	/**
	 * @param index 0 = stacked, 1 = first page
	 */
	public void pageChanged(int index);

	public void pageGroupSelected(PageGroup pageGroup);

	public void rectsStateChanged();

}
