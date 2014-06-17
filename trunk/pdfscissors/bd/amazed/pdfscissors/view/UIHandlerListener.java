package bd.amazed.pdfscissors.view;

import bd.amazed.pdfscissors.model.PageGroup;

public interface UIHandlerListener {

	public void editingModeChanged(int newMode);

	/**
	 * @param index 0 = stacked, 1 = first page
	 */
	public void pageChanged(int index);

	public void pageGroupSelected(PageGroup pageGroup);

	public void rectsStateChanged();

}
